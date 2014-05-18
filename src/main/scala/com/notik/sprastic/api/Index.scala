package com.notik.sprastic.api

import com.notik.sprastic.config.SprasticConfig

sealed trait Index

/**
 *
 * @param index
 * @param `type`
 * @param document
 * @param id if specified, this id will be used and a PUT request will be used. Otherwise, one will be created
 *           automatically and a POST request will be used
 */
case class Add(index: String,
               `type`: String,
                 document: String,
                 id: Option[String] = None
                ) extends Index with ESOperation

case class Update(index: String,
                  `type`: String,
                  document: String,
                  id: String,
                  version: Option[Int] = None) extends Index with ESOperation