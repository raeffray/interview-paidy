package forex.http
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import forex.services.rates.errors.RateLookupConnectionException
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import scala.util.{Failure, Success, Try}

class RatesHttpRoutes[F[_] : Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, Protocol._, QueryParams._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(candidateFrom) +& ToQueryParam(candidateTo) =>
      Try({
        val validFrom = candidateFrom match {
          case Some(f) => f.fold(_ => throw new IllegalArgumentException("Currency target is invalid"), currency => currency)
          case None => throw new IllegalArgumentException("Currency target is missing")
        }
        val validTo = candidateTo match {
          case Some(f) => f.fold(_ => throw new IllegalArgumentException("Currency target is invalid"), currency => currency)
          case None => throw new IllegalArgumentException("Currency target is missing")
        }
        rates.get(RatesProgramProtocol.GetRatesRequest(validFrom, validTo)).flatMap(Sync[F].fromEither)
      }) match {
        case Success(response) => response.flatMap(rate => Ok(rate.asGetApiResponse))
        case Failure(exception) => exception match {
          case e: IllegalArgumentException => BadRequest(e.getMessage())
          case _: RateLookupConnectionException => BadGateway("rate lookup service is unavailable")
          case e: Exception => InternalServerError(e.getMessage())
        }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
