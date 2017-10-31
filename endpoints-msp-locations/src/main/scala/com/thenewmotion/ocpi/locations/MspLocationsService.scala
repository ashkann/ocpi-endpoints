package com.thenewmotion.ocpi
package locations

import com.thenewmotion.ocpi.common.CreateOrUpdateResult
import msgs.GlobalPartyId
import msgs.v2_1.Locations._

import scala.concurrent.Future

/**
  * All methods are to be implemented in an idempotent fashion.
  */
trait MspLocationsService {

  def createOrUpdateLocation(
    globalPartyId: GlobalPartyId,
    locId: LocationId,
    loc: Location
  ): Future[Either[LocationsError, CreateOrUpdateResult]]

  def addOrUpdateEvse(
    globalPartyId: GlobalPartyId,
    locId: LocationId,
    evseUid: EvseUid,
    evse: Evse
  ): Future[Either[LocationsError, CreateOrUpdateResult]]

  def addOrUpdateConnector(
    globalPartyId: GlobalPartyId,
    locId: LocationId,
    evseUid: EvseUid,
    connId: ConnectorId,
    connector: Connector
  ): Future[Either[LocationsError, CreateOrUpdateResult]]

  def updateLocation(
    globalPartyId: GlobalPartyId,
    locId: LocationId,
    locPatch: LocationPatch
  ): Future[Either[LocationsError, Unit]]

  def updateEvse(
    globalPartyId: GlobalPartyId,
    locId: LocationId,
    evseUid: EvseUid,
    evsePatch: EvsePatch
  ): Future[Either[LocationsError, Unit]]

  def updateConnector(
    globalPartyId: GlobalPartyId,
    locId: LocationId,
    evseUid: EvseUid,
    connId: ConnectorId,
    connectorPatch: ConnectorPatch
  ): Future[Either[LocationsError, Unit]]

  def location(
    globalPartyId: GlobalPartyId,
    locId: LocationId
  ): Future[Either[LocationsError, Location]]

  def evse(
    globalPartyId: GlobalPartyId,
    locId: LocationId,
    evseUid: EvseUid
  ): Future[Either[LocationsError, Evse]]

  def connector(
    globalPartyId: GlobalPartyId,
    locId: LocationId,
    evseUid: EvseUid,
    connectorId: ConnectorId
   ): Future[Either[LocationsError, Connector]]

}
