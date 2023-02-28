package forex.services.rates.interpreters

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.cache.CacheService
import forex.services.rates.Algebra
import forex.services.rates.errors._
import org.slf4j.LoggerFactory

import java.time.OffsetDateTime

/** *
 *
 * Live Interpreter for One-Service API. Retrieved a live rate for a given pair of currencies. (See @Currency)
 *
 * @author Renato Barbosa
 * @constructor Creates a new OneFrame interpreter with a given configuration and a cache service
 * @param rateServiceConfig the service configuration, such as one-service endpoint and port (See @RateServiceConfig)
 * @param cacheService      service in charge to cache rates (see @CacheService)
 *
 */
class CacheableRateService[F[_] : Applicative](cacheService: CacheService[Rate], oneFrameLookup: OneFrameLookup)
  extends Algebra[F] {

  private val logger = LoggerFactory.getLogger("forex.services.rates.interpreters.OneFrame")

  private def createCacheKey(currencyFrom: String, currencyTo: String): String = s"$currencyFrom$currencyTo"

  override def get(pair: Rate.Pair): F[Error Either Rate] = {

    // check if the value is in the cache
    cacheService.get(createCacheKey(pair.from.toString, pair.to.toString)) match {
      // if it is, return it
      case Some(cachedValue) =>
        logger.trace("Getting value from the cache")
        cachedValue.asRight[Error].pure[F]
      case None =>
        logger.trace("Value not found in the cache")
        // if it is not, retrieve it from the service and put each rate into the cache
        oneFrameLookup.findNewRate.get.map(rate => {
          val freshRate = Rate(Pair(Currency.fromString(rate.from), Currency.fromString(rate.to)), Price(rate.price), Timestamp(OffsetDateTime.parse(rate.time_stamp)))
          cacheService.put(createCacheKey(rate.from, rate.to), freshRate)
        })
        // get the originally requested rate from the cache
        val freshRate = cacheService.get(createCacheKey(pair.from.toString, pair.to.toString)) match {
          case Some(rate) => rate
          case None => throw new Exception()
        }
        freshRate.asRight[Error].pure[F]
    }
  }
}
