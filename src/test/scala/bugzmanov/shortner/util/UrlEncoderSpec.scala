package bugzmanov.shortner.util

import org.specs2.mutable.Specification
import scala.util.Random

class UrlEncoderSpec extends Specification  {

  import UrlEncoder._

  "UrlEncoder" should {
    "return same value after decoding encoded value" in {
      Seq.fill(100)(Random.nextInt(Int.MaxValue)).foreach { v =>
        decode(encode(v)) must_==  v
      }

      decode(encode(Long.MaxValue)) must_==  Long.MaxValue
    }
  }

}
