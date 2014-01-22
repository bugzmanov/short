package bugzmanov.shortner.util
import Stream._

object UrlEncoder {
  val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
  val base     = 64

  def encode(input: Long): String = {
    require(input >= 0)

    def stream(num: Long): Stream[Long] =  if (num > 0) num #:: stream(num / base) else empty

    stream(input).foldRight("") { case (n, str) => str + alphabet.charAt((n % base).toInt) }
  }

  def decode(str: String): Long = str.reverse.foldRight(0L) { case (c, summ) => summ * base + alphabet.indexOf(c) }
}