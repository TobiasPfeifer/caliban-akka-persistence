package de.tobiaspfeifer.example.cap

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import de.tobiaspfeifer.example.cap.Counter.{Decrement, GetCount, Increment, SetDescription}
import zio.{Task, UIO, ZIO}

class CounterService(counter: ActorRef[Counter.Command], scheduler: Scheduler, timeout: Timeout) {

  implicit val s: Scheduler = scheduler
  implicit val t: Timeout = timeout

  def getCounter: Task[Counter.Count] = {
    ZIO.fromFuture { ec =>
      counter ? GetCount
    }
  }

  def incrementBy(n: Long): Task[Counter.Count] = {
    ZIO.fromFuture { ec =>
      counter ? (Increment(_, n))
    }
  }

  def decrementBy(n: Long): Task[Counter.Count] = {
    ZIO.fromFuture { ec =>
      counter ? (Decrement(_, n))
    }
  }

  def setDescription(ifCountMatching: Option[Long], description: String): Task[Counter.Count] = {
    ZIO.fromFuture { ec =>
      counter ? (SetDescription(_, ifCountMatching, description))
    }
  }
}

object CounterService {
  def make(countManager: ActorRef[Counter.Command], scheduler: Scheduler, timeout: Timeout): UIO[CounterService] = {
    for {
      manager <- ZIO.succeed(countManager)
    } yield new CounterService(manager, scheduler, timeout)
  }
}
