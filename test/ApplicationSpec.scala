import org.scalatestplus.play._
import play.api.libs.json.{JsSuccess, Json}
import play.api.test._
import play.api.test.Helpers._

/**
  * Application test specification
  */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {
    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HelthCheckController" should {
     "answer random string" in {
       val healthCheck = route(app, FakeRequest(GET, "/healthcheck")).get
       status(healthCheck) mustBe OK
       contentType(healthCheck) mustBe Some("application/json")
     }
  }

  "WalletController"  should {
     "Register wallet" in {
       val register = route(app, FakeRequest(GET, "/wallet/register")).get
       status(register) mustBe CREATED
       contentType(register) mustBe Some("application/json")
       val jsonObject = contentAsJson(register)
       val id = (jsonObject \ "id").as[String]
       val balance = (jsonObject \ "balance").as[Double]
       balance mustBe (0)
     }

     "Deposit money to wallet" in {
       val register = route(app, FakeRequest(GET, "/wallet/register")).get
       status(register) mustBe CREATED
       contentType(register) mustBe Some("application/json")
       val jsonObject = contentAsJson(register)
       val id = (jsonObject \ "id").as[String]
       val deposit = route(app, FakeRequest(POST, "/wallet/deposit").withJsonBody(Json.obj("id" -> id, "balance"->100.0))).get
       status(deposit) mustBe ACCEPTED
       contentAsString(deposit) must include ("currentBalance")
       contentAsString(deposit) must include ("100")
     }

    "Withdraw money from wallet" in {
      val register = route(app, FakeRequest(GET, "/wallet/register")).get
      status(register) mustBe CREATED
      contentType(register) mustBe Some("application/json")
      val jsonObject = contentAsJson(register)
      val id = (jsonObject \ "id").as[String]
      val deposit = route(app, FakeRequest(POST, "/wallet/deposit").withJsonBody(Json.obj("id" -> id, "balance"->100.0))).get
      status(deposit) mustBe ACCEPTED
      contentAsString(deposit) must include ("currentBalance")
      contentAsString(deposit) must include ("100")
      val withdraw = route(app, FakeRequest(POST, "/wallet/withdraw").withJsonBody(Json.obj("id" -> id, "balance"->70.0))).get
      status(withdraw) mustBe ACCEPTED
      contentAsString(withdraw) must include ("currentBalance")
      contentAsString(withdraw) must include ("30")
    }

    "Wallet ballance" in {
      val register = route(app, FakeRequest(GET, "/wallet/register")).get
      status(register) mustBe CREATED
      contentType(register) mustBe Some("application/json")
      val jsonObject = contentAsJson(register)
      val id = (jsonObject \ "id").as[String]
      val balance = route(app, FakeRequest(GET, "/wallet/balance?id="+id)).get
      contentAsString(balance) must include ("\"balance\":0")
    }
  }

}