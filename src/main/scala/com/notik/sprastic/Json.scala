package com.notik.sprastic

import com.notik.sprastic.api.{Docs, Doc}

object Json {
  import org.json4s._
  import org.json4s.jackson.Serialization.write
  import org.json4s.jackson.Serialization
  implicit val formats = Serialization.formats(NoTypeHints)

  def multiGetDocsArray(docs: Seq[Doc]) = {
    write(Docs(docs))
  }
}
