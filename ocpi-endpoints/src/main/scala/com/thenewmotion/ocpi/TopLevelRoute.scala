package com.thenewmotion.ocpi

import com.thenewmotion.ocpi.handshake.{HandshakeErrorRejection, HandshakeError, InitiateHandshakeRoute}
import com.thenewmotion.ocpi.msgs.v2_0.OcpiStatusCodes.GenericSuccess
import com.thenewmotion.ocpi.msgs.v2_0.Versions._
import spray.http._, HttpHeaders._
import spray.routing._, authentication._, directives.FutureDirectives
import scala.concurrent.{ExecutionContext, Future}
import org.joda.time.DateTime
import spray.http.Uri

import scalaz.{\/-, -\/, \/}

trait TopLevelRoute extends JsonApi {
  import com.thenewmotion.ocpi.msgs.v2_0.OcpiJsonProtocol._

  def routingConfig: OcpiRoutingConfig

  def currentTime = DateTime.now

  val EndPointPathMatcher = Segment.flatMap {
    case s => EndpointIdentifier.withName(s)
  }

  trait TopLevelApi extends JsonApi {
    protected def leftToRejection[T](errOrX: HandshakeError \/ T)(f: T => Route): Route =
      errOrX match {
        case -\/(err) => reject(HandshakeErrorRejection(err))
        case \/-(res) => f(res)
      }

    protected def futLeftToRejection[T](errOrX: Future[HandshakeError \/ T])(f: T => Route)
      (implicit ec: ExecutionContext): Route = {
      FutureDirectives.onSuccess(errOrX) {
        case -\/(err) => reject(HandshakeErrorRejection(err))
        case \/-(res) => f(res)
      }
    }
  }

  def appendPath(uri: Uri, segments: String*) = {
    uri.withPath(segments.foldLeft(uri.path) {
      case (path, add) if path.toString().endsWith("/") => path + add
      case (path, add) => path / add
    })
  }

  def versionsRoute(uri: Uri): Route = routingConfig.versions match {
    case v if v.nonEmpty =>
      complete(VersionsResp(
        GenericSuccess.code,
        Some(GenericSuccess.default_message),
        currentTime,
        v.keys.map(x => Version(x, appendPath(uri, x).toString())).toList)
      )
    case _ => reject(NoVersionsRejection())
  }

  def versionRoute(version: String, versionInfo: OcpiVersionConfig, uri: Uri, apiUser: ApiUser): Route =
    pathEndOrSingleSlash {
      complete(
        VersionDetailsResp(
          GenericSuccess.code,
          Some(GenericSuccess.default_message),
          currentTime,
          VersionDetails(
            version, versionInfo.endPoints.map {
              case (k, v) => Endpoint(k, appendPath(uri, k.name).toString() )
            }.toList
          )
        )
      )
    } ~
    pathPrefix(EndPointPathMatcher) { path =>
      versionInfo.endPoints.get(path) match {
        case None => reject
        case Some(route) => route(version, apiUser.token)
      }
    }


  def topLevelRoute(implicit ec: ExecutionContext) = {
    val externalUseToken = new TokenAuthenticator(routingConfig.authenticateApiUser)
    val internalUseToken = new TokenAuthenticator(routingConfig.authenticateInternalUser)

    (pathPrefix(routingConfig.namespace) & extract(_.request.uri)) { uri =>
      pathPrefix("initiateHandshake") {
        pathEndOrSingleSlash {
          authenticate(internalUseToken) { internalUser: ApiUser =>
            new InitiateHandshakeRoute(routingConfig.handshakeService).route
          }
        }
      } ~
      authenticate(externalUseToken) { apiUser: ApiUser =>
        pathPrefix(EndpointIdentifier.Versions.name) {
          pathEndOrSingleSlash {
            versionsRoute(uri)
          } ~
          pathPrefix(Segment) { version =>
            routingConfig.versions.get(version) match {
              case None => reject(UnsupportedVersionRejection(version))
              case Some(validVersion) => versionRoute(version, validVersion, uri, apiUser)
            }
          }
        }
      }
    }
  }
}

class TokenAuthenticator(
  apiUser: String => Option[ApiUser]
)(implicit val executionContext: ExecutionContext) extends HttpAuthenticator[ApiUser] {

  val challenge = `WWW-Authenticate`(
    HttpChallenge(scheme = "Token", realm = "???", params = Map.empty)) :: Nil

  def authenticate(credentials: Option[HttpCredentials], ctx: RequestContext) =
    Future(
      credentials
        .flatMap {
          case GenericHttpCredentials("Token", token, _) => Some(token)
          case _ => None
        }
        .flatMap(apiUser)
    )

  def getChallengeHeaders(r: HttpRequest) = challenge
}
