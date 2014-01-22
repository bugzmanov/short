package bugzmanov.shortner

import scala.concurrent.duration.Duration

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.io.IO

import spray.can.Http
import bugzmanov.shortner.web.ShortenerWebActor
import bugzmanov.shortner.service.{ShortenerServiceActor, ShortenerServiceImpl}
import bugzmanov.shortner.backend.{KeyRepository, SqlDBKeyRepository}

object ShortnerApp extends App {

  implicit val system: ActorSystem = ActorSystem("shortner")

  implicit val timeout: Timeout = Duration(5, "s")

  private val port: Int = 9000

  val service = {
    val keyRepository = new SqlDBKeyRepository

    keyRepository.createSchema()

    val keyRepoActor = system.actorOf(ShortenerServiceActor.props(keyRepository))

    val shortener: ShortenerServiceImpl = new ShortenerServiceImpl(keyRepoActor, "http://localhost:" + port)

    system.actorOf(ShortenerWebActor.props(shortener), "web-actor")
  }

  IO(Http) ! Http.Bind(service, interface = "localhost", port = port)
}
