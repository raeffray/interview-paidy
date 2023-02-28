package forex.http.rates

import forex.domain.Currency
import org.http4s.dsl.impl.OptionalValidatingQueryParamDecoderMatcher
import org.http4s.{ParseFailure, QueryParamDecoder}

import scala.util.Try

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] = {
    QueryParamDecoder[String].emap(parameter => {
      Try(Currency.fromString(parameter))
        .toEither
        .left
        .map(e => ParseFailure(e.getMessage, e.getMessage))
    })
  }

  object FromQueryParam extends OptionalValidatingQueryParamDecoderMatcher[Currency]("from")

  object ToQueryParam extends OptionalValidatingQueryParamDecoderMatcher[Currency]("to")

}
