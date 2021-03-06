package de.tobiaspfeifer.example.cap

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import caliban.{AkkaHttpAdapter, GraphQLInterpreter}
import zio.Runtime

import scala.concurrent.ExecutionContext


object HttpRoutes {
  def counterRoutes(interpreter: GraphQLInterpreter[zio.ZEnv, _])(
    implicit ec: ExecutionContext, runtime: Runtime[zio.ZEnv]): Route = {
    logRequestResult("counter-routes") {
      concat {
        pathPrefix("graphql") {
          pathEnd {
            AkkaHttpAdapter.makeHttpService(interpreter)(ec, runtime)
          }
        } ~
          pathPrefix("graphiql") {
            pathEnd {
              get {
                getFromResource("graphiql.html")
              }
            }
          }
      }
    }
  }

}
