package com.notik.sprastic

import akka.actor.{Props, ActorRef, Actor}
import akka.io.IO
import spray.can.Http
import spray.client.pipelining.sendTo
import spray.http._
import com.typesafe.config.{ConfigFactory, Config}
import com.notik.sprastic.api._
import spray.http.HttpRequest
import com.notik.sprastic.api.MultiGet
import com.notik.sprastic.ElasticSearchActor.Response
import com.notik.sprastic.api.Index
import com.notik.sprastic.api.Update
import com.notik.sprastic.api.Get
import com.notik.sprastic.api.Delete
import com.notik.sprastic.api.Docs
import spray.http.HttpResponse
import com.notik.sprastic.config.SprasticConfig

class ElasticSearchActor(config: Config) extends Actor {

  import context._

  val io = IO(Http)(system)

  override def receive: Receive = {
    case Response(httpResponse, target) =>
      target ! httpResponse
    case msg =>
      actorOf(Worker.props(io, sender, config.getString("host"), config.getInt("port"))) ! msg
  }
}

object ElasticSearchActor {
  def props(config: Config = SprasticConfig.defaultConfig): Props = Props(new ElasticSearchActor(config))
  case class Response(httpResponse: HttpResponse, target: ActorRef)
}

private[this] class Worker(io: ActorRef, target: ActorRef, host: String, port: Int) extends Actor {

  import context._

  import org.json4s._
  import org.json4s.jackson.Serialization.write
  import org.json4s.jackson.Serialization

  implicit val formats = Serialization.formats(NoTypeHints)

  override def receive: Actor.Receive = {
    case Index(index, t, data, id, opType) =>
      val baseUri = s"http://$host:$port/$index/$t"
      val effectiveUri = id match {
        case Some(i) => s"$baseUri/$i${opType.map(op => s"?op_type=${op.value}").getOrElse("")}"
        case None => s"$baseUri${opType.map(op => s"?op_type=${op.value}").getOrElse("")}"
      }
      val request = HttpRequest(method = id.fold(HttpMethods.POST)(_ => HttpMethods.PUT),
        uri = effectiveUri,
        entity = HttpEntity(data))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)

    case Update(index, t, data, id, version) =>
      val request = HttpRequest(method = HttpMethods.PUT,
        uri = Uri(s"http://$host:$port/$index/$t/$id${version.fold("")(v => s"?version=$v")}"),
        entity = HttpEntity(data))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)

    case Get(index, t, id) =>
      val request = HttpRequest(method = HttpMethods.GET,
        uri = Uri(s"http://$host:$port/$index/$t/$id"))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)

    case Delete(index, t, id) =>
      val request = HttpRequest(method = HttpMethods.DELETE,
        uri = Uri(s"http://$host:$port/$index/$t/$id"))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)

    case MultiGet(docs) =>
      val request = HttpRequest(method = HttpMethods.GET,
        uri = Uri(s"http://$host:$port/_mget"), entity = HttpEntity(write(Docs(docs))))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)

    case Bulk(ops) =>
      val json = ops.map(_.bulkJson).mkString("\n")
      val request = HttpRequest(method = HttpMethods.POST, uri = Uri(s"http://$host:$port/_bulk"), entity = HttpEntity(json))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)
  }

  def responseReceive: Receive = {
    case response: HttpResponse =>
      parent ! Response(response, target)
  }
}

object Worker {
  def props(io: ActorRef, target: ActorRef, host: String, port: Int): Props = Props(new Worker(io, target, host, port))
}
