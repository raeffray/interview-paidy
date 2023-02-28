package forex.services.rates.interpreters

/**
 *
 * Class to represents the response from the OneFrame API
 *
 * @author Renato Barbosa
 * @param from  the currency to convert from
 *              e.g. EUR
 * @param to    the currency to convert to
 *              e.g. USD
 * @param price the price of the currency pair
 * @param bid   the bid price of the currency pair
 * @param ask   the ask price of the currency pair
 *
 *
 * */
case class OneFrameResponse(from: String, to: String, price: BigDecimal, bid: BigDecimal, ask: BigDecimal, time_stamp: String)
