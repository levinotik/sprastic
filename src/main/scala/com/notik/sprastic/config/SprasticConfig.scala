package com.notik.sprastic.config

import com.typesafe.config.ConfigFactory

object SprasticConfig {
  private val config = ConfigFactory.load().getConfig("sprastic")
  val host = config.getString("host")
  val port = config.getInt("port")
}
