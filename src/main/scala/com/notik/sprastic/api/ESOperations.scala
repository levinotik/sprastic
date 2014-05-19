package com.notik.sprastic.api

import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

sealed trait ESOperation

sealed trait OpType {
  def value: String
}
case object Create extends OpType {
  override def value: String = "create"
}

case class Index(index: String,
               `type`: String,
                 document: String,
                 id: Option[String] = None,
                 opType: Option[OpType] = None)
  extends ESOperation with BulkSupport{
  override def bulkJson: String = {
    val action = opType match {
      case Some(Create) => "create"
      case _ => "index"
    }
   val actionAndMetadata = compact(render(action -> ("_index" -> index) ~ ("_type" -> `type`) ~ ("_id" -> id)))
   s"""
      |$actionAndMetadata
      |$document
    """.stripMargin 
  }
}

case class Update(index: String,
                  `type`: String,
                  document: String,
                  id: String,
                  version: Option[Int] = None)
  extends ESOperation with BulkSupport{
  override def bulkJson: String = {
    val actionAndMetadata = compact(render("update" -> ("_index" -> index) ~ ("_type" -> `type`) ~ ("_id" -> id)))
    val doc = s""" {"doc": $document} """
    s"""
      |$actionAndMetadata
      |$doc
    """.stripMargin
  }
}

case class Delete(index: String, `type`: String, id: String) extends ESOperation with BulkSupport{
  override def bulkJson: String = {
    val actionAndMetadata = compact(render("delete" -> ("_index" -> index) ~ ("_type" -> `type`) ~ ("_id" -> id)))
    s"""
      |$actionAndMetadata
    """.stripMargin
  }
}

case class MultiGet(docs: Seq[Doc]) extends ESOperation

case class Get(index: String, `type`: String, id: String) extends ESOperation

sealed trait BulkSupport {
  def bulkJson: String
}

case class Bulk(actions: Seq[BulkSupport])
