package com.notik.sprastic.config

import com.typesafe.config.ConfigFactory

object SprasticConfig {
  val defaultConfig = ConfigFactory.load().getConfig("sprastic")
  val host = defaultConfig.getString("host")
  val port = defaultConfig.getInt("port")
}
