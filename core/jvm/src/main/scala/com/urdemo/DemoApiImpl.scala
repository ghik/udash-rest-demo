package com.urdemo

import scala.concurrent.Future

class DemoApiImpl extends DemoApi {
  def upperEcho(message: String): Future[String] =
    Future.successful(message.toUpperCase)
}
