package forex.services.rates

import cats.Applicative
import forex.domain.Rate
import forex.services.cache.CacheService
import forex.services.rates.interpreters.{CacheableRateService, OneFrameLookup}

object Interpreters {
  def rateLookup[F[_] : Applicative](cacheService: CacheService[Rate], oneFrameLookup: OneFrameLookup): Algebra[F] =
    new CacheableRateService[F](cacheService, oneFrameLookup)

}
