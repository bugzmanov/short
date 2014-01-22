package bugzmanov.shortner.web

import java.net.URL

import scala.concurrent.ExecutionContext
import scala.util.{Try, Success, Failure}

import akka.actor.Props

import spray.util.actorSystem
import spray.routing._
import spray.http._
import spray.http.StatusCodes.{NotFound, BadRequest, InternalServerError, Found}
import bugzmanov.shortner.service.ShortenerService


trait ShortenerWebService extends HttpService {

  private implicit def executionContext: ExecutionContext = actorSystem.dispatcher

  def shortener: ShortenerService

  def domainAddress: String

  private def validate(url:String): Either[String, URL]= Try(new URL(url)) match {
    case Success(url0) =>
      if (url0.getProtocol == "http") Right(url0) else Left("Only http protocol is supported")
    case _ =>
      Left("Not a valid url string")
  }

  def expand(key: String): RequestContext => Unit = { implicit ctx =>
    shortener.expand(key) onComplete {
      case Success(Some(url)) => ctx.redirect(Uri(url.toString), Found)
      case Success(None) => ctx.complete(NotFound)
      case Failure(e) => ctx.complete(InternalServerError)
    }
  }

  def shorten(url: String): RequestContext => Unit = { implicit ctx =>
    validate(url) match {
      case Right(url0) =>
        shortener.shorten(url0) onComplete {
          case Success(key) => Html.shortenResult(domainAddress + "/" + key, url0.toString)
          case Failure(e) => ctx.complete(InternalServerError, "Ooops")
        }
      case Left(error) => ctx.complete(BadRequest, error)
    }
  }

  val router = {
    path("") {
      get { implicit ctx => Html.welcome }
    } ~
    path(Segment) { key =>
      get(expand(key))
    }~
    path("") {
      post {
        formField('url.as[String]) { url =>
          shorten(url)
        }
      }
    }
  }
}

final class ShortenerWebActor(val shortener: ShortenerService, val domainAddress: String)
  extends HttpServiceActor with ShortenerWebService {
  def receive = runRoute(router)
}

object ShortenerWebActor {
  def props(shortener: ShortenerService, domainAddress: String) =
    Props(classOf[ShortenerWebActor], shortener, domainAddress)
}

//todo: damn, this is ugly
private object Html {

  def welcome(implicit ctx: RequestContext) = ctx.complete {
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <meta charset="utf-8"/>
        <title>URL Shortener</title>
      </head>
      <body>
        <form method="POST">
          <h2>Enter url</h2>
          <input name="url" type="text"/>
          <button type="submit">Submit</button>
        </form>
      </body>
    </html>
  }
  
  def shortenResult(shortenedUrl:String, originalUrl: String)(implicit ctx: RequestContext) = ctx.complete {
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <meta charset="utf-8"/>
        <title>URL Shortener</title>
      </head>
      <body>
        <h2>Shortened url: </h2>
        <p>{originalUrl} --&gt; {shortenedUrl}</p>
        <form method="POST">
          <h2>Enter url</h2>
          <input name="url" type="text"/>
          <button type="submit">Submit</button>
        </form>
      </body>
    </html>
  }
}