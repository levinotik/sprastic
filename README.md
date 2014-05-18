#Sprastic - lightweight elasticsearch client built on Akka and Spray

##Getting started

`val client = SprasticClient(context)`

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

In Sprastic, all you do is send messages (Add, Get, Delete, MultiGet, etc) to the client and get back HttpResponses. That's it.

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
  }
```

		{"_index":"twitter","_type":"tweet","_id":"1","_version":2,"found":true, "_source" : {"text": "chirp"} }





