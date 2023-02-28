package forex.services.cache

import com.github.benmanes.caffeine.cache.Caffeine
import forex.config.CacheConfig
import scalacache.Entry
import scalacache.caffeine.CaffeineCache
import scalacache.modes.try_.mode

import java.util.concurrent.TimeUnit
import scala.util.Try

trait CacheService[T] {
  def get(key: String): Option[T]

  def put(key: String, cacheable: T): Try[Any]
}

/**
 *
 * Cache service of type T, implementing underlying Caffeine cache to memoize rates for a pre-configured period of time (TTL)
 *
 * @author Renato Barbosa
 * @constructor creates a new instance with a set of configuration
 * @param config the cache config  (see @CacheConfig)
 *
 * */
class CacheServiceCaffeine[T](config: CacheConfig) extends CacheService[T] {
  object CacheMemoizationConfigCacheMemoizationConfig {
    private val cacheEngine = Caffeine
      .newBuilder()
      .maximumSize(config.maxSize)
      .expireAfterWrite(config.ttl, TimeUnit.SECONDS)
      .build[String, Entry[T]]
    implicit val cache: CaffeineCache[T] = CaffeineCache(cacheEngine)
  }

  def get(key: String): Option[T] = {
    import CacheMemoizationConfigCacheMemoizationConfig._
    cache.get(key).getOrElse(None)
  }

  def put(key: String, cacheable: T): Try[Any] = {
    import CacheMemoizationConfigCacheMemoizationConfig._
    cache.put(key)(cacheable)
  }

}

