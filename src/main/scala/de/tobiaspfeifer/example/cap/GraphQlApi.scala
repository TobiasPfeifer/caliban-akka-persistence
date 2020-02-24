package de.tobiaspfeifer.example.cap

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import caliban.GraphQL.graphQL
import caliban.{GraphQL, RootResolver}
import de.tobiaspfeifer.example.cap.Counter.{Decrement, GetCount, Increment, SetDescription}
import io.scalaland.chimney.dsl._

import scala.concurrent.{ExecutionContextExecutor, Future}

object GraphQlApi {


  def apply(counterRef: ActorRef[Counter.Command])(implicit scheduler: Scheduler, timeout: Timeout, ec: ExecutionContextExecutor): GraphQL[zio.ZEnv] = {
    implicit val converter: Future[Counter.Count] => Future[Count] = _.map(_.transformInto[Count])

    def incrementCount(args: IncArgs): Future[Counter.Count] = {
      counterRef ? (Increment(_, args.n))
    }

    def decrementCount(args: DecArgs): Future[Counter.Count] = {
      counterRef ? (Decrement(_, args.n))
    }

    def setDescription(args: DescriptionArgs): Future[Counter.Count] = {
      counterRef ? (SetDescription(_, args.ifCountMatching, args.description))
    }

    def getCount(): Future[Counter.Count] = {
      counterRef ? (GetCount(_))
    }

    graphQL(RootResolver(
      Queries(
        getCount()
      ),
      Mutations(
        args => incrementCount(args),
        args => decrementCount(args),
        args => setDescription(args)
      )
    ))
  }

  case class Count(count: Long, description: Option[String], lastUpdateTimestamp: Long)

  case class Queries(
                      count: Future[Count]
                    )

  case class IncArgs(n: Long)

  case class DecArgs(n: Long)

  case class DescriptionArgs(ifCountMatching: Option[Long], description: String)

  case class Mutations(
                        increment: IncArgs => Future[Count],
                        decrement: DecArgs => Future[Count],
                        setDescription: DescriptionArgs => Future[Count]
                      )

}
