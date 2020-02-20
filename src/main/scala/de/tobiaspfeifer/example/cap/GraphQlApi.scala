package de.tobiaspfeifer.example.cap

import caliban.GraphQL.graphQL
import caliban.{GraphQL, RootResolver}
import io.scalaland.chimney.dsl._
import zio.Task

object GraphQlApi {

  def apply(counterService: CounterService): GraphQL[zio.ZEnv] = {
    implicit val converter: Task[Counter.Count] => Task[Count] = _.map(_.transformInto[Count])

    graphQL(RootResolver(
      Queries(
        counterService.getCounter
      ),
      Mutations(
        args => counterService.incrementBy(args.n),
        args => counterService.decrementBy(args.n),
        args => counterService.setDescription(args.ifCountMatching, args.description)
      )
    ))
  }

  case class Count(count: Long, description: Option[String], lastUpdateTimestamp: Long)

  case class Queries(
                      count: Task[Count]
                    )

  case class IncArgs(n: Long)

  case class DecArgs(n: Long)

  case class DescriptionArgs(ifCountMatching: Option[Long], description: String)

  case class Mutations(
                        increment: IncArgs => Task[Count],
                        decrement: DecArgs => Task[Count],
                        setDescription: DescriptionArgs => Task[Count]
                      )

}
