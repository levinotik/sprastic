package com.notik.sprastic.client

import akka.actor.{ActorSystem, ActorRefFactory, ActorRef}
import com.notik.sprastic.ElasticSearchActor
import com.typesafe.config.{ConfigFactory, Config}
import com.notik.sprastic.api.ESOperation
import scala.concurrent.Future
import spray.http.HttpResponse
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import com.notik.sprastic.config.SprasticConfig

class SprasticClient(config: Config = SprasticConfig.defaultConfig) {
  import akka.pattern.ask
  val system: ActorSystem = ActorSystem("sprastic-actor-system")
  def execute(operation: ESOperation)(implicit timeout: FiniteDuration): Future[HttpResponse] =
    system.actorOf(ElasticSearchActor.props(config)).ask(operation)(Timeout(timeout)).mapTo[HttpResponse]
  def shutdown() = system.shutdown()
}

object SprasticClient {

  def apply(actorRefFactory: ActorRefFactory): ActorRef =
    actorRefFactory.actorOf(ElasticSearchActor.props())

  def apply(actorRefFactory: ActorRefFactory, config: Config): ActorRef =
    actorRefFactory.actorOf(ElasticSearchActor.props(config))

  def apply(config: Config): SprasticClient =  new SprasticClient(config)

  def apply(): SprasticClient = new SprasticClient
}
