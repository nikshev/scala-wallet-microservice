package models

import play.api.libs.json.Json

/**
  * Wallet class
  * @param id - UUID string
  * @param balance - balance
  */
case class Wallet(id: String, balance: Double)

object Wallet {
  implicit val formatter = Json.format[Wallet]
}
