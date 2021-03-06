package concurrency.actor

import akka.actor.{Actor, Props, ActorRef, ReceiveTimeout, ActorLogging}
import scala.concurrent.duration._

object Controller {
  case class Check(link: String, depth: Int)
  case class Result(links: Set[String])
}

/** Class in charge of spawning Getter to retrieve url's content.
  */
class Controller extends Actor with ActorLogging {
  import Controller._
  import context.dispatcher

  context.system.scheduler.scheduleOnce(10.seconds, self, ReceiveTimeout)

  var cacheLinks = Set.empty[String]
  var children = Set.empty[ActorRef]

  def receive = {
    case Check(url, depth) =>
      log.debug("{} checking {}", depth, url)
      if(!cacheLinks(url) && depth > 0) // spawn a new Getter in charge of retrieving the url's content
        children += context.actorOf(Props(new Getter(url, depth - 1)))
      cacheLinks += url
    case Getter.Done       => //
      children -= sender
      if (children.isEmpty) context.parent ! Result(cacheLinks) // computation done, we send the result to the caller
    case ReceiveTimeout    => // this way, we do not wait too long and be able to send a stop all to every Getter spawned
      children foreach(_ ! Getter.Abort)
  }
}
