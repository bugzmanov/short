package bugzmanov.shortner.web

import java.net.{URLEncoder, URL}
import java.net.URLEncoder._

import scala.concurrent.Future

import spray.http._
import spray.http.MediaTypes._
import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http.StatusCodes._
import bugzmanov.shortner.service.ShortenerService

class WebServiceSpec extends Specification with Specs2RouteTest with ShortenerWebService {
  implicit def executionContext = system.dispatcher

  def actorRefFactory = system

  val domain: String = "http://localhost:9000/"

  def shortener = {
    new ShortenerService {
      def expand(key: String): Future[Option[URL]] = {
        if (key == "666")
          throw new RuntimeException("thou shall not pass")
        else
          Future(Some(new URL("http://www.google.com/" + key)))
      }

      def shorten(url: URL): Future[String] = Future(domain + url.toString.hashCode().toString)
    }
  }

  def fillForm(url: String): HttpEntity = {
    val encoded = URLEncoder.encode(url, "UTF-8")
    HttpEntity(`application/x-www-form-urlencoded`, s"url=$encoded")
  }

  "The service " should {
    "return a greeting for GET requests to the root path" in {
      Get() ~> router ~> check {
        entityAs[String] must contain("Enter url")
      }
    }

    "return expanded URL for GET request with key" in {
      Get("/123") ~> router ~> check {
        entityAs[String] must contain("http://www.google.com/123")
      }
    }

    "return shortened url for POST with url field if url is valid" in {
      val url = "http://google.com"
      Post("/", fillForm(url)) ~> router ~> check {
        entityAs[String] must contain(domain + url.hashCode())
      }
    }

    "return BadRequest status if url is invalid" in {
      val url = "sdfgsd:!@!3google.com"
      Post("/", fillForm(url)) ~> router ~> check {
        status must_== BadRequest
      }
    }

    "return BadRequest status if url is not http based" in {
      val url = "ftp://google.com"
      Post("/", fillForm(url)) ~> router ~> check {
        status must_== BadRequest
      }
    }

    "return InternalServerError status if request nukes server" in {
      Get("/666") ~> router ~> check {
        status must_== InternalServerError
      }
    }
  }
}
