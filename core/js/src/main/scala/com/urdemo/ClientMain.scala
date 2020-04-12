package com.urdemo

import com.softwaremill.sttp.SttpBackend
import io.udash.rest.{DefaultSttpBackend, SttpRestClient}
import org.scalajs.dom.html.Div
import org.scalajs.dom.{document, window}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ClientMain {
  def main(args: Array[String]): Unit = {
    // STTP is an HTTP library which powers Udash REST JS-based client
    implicit val sttpBackend: SttpBackend[Future, Nothing] = DefaultSttpBackend()
    val demoApi: DemoApi = SttpRestClient[DemoApi](s"${window.location.origin}/api")

    val ui = document.getElementById("ui").asInstanceOf[Div]

    demoApi.upperEcho("dafuq").foreach { upper =>
      ui.appendChild(document.createTextNode(upper))
    }
  }
}
