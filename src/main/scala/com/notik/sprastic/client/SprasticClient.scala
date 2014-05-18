package com.notik.sprastic.client

import akka.actor.{ActorRefFactory, ActorRef}
import com.notik.sprastic.ElasticSearchActor
import com.typesafe.config.{ConfigFactory, Config}

object SprasticClient {
  def apply(actorRefFactory: ActorRefFactory, config: Config = ConfigFactory.load().getConfig("sprastic")): ActorRef =
    actorRefFactory.actorOf(ElasticSearchActor.props(config))
}
