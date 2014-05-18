package com.notik.sprastic

import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.ActorDSL._
import com.notik.sprastic.api.Get
import com.notik.sprastic.client.SprasticClient
import spray.http.HttpResponse

object SprasticApp extends App {
  implicit val system = ActorSystem("sprastic-system")

  system.actorOf(Props(new Tester)) ! "go"

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
