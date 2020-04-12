package com.urdemo

import io.udash.rest.{DefaultRestApiCompanion, GET}

import scala.concurrent.Future

trait DemoApi {
  // message will become a query parameter
  @GET def upperEcho(message: String): Future[String]
}
object DemoApi extends DefaultRestApiCompanion[DemoApi]
