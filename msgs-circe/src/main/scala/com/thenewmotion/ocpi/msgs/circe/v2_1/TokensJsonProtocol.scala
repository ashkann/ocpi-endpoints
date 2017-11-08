package com.thenewmotion.ocpi.msgs.circe.v2_1

import com.thenewmotion.ocpi.msgs.circe.SimpleStringEnumSerializer
import com.thenewmotion.ocpi.msgs.v2_1.Tokens._
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import LocationsJsonProtocol._
import CommonJsonProtocol._

trait TokensJsonProtocol {

  implicit val tokenTypeE: Encoder[TokenType] =
    SimpleStringEnumSerializer.encoder(TokenType)

  implicit val tokenTypeD: Decoder[TokenType] =
    SimpleStringEnumSerializer.decoder(TokenType)

  implicit val whitelistTypeE: Encoder[WhitelistType] =
    SimpleStringEnumSerializer.encoder(WhitelistType)

  implicit val whitelistTypeD: Decoder[WhitelistType] =
    SimpleStringEnumSerializer.decoder(WhitelistType)

  implicit val tokenUidE: Encoder[TokenUid] = stringEncoder(_.value)
  implicit val tokenUidD: Decoder[TokenUid] = tryStringDecoder(TokenUid.apply)

  implicit val authIdE: Encoder[AuthId] = stringEncoder(_.value)
  implicit val authIdD: Decoder[AuthId] = tryStringDecoder(AuthId.apply)

  implicit val tokenE: Encoder[Token] = deriveEncoder
  implicit val tokenD: Decoder[Token] = deriveDecoder

  implicit val tokenPatchE: Encoder[TokenPatch] = deriveEncoder
  implicit val tokenPatchD: Decoder[TokenPatch] = deriveDecoder

  implicit val locationReferencesE: Encoder[LocationReferences] = deriveEncoder
  implicit val locationReferencesD: Decoder[LocationReferences] = deriveDecoder

  implicit val allowedE: Encoder[Allowed] =
    SimpleStringEnumSerializer.encoder(Allowed)

  implicit val allowedD: Decoder[Allowed] =
    SimpleStringEnumSerializer.decoder(Allowed)

  implicit val authorizationInfoE: Encoder[AuthorizationInfo] = deriveEncoder
  implicit val authorizationInfoD: Decoder[AuthorizationInfo] = deriveDecoder
}

object TokensJsonProtocol extends TokensJsonProtocol
