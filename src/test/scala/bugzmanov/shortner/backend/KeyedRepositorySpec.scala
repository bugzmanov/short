package bugzmanov.shortner.backend

import java.util.UUID
import org.specs2.mutable.Specification

trait KeyedRepositorySpec extends Specification {

  def repo: KeyRepository

  "repository" should {
    "return saved url for generated key" in {
      val url = "http://badoo.com"
      val key = repo.store(url)
      val result = repo.get(key)
      result mustEqual Some(url)
    }

    "return None if no entry exists for url" in {
      val result = repo.get(UUID.randomUUID().toString)
      result mustEqual None
    }

    "return same key for same url provided" in {
      val url = "http://ya.ru"
      val firstKey = repo.store(url)
      val secondKey = repo.store(url)
      firstKey mustEqual secondKey
    }

    "return different keys for different urls" in {
      val firstKey = repo.store("http://google.com")
      val secondKey = repo.store("http://bing.com")
      firstKey mustNotEqual secondKey
    }
  }
}
