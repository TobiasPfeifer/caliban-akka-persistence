package de.tobiaspfeifer.example.cap

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.http.scaladsl.Http
import akka.util.Timeout
import akka.{Done, actor}
import zio.DefaultRuntime

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}


object Application extends App {
  val system = ActorSystem[Done](Behaviors.setup { context =>

    implicit val untypedSystem: actor.ActorSystem = context.system.toClassic
    implicit val ec: ExecutionContextExecutor = context.system.executionContext
    implicit val timeout: Timeout = 3.seconds
    implicit val scheduler: Scheduler = context.system.scheduler

    implicit val defaultRuntime: DefaultRuntime = new DefaultRuntime {}

    val port = 8080

    val counter = context.spawn(Counter("counter"), "counter")
    val graphQl = GraphQlApi(counter)
    val routes = HttpRoutes.counterRoutes(graphQl.interpreter)

    val serverBinding: Future[Http.ServerBinding] = Http()(untypedSystem).bindAndHandle(routes, "localhost", port)
    serverBinding.onComplete {
      case Success(bound) =>
        println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
      case Failure(e) =>
        context.log.error(s"Server could not start!", e)
        context.self ! Done
    }
    Behaviors.receiveMessage {
      case Done =>
        serverBinding.onComplete(_.foreach(_.unbind()))
        Behaviors.stopped
    }

  }, "helloAkkaHttpServer")


  StdIn.readLine()
  println("Shutting down now")
  system.tell(Done)
}
