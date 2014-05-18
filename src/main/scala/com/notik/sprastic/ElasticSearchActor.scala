package com.notik.sprastic

import akka.actor.{Props, ActorRef, Actor}
import akka.io.IO
import spray.can.Http
import spray.client.pipelining.sendTo
import spray.http._
import com.notik.sprastic.api._
import spray.http.HttpRequest
import spray.http.HttpResponse
import com.notik.sprastic.api.Add
import com.notik.sprastic.ElasticSearchActor.Response
import com.notik.sprastic.api.Update
import com.notik.sprastic.api.MultiGet
import com.typesafe.config.{ConfigFactory, Config}

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
  def props(config: Config = ConfigFactory.load().getConfig("sprastic")):Props = Props(new ElasticSearchActor(config))
  case class Response(httpResponse: HttpResponse, target: ActorRef)
}

private[this] class Worker(io: ActorRef, target: ActorRef, host: String, port: Int) extends Actor {
  import context._

  import org.json4s._
  import org.json4s.jackson.Serialization.write
  import org.json4s.jackson.Serialization
  implicit val formats = Serialization.formats(NoTypeHints)

  override def receive: Actor.Receive = {
    case i: Index =>
      i match {
        case Add(index, t, data, id) =>
          val request = HttpRequest(method = id.fold(HttpMethods.POST)(_ => HttpMethods.PUT),
            uri = Uri(s"http://$host:$port/$index/$t/${id.getOrElse("")}"),
            entity = HttpEntity(data))
          sendTo(io).withResponsesReceivedBy(self)(request)
          become(responseReceive)
        case Update(index, t, data, id, version) =>
          val request = HttpRequest(method =  HttpMethods.PUT,
            uri = Uri(s"http://$host:$port/$index/$t/$id${version.fold("")(v => s"?version=$v")}"),
            entity = HttpEntity(data))
          sendTo(io).withResponsesReceivedBy(self)(request)
          become(responseReceive)
      }

    case Get(index, t, id) =>
      val request = HttpRequest(method =  HttpMethods.GET,
        uri = Uri(s"http://$host:$port/$index/$t/$id"))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)

    case Delete(index, t, id) =>
      val request = HttpRequest(method =  HttpMethods.DELETE,
        uri = Uri(s"http://$host:$port/$index/$t/$id"))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)

    case MultiGet(docs) =>
      val request = HttpRequest(method =  HttpMethods.GET,
        uri = Uri(s"http://$host:$port/_mget"), entity = HttpEntity(write(Docs(docs))))
      sendTo(io).withResponsesReceivedBy(self)(request)
      become(responseReceive)
  }
  def responseReceive: Receive = {
    case response: HttpResponse =>
      parent ! Response(response, target)
  }
}

object Worker {
  def props(io: ActorRef, target: ActorRef, host: String, port: Int):Props = Props(new Worker(io, target, host, port))
}
