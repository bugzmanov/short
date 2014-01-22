package bugzmanov.shortner.service

import java.net.URL
import scala.concurrent.Future
import bugzmanov.shortner.backend.KeyRepository
import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.pattern.ask
import akka.util.Timeout

trait ShortenerService {
  def expand(key: String): Future[Option[URL]]

  def shorten(url: URL): Future[String]
}

class ShortenerServiceImpl(serviceActor: ActorRef)
                          (implicit system: ActorSystem, timeout: Timeout) extends ShortenerService {
  
  private implicit def executionContext = system.dispatcher
  
  def expand(key: String): Future[Option[URL]] = (serviceActor ? Expand(key)).mapTo[ExpandResult].map { _.url.map(new URL(_)) }

  def shorten(url: URL): Future[String] = (serviceActor ? Shorten(url.toString)).mapTo[ShortenResult].map { _.key }
}

case class Shorten(url: String)
case class ShortenResult(url: String, key: String)

case class Expand(key: String)
case class ExpandResult(key: String, url: Option[String])

class ShortenerServiceActor(repo: KeyRepository) extends Actor {

  def receive = {
    case Shorten(url) =>
      sender ! ShortenResult(url, repo.store(url))

    case Expand(key) =>
      sender ! ExpandResult(key, repo.get(key))
   }
}

object ShortenerServiceActor {
  def props(repo: KeyRepository) = Props(classOf[ShortenerServiceActor], repo)
}