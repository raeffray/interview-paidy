package forex.services.interpreters

import cats.Id
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.cache.CacheService
import forex.services.rates.interpreters.{CacheableRateService, OneFrameLookup, OneFrameResponse}
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.OffsetDateTime
import scala.util.Try;

/**
 * This is a test for CacheableRateService class which is a wrapper around OneFrameLookup
 *
 * @author Renato Barbosa
 *
 *
 * */
class CacheableRateServiceTest extends AnyFlatSpec with Matchers with MockFactory with EitherValues {

  private val cacheService = mock[CacheService[Rate]]

  private val oneFrameLookup = mock[OneFrameLookup]

  private val cacheableRateServiceSubject = new CacheableRateService[Id](cacheService, oneFrameLookup)

  it should "retrieve rate from cache when it's already there" in {
    val pair = Pair(Currency.fromString("EUR"), Currency.fromString("USD"))
    val cachedRate = Rate(pair, Price(1.2), Timestamp.now)
    (cacheService.get _).expects("EURUSD").returns(Some(cachedRate))
    cacheableRateServiceSubject.get(pair) shouldBe Right(cachedRate)
  }

  it should "fetch and cache new rate when it's not in cache" in {
    val pair = Pair(Currency.fromString("EUR"), Currency.fromString("USD"))

    val response = OneFrameResponse("EUR", "USD", 1.3, 1.2, 1.4, "2022-02-27T12:00:00Z")

    (() => oneFrameLookup.findNewRate).expects().returns(Try(Vector(response)))

    (cacheService.get _).expects("EURUSD").returns(None).once()
    (cacheService.put _).expects("EURUSD", Rate(pair, Price(response.price), Timestamp(OffsetDateTime.parse(response.time_stamp))))
    val cachedRate = Rate(pair, Price(response.price), Timestamp(OffsetDateTime.parse(response.time_stamp)))
    (cacheService.get _).expects("EURUSD").returns(Some(cachedRate)).once()

    val rate = cacheableRateServiceSubject.get(pair).getOrElse(null)
    rate.pair shouldBe pair
    rate.price.value shouldBe response.price
    rate.timestamp.value shouldBe OffsetDateTime.parse(response.time_stamp)

  }

  it should "fetch and cache new rate when it's not in cache, but none was received" in {
    val pair = Pair(Currency.fromString("EUR"), Currency.fromString("USD"))
    (cacheService.get _).expects("EURUSD").returns(None).once()
    (() => oneFrameLookup.findNewRate).expects().returns(Try(Vector.empty[OneFrameResponse]))
    (cacheService.get _).expects("EURUSD").returns(None).once()
    assertThrows[Exception] {
      cacheableRateServiceSubject.get(pair)
    }
  }
}