package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.Failure
import scala.util.Success

object App {
  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val routes = new TenderRoutes()(context.system)
      startHttpServer(routes.tenderRoutes)(context.system)
      Behaviors.empty
    }
    ActorSystem[Nothing](rootBehavior, "AkkaHttpServer")
  }

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext
    Http().newServerAt("localhost", 8080).bind(routes) onComplete {
      case Success(binding) => system.log.info("Server online at http://{}:{}/", binding.localAddress.getHostString, binding.localAddress.getPort)
      case Failure(_) => system.terminate()
    }
  }
}
