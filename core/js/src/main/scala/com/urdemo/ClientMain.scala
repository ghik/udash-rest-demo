package com.urdemo

import com.softwaremill.sttp.SttpBackend
import io.udash.rest.{DefaultSttpBackend, SttpRestClient}
import org.scalajs.dom.html.Div
import org.scalajs.dom.{document, window}
import scalatags.JsDom.all._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ClientMain {
  def main(args: Array[String]): Unit = {
    // STTP is an HTTP library which powers Udash REST JS-based client
    implicit val sttpBackend: SttpBackend[Future, Nothing] = DefaultSttpBackend()
    val demoApi: DemoApi = SttpRestClient[DemoApi](s"${window.location.origin}/api")

    val textInput =
      input(`type` := "text").render

    val button = input(
      `type` := "button",
      value := "Convert to uppercase using REST",
      onclick := { () =>
        demoApi.upperEcho(textInput.value).foreach { upper =>
          textInput.value = upper
        }
      }
    )

    val ui = div(textInput, button)

    document.body.appendChild(ui.render)
  }
}
