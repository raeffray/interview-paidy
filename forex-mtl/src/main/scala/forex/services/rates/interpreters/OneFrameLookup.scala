package forex.services.rates.interpreters

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import forex.config.RateServiceConfig
import forex.services.rates.errors.RateLookupConnectionException
import org.slf4j.LoggerFactory
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 *
 * OneFrameLookup is a service that connects to OneFrame and retrieves the latest rates for all pairs
 *
 * @author Renato Barbosa
 * @param rateServiceConfig the configuration for the service
 * @see forex.config.RateServiceConfig
 * @param allPairs the list of all pairs to be retrieved @see forex.domain.Rate.Pair
 *                 this information is an arrangement of all currencies defined in
 *                 src/main/scala/forex/domain/Currency.scala
 *
 * */
class OneFrameLookup(rateServiceConfig: RateServiceConfig, allPairs: List[String]) {

  private val logger = LoggerFactory.getLogger("forex.services.rates.interpreters.OneFrame");

  private def createQueryParameter(): String = "?pair=" + allPairs.mkString("&pair=")

  private object OneFrameResponseProtocolFormatter extends DefaultJsonProtocol {
    implicit val oneFrameResponseFormat = jsonFormat6(OneFrameResponse)
  }

  private def buildRequest(uri: String): HttpRequest = HttpRequest(
    method = HttpMethods.GET,
    uri = rateServiceConfig.lookupService.endpoint + rateServiceConfig.lookupService.path + uri,
  ).withHeaders(
    RawHeader(
      rateServiceConfig.lookupService.authenticationTokenKeyword,
      rateServiceConfig.lookupService.authenticationTokenValue)
  )

  private def send(uri: String): Future[JsValue] = {
    implicit val system = ActorSystem()
    import system.dispatcher
    val futureResponse: Future[HttpResponse] = Http().singleRequest(buildRequest(uri))
    val futureEntity: Future[HttpEntity.Strict] = futureResponse
      .flatMap(response => response.entity.toStrict(rateServiceConfig.lookupService.serviceTimeout.seconds))
    futureEntity.map(entity => entity.data.utf8String.parseJson)
  }

  def findNewRate: Try[Vector[OneFrameResponse]] = {
    import OneFrameResponseProtocolFormatter._
    val uri: String = createQueryParameter()

    for {
      jsonData <- Try(Await.result(send(uri), rateServiceConfig.lookupService.serviceTimeout seconds))
        .orElse({
          Failure(RateLookupConnectionException("Unexpected response from the service"))
        })
      response <- jsonData match {
        case o: JsArray =>
          logger.info("Received response from OneFrame: " + o.elements.size)
          Success(o.elements.map(e => e.convertTo[OneFrameResponse]))
        case o: JsObject =>
          logger.error("Invalid response from  OneFrame: " + o.toString())
          Failure(RateLookupConnectionException("Unexpected response from the service: " + o.toString()))
        case e =>
          logger.error("Error connecting to OneFrame", e)
          Failure(new Exception("Unexpected response from the service"))
      }
    } yield response
  }
}
