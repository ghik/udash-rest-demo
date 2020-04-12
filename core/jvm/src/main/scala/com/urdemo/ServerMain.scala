package com.urdemo

import io.udash.rest.RestServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{HandlerCollection, ResourceHandler}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.resource.Resource

object ServerMain {
  def main(args: Array[String]): Unit = {
    val server = new Server(8080)

    val apiHandler = new ServletContextHandler
    val servlet = RestServlet[DemoApi](new DemoApiImpl)
    apiHandler.addServlet(new ServletHolder(servlet), "/api/*")

    val staticHandler = new ResourceHandler
    staticHandler.setBaseResource(Resource.newClassPathResource("/"))

    server.setHandler(new HandlerCollection(staticHandler, apiHandler))
    server.start()

    server.join()
  }
}
