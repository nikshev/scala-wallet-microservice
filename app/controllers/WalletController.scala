package controllers

import javax.inject._

import models.Wallet
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import java.util.UUID.randomUUID

import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}


/**
  * Simple controller that directly stores and retrieves [models.Wallet] instances into a MongoDB Collection
  */
@Singleton
class WalletController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  def walletsFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("wallets"))

  /**
    * Register new wallet
    * @return
    */
  def registerWallet = Action.async {
    val id = randomUUID().toString
    val wallet = Wallet(id,0.0)
    for {
      wallets <- walletsFuture
      lastError <- wallets.insert(wallet)
    } yield
      Ok(Json.toJson(wallet))
  }


  /**
    * Find wallet by id
    * @param id - string UUID
    * @return
    */
  def findById(id: String): Future[List[Wallet]] = {
    // let's do our query
    val futureWalletsList: Future[List[Wallet]] = walletsFuture.flatMap {
      // find wallet with name `id`
      _.find(Json.obj("id" -> id)).
      // perform the query and get a cursor of JsObject
      cursor[Wallet](ReadPreference.primary).
      // Coollect the results as a list
      collect[List]()
    }

    futureWalletsList
  }

  /**
    * Get balance objects by id
    * @param id - string UUID
    * @return
    */
  def balanceById(id: String) = Action.async {
    val walletsList = findById(id)
    // everything's ok! Let's reply with a JsValue
    walletsList.map { wallets =>
      Ok(Json.toJson(wallets))
    }
  }


}


