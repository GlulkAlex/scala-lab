package concurrency

import scala.language.postfixOps
import scala.util._
import scala.util.control.NonFatal
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.async.Async.{async, await}

object tryouts {

  def computeSomeStrings: List[String] = List("hello",
                                              "did you understand the futures/promises?",
                                              "I'm beginning to see something, is this light?")

  def simpleFuture(): Unit = {
    println("Start - simpleFuture method.")
    val f: Future[List[String]] = future {
      computeSomeStrings
    }

    f onComplete {
      case Success(x) => println("future completes: " + x)
      case Failure(t) => println("an error occurred.")
    }

    println("Stop - simpleFuture method.")
  }
// [info] Running concurrency.tryouts
// Start - simpleFuture method.
// Stop - simpleFuture method.
// future completes: List(hello, did you understand the futures/promises?)
// [success] Total time: 1 s, completed 20 nov. 2013 09:25:05

  def simpleFutureOnSuccessOnFailure(): Unit = {
    println("Start - simpleFutureOnSuccessOnFailure method.")
    val f: Future[List[String]] = future {
      computeSomeStrings
    }

    f onSuccess {
      case x => println("future completes: " + x)
    }

    f onFailure {
      case t => println("an error occurred.")
    }

    println("Stop - simpleFutureOnSuccessOnFailure method.")
  }
// [info] Running concurrency.tryouts
// Start - simpleFutureOnSuccessOnFailure method.
// future completes: List(hello, did you understand the futures/promises?)
// Stop - simpleFutureOnSuccessOnFailure method.
// [success] Total time: 1 s, completed 20 nov. 2013 13:16:18

  def simpleFutureWithFailure(): Unit = {
    println("Start - simpleFutureWithFailure method.")
    val f = future(2 / 0)

    f onFailure {
      case t: ArithmeticException =>
        println("This is the right exception, the other will not be triggered fortunately.")
      case t: NullPointerException =>
        println("I'd be amazed if this printed out.")
    }

    println("Stop - simpleFutureWithFailure method.")
  }
// [info] Running concurrency.tryouts
// Start - simpleFutureWithFailure method.
// Stop - simpleFutureWithFailure method.
// This is the right exception, the other will not be triggered fortunately.
// [success] Total time: 1 s, completed 20 nov. 2013 13:31:57

  def simpleFutureWillPrependANewStringToList(): Unit = {
    println("Start - simpleFutureWillPrependANewStringToList method.")

    val f: Future[List[String]] = future {
      computeSomeStrings
    }

    val newStringPrependActionFuture: Future[List[String]] =
      f map (xs => "prepend a new string to the list!" :: xs)

    newStringPrependActionFuture onComplete {
      case Success(xs) => println(xs)
      case Failure(_) => println("error")
    }

    println("Stop - simpleFutureWillPrependANewStringToList method.")
  }
// [info] Running concurrency.tryouts
// Start - simpleFutureWillPrependANewStringToList method.
// Stop - simpleFutureWillPrependANewStringToList method.
// List(prepend a new string to the list!, hello, did you understand the futures/promises?, I'm beginning to see something, is this light?)
// [success] Total time: 0 s, completed 20 nov. 2013 13:40:16

  def simpleFirstFutureThrowsSoMapFutureWillTransmitTheError(): Unit = {
    println("Start - simpleFirstFutureThrowsSoMapFutureWillTransmitTheError method.")

    val f: Future[Int] = future {
      1 / 0
    }

    val newAddFutureActionFuture: Future[Int] =
      f map (n => 1 + n)

    newAddFutureActionFuture onComplete {
      case Failure(t) => println(t + " happened!")
      case _          => println("this will not happen")
    }

    println("Stop - simpleFirstFutureThrowsSoMapFutureWillTransmitTheError method.")
  }
// [info] Running concurrency.tryouts
// Start - simpleFirstFutureThrowsSoMapFutureWillTransmitTheError method.
// Stop - simpleFirstFutureThrowsSoMapFutureWillTransmitTheError method.
// java.lang.ArithmeticException: / by zero happened!
// [success] Total time: 0 s, completed 20 nov. 2013 13:44:02

  def loadFileFromFuture(): Unit = {
    println("Start - loadFileFromFuture method.")
    val firstOccurrence: Future[Int] = future {
        val source = scala.io.Source.fromFile("src/main/resources/linuxwords.txt")
        source.toSeq.indexOfSlice("Zulu")
      }

    firstOccurrence onComplete {
      case Success(x) => println("Occurence was found at position " + x)
    }
    println("Stop - loadFileFromFuture method.")
  }
// [info] Running concurrency.tryouts
// Start - loadFileFromFuture method.
// Stop - loadFileFromFuture method.
// [success] Total time: 4 s, completed 20 nov. 2013 18:57:00
// > Occurence was found at position 408808

  def main(args: Array[String]): Unit = {
//    simpleFuture
//    simpleFutureOnSuccessOnFailure
//    simpleFutureWillPrependANewStringToList
//    simpleFirstFutureThrowsSoMapFutureWillTransmitTheError
    loadFileFromFuture
  }

}
