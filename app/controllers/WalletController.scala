package controllers

import javax.inject._

import models.Wallet
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import java.util.UUID.randomUUID

import reactivemongo.api.ReadPreference
import reactivemongo.play.json.{collection, _}
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
    *
    * @return
    */
  def registerWallet = Action.async {
    val id = randomUUID().toString
    val wallet = Wallet(id, 0.0)
    for {
      wallets <- walletsFuture
      lastError <- wallets.insert(wallet)
    } yield
      Created(Json.toJson(wallet))
  }


  /**
    * Deposit money to wallet
    *
    * @return
    *
    * TO-DO for real life
    * Deposit only after authentication
    * Check balance in json (greater than zero and etc)
    */
  def depositMoney = Action.async(parse.json) { request =>
    Json.fromJson[Wallet](request.body) match {
      case JsSuccess(wallet, _) =>
        val walletsList = findWalletById(wallet.id)
        walletsList.map { wallets =>
          val walletFromBase = wallets.head
          val currentBalance = walletFromBase.balance + wallet.balance
          updateBalanceById(wallet.id, currentBalance)
          Accepted(Json.obj("currentBalance" -> currentBalance))
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Can't create operation from the json provided. " + Errors.show(errors)))
    }
  }


  /**
    * Withdraw money from wallet
    *
    * @return
    *
    * TO-DO for real life
    * Deposit only after authentication
    * Check balance in json (greater than zero and etc)
    */
  def withdrawMoney = Action.async(parse.json) { request =>
    Json.fromJson[Wallet](request.body) match {
      case JsSuccess(wallet, _) =>
        val walletsList = findWalletById(wallet.id)
        walletsList.map { wallets =>
          val walletFromBase = wallets.head
          if ((walletFromBase.balance - wallet.balance) > 0) {
            val currentBalance = walletFromBase.balance - wallet.balance
            updateBalanceById(wallet.id, currentBalance)
            Accepted(Json.obj("currentBalance" -> currentBalance))
          } else
            BadRequest(Json.obj("error" -> "Can't withdraw this sum!!!"))
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Can't create operation from the json provided. " + Errors.show(errors)))
    }
  }


  /**
    * Find wallet by id
    *
    * @param id - string UUID
    * @return
    */
  def findWalletById(id: String): Future[List[Wallet]] = {
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
    *
    * @param id - string UUID
    * @return
    */
  def getBalanceById(id: String) = Action.async {
    val walletsList =findWalletById(id)
    // everything's ok! Let's reply with a JsValue
    walletsList.map { wallets =>
      Ok(Json.toJson(wallets))
    }
  }

  /**
    * Update balance by id
    * @param id - wallet id
    * @param currentBalance - balance for update
    */
  def updateBalanceById(id: String, currentBalance: Double): Unit = {
    val modifier = Json.obj(
      "$set" -> Json.obj(
        "balance" -> JsNumber(currentBalance)
      )
    )
    for {
      collection <- walletsFuture
      lastError <- collection.update(Json.obj("id" -> id), modifier)
    } yield {
      Logger.info(s"Successfully inserted with LastError: $lastError")
    }
  }


}


