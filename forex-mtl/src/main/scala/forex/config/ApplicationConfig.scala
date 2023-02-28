package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    rateService: RateServiceConfig
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

/***
 *
 * Represents the configuration for the RateService
 *
 * @param lookupService the configuration for the LookupService
 * @param cache the configuration for the Cache
 *
 */
case class RateServiceConfig (
    lookupService: LookupServiceConfig,
    cache: CacheConfig
)

/**
 * Represents the configuration for the LookupService, that
 * connects to the external service to retrieve the rates
 *
 * @param endpoint the endpoint of the service
 * @param path uri of the service
 * @param authenticationTokenKeyword the keyword for the authentication token
 * @param authenticationTokenValue the value for the authentication token
 * @param serviceTimeout the timeout for the service call
 */
case class LookupServiceConfig (
  endpoint: String,
  path: String,
  authenticationTokenKeyword: String,
  authenticationTokenValue: String,
  serviceTimeout: Int,
)

/**
 * Represents the configuration for the Cache
 *
 * @param ttl the time to live for the cache
 * @param maxSize the maximum size of the cache
 *
 */
case class CacheConfig (
  ttl: Long,
  maxSize: Long
)