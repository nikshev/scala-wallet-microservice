package controllers

import java.util.UUID.randomUUID
import javax.inject._

import models.Wallet
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._

import scala.concurrent.{ExecutionContext, Future}


/**
  * Simple controller that directly stores and retrieves [models.Wallet] instances into a MongoDB Collection
  */
@Singleton
class HealthCheckController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  /**
    * Health check return UUID always different
    * @return
    */
  def healthCheck =  Action {
    val id = randomUUID().toString
    Ok(Json.toJson(id))
  }

 }


