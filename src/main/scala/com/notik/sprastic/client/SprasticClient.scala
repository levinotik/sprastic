package com.notik.sprastic.client

import akka.actor.{ActorSystem, ActorRefFactory, ActorRef}
import com.notik.sprastic.ElasticSearchActor
import com.typesafe.config.{ConfigFactory, Config}
import com.notik.sprastic.api.ESOperation
import scala.concurrent.Future
import spray.http.HttpResponse
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration

class SprasticClient(config: Config = ConfigFactory.load().getConfig("sprastic")) {
  import akka.pattern.ask
  val system: ActorSystem = ActorSystem("sprastic-actor-system")
  def execute(operation: ESOperation)(implicit timeout: FiniteDuration): Future[HttpResponse] =
    system.actorOf(ElasticSearchActor.props(config)).ask(operation)(Timeout(timeout)).mapTo[HttpResponse]
  def shutdown() = system.shutdown()
}

object SprasticClient {

  def apply(actorRefFactory: ActorRefFactory): ActorRef =
    actorRefFactory.actorOf(ElasticSearchActor.props(ConfigFactory.load().getConfig("sprastic")))

  def apply(actorRefFactory: ActorRefFactory, config: Config): ActorRef =
    actorRefFactory.actorOf(ElasticSearchActor.props(config))

  def apply(config: Config): SprasticClient =  new SprasticClient(config)

  def apply(): SprasticClient = new SprasticClient
}
