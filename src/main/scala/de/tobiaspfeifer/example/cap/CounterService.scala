package de.tobiaspfeifer.example.cap

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import de.tobiaspfeifer.example.cap.Counter.{Decrement, GetCount, Increment, SetDescription}
import zio.{Task, ZIO}

class CounterService(counter: ActorRef[Counter.Command], scheduler: Scheduler, timeout: Timeout) {

  implicit val s: Scheduler = scheduler
  implicit val t: Timeout = timeout

  def getCounter: Task[Counter.Count] = {
    ZIO.fromFuture { _ =>
      counter ? GetCount
    }
  }

  def incrementBy(n: Long): Task[Counter.Count] = {
    ZIO.fromFuture { _ =>
      counter ? (Increment(_, n))
    }
  }

  def decrementBy(n: Long): Task[Counter.Count] = {
    ZIO.fromFuture { _ =>
      counter ? (Decrement(_, n))
    }
  }

  def setDescription(ifCountMatching: Option[Long], description: String): Task[Counter.Count] = {
    ZIO.fromFuture { _ =>
      counter ? (SetDescription(_, ifCountMatching, description))
    }
  }
}

object CounterService {
  def apply(counter: ActorRef[Counter.Command])(implicit scheduler: Scheduler, timeout: Timeout): CounterService = new CounterService(counter, scheduler, timeout)
}
