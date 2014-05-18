#Sprastic - lightweight elasticsearch client built on Akka and Spray

##Getting started

A client can be used inside or outside of the context of an ActorSystem. 

**Within your ActorSystem** (if you're using Akka in your project, you'll want to use this) :

```scala
val client = SprasticClient(context) // <-- this is just an ActorRef 
client ! Get("twitter", "tweet", "1")
```

**Outside of an ActorSystem** (if you're not using Akka in your project, you'll have to use this):

```scala
val client = SprasticClient() // <-- instance of a SprasticClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
implicit val timeout:FiniteDuration = 10.seconds

client.execute(Get("twitter", "tweet", "1")) onComplete {
  case Success(response) => println(response)
  case Failure(failure) => println(failure)
}

```

by default this will use the host and port specified in your config in "sprastic", e.g.

		sprastic {
			host = "localhost"
			port = 9200
		}

If you need to use a different config or have several, you can simply create a new Config and use that. For example:


		sprastic-production {
 			host = "production-host"
 			port = production-port
		}
		
`val client = SprasticClient(context, ConfigFactory.load().getConfig("sprastic-production"))`

		sprastic-stage {
 			host = "stage-host"
 			port = stage-port
		}
		
`val client = SprasticClient(context, ConfigFactory.load().getConfig("stage-production"))`

**The following examples assume you're using Sprastic within your own ActorSystem. If you're not, refer to the above example which demonstrates usage outside of an ActorSystem.**


##Add

To add a document with an id:

`client ! Add("twitter", "tweet", s"""{"text": "chirp"}""", Some("1"))`

To add without an id (relying on elasticsearch to auto-generate one):

`client ! Add("twitter", "tweet", s"""{"text": "chirp"}""")`

in both cases, you'll get back a spray.http.HttpResponse:

`case response: HttpResponse => println(response)`

		HttpResponse(200 OK,HttpEntity(application/json; charset=UTF-8,{"_index":"twitter","_type":"tweet","_id":"1","_version":2,"created":false}),List(Content-Length: 75, Content-Type: application/json; charset=UTF-8),HTTP/1.1)
		
		
##Get

```scala   
      val client = SprasticClient(context)
      client ! Get("twitter", "tweet", "1")
      //... in your Receive function
    case response: HttpResponse =>
      println(response.entity.asString)
      //prints: {"_index":"twitter","_type":"tweet","_id":"1","_version":2,"found":true, "_source" : {"text": "chirp"} }
  }
```







