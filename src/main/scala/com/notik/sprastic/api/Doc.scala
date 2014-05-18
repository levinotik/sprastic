package com.notik.sprastic.api

case class Docs(docs: Seq[Doc])
case class Doc(_index: String, _type: Option[String], _id: String)
