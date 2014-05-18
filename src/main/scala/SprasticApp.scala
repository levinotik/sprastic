package com.notik.sprastic

import akka.actor.{Actor, ActorSystem}
import com.notik.sprastic.api.Get
import com.notik.sprastic.client.SprasticClient
import spray.http.HttpResponse
import scala.util.{Failure, Success}

object SprasticApp extends App {
  implicit val system = ActorSystem("sprastic-system")

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  implicit val timeout:FiniteDuration = 10.seconds

  SprasticClient().execute(Get("twitter", "tweet", "1")) onComplete {
    case Success(response) => println(response)
    case Failure(failure) => println(failure)
  }
}

class Tester extends Actor {
  override def receive: Receive = {
    case "go" =>
      val client = SprasticClient(context)
      client ! Get("twitter", "tweet", "1")
    case response: HttpResponse =>
      println(response.entity.asString)
  }
}
