package de.tobiaspfeifer.example.cap

import java.util.Objects

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}

object Counter {
  type State = Count
  lazy val initialState = Count(0, None, 0, 0)
  private val eventHandler: (State, Event) => State = (state: State, event: Event) => {
    event match {
      case Resetted(lastUpdateTimestamp) => initialState.copy(lastUpdateTimestamp = lastUpdateTimestamp)
      case Decremented(byN, lastUpdateTimestamp) => state.copy(count = state.count - byN, lastUpdateTimestamp = lastUpdateTimestamp)
      case Incremented(byN, lastUpdateTimestamp) => state.copy(count = state.count + byN, lastUpdateTimestamp = lastUpdateTimestamp)
      case DescriptionUpdated(str, lastUpdateTimestamp) => state.copy(state.count, Some(str), lastUpdateTimestamp, Objects.hashCode(str))
    }
  }
  private val commandHandler: (State, Command) => Effect[Event, State] = (state: State, command: Command) => {
    val now = System.currentTimeMillis()
    command match {
      case Reset(replyTo) => Effect.persist(Resetted(now)).thenReply(replyTo)(identity)
      case GetCount(replyTo) => Effect.reply(replyTo)(state)
      case Decrement(replyTo, byN) =>
        if (state.count >= byN) Effect.persist(Decremented(byN, now)).thenReply(replyTo)(identity)
        else Effect.reply(replyTo)(state)
      case Increment(replyTo, byN) =>
        if (state.count >= Long.MaxValue - byN) Effect.reply(replyTo)(state)
        else Effect.persist(Incremented(byN, now)).thenReply(replyTo)(identity)
      case SetDescription(replyTo, ifCountMatching, description) =>
        lazy val successEffect: ReplyEffect[Event, State] = Effect.persist(DescriptionUpdated(description, now)).thenReply(replyTo)(identity)
        ifCountMatching.fold(successEffect) { guard =>
          if (guard == state.count) successEffect
          else Effect.reply(replyTo)(state)
        }
    }
  }

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior(
      PersistenceId.ofUniqueId(id),
      initialState,
      commandHandler,
      eventHandler
    )

  sealed trait Command {
    val replyTo: ActorRef[Count]
  }

  sealed trait Event extends JsonSerializable {
    val lastUpdateTimestamp: Long
  }

  case class Count(count: Long, description: Option[String], lastUpdateTimestamp: Long, someInternalProperty: Int)

  final case class Reset(replyTo: ActorRef[Count]) extends Command

  final case class GetCount(replyTo: ActorRef[Count]) extends Command

  final case class SetDescription(replyTo: ActorRef[Count], ifCountMatching: Option[Long], description: String) extends Command

  final case class Increment(replyTo: ActorRef[Count], byN: Long) extends Command

  final case class Decrement(replyTo: ActorRef[Count], byN: Long) extends Command

  final case class Resetted(lastUpdateTimestamp: Long) extends Event

  final case class Decremented(byN: Long, lastUpdateTimestamp: Long) extends Event

  final case class Incremented(byN: Long, lastUpdateTimestamp: Long) extends Event

  final case class DescriptionUpdated(str: String, lastUpdateTimestamp: Long) extends Event

}
