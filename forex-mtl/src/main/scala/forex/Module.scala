package forex

import cats.effect.{Concurrent, Timer}
import forex.config.ApplicationConfig
import forex.domain.{Currency, Rate}
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import forex.services.cache.{CacheService, CacheServiceCaffeine}
import forex.services.rates.interpreters.OneFrameLookup
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig) {

  // get all currencies from Currency
  private val allCurrencies = scala.reflect.runtime.currentMirror
    .classSymbol(Currency.getClass)
    .info
    .members
    .filter(_.isModule)
    .map(_.name.toString)
    .toList

  // arrange the currencies in pairs
  val allPairs = allCurrencies.flatMap(x => allCurrencies.map(y => if (x != y) s"$x$y" else null)).filter(p => p != null)

  private val cacheService: CacheService[Rate] = new CacheServiceCaffeine[Rate](config.rateService.cache);

  private val oneFrameLookup = new OneFrameLookup(config.rateService, allPairs)

  private val ratesService: RatesService[F] = RatesServices.rateLookup[F](cacheService, oneFrameLookup)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
