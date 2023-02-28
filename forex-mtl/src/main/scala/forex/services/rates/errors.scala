package forex.services.rates

object errors {

  sealed trait Error
  object Error {
    final case class OneFrameLookupFailed(msg: String) extends Error
  }

  case class RateLookupConnectionException(message: String) extends Exception(message)

}
