package com.notik.sprastic.api

/**
 *
 * @param docs list of docs specifying the index, type (optional) and id for docs to fetch
 */
case class MultiGet(docs: Seq[Doc])

