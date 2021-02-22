package sttp.tapir.client.tests

import cats.effect.IO
import sttp.capabilities.{Streams, WebSockets}
import sttp.tapir._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import sttp.tapir.generic.auto._
import sttp.tapir.tests.Fruit
import sttp.ws.WebSocketFrame

trait ClientWebSocketTests[S] { this: ClientTests[S with WebSockets] =>
  val streams: Streams[S]

  def sendAndReceiveLimited[A, B](p: streams.Pipe[A, B], receiveCount: Int, as: List[A]): IO[List[B]]

  def webSocketTests(): Unit = {
    test("web sockets, string client-terminated echo") {
      send(
        endpoint.get.in("ws" / "echo").out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain].apply(streams)),
        port,
        (),
        "ws"
      )
        .flatMap { r =>
          sendAndReceiveLimited(r.toOption.get, 2, List("test1", "test2"))
        }
        .unsafeRunSync() shouldBe List("echo: test1", "echo: test2")
    }

    test("web sockets, json client-terminated echo") {
      send(
        endpoint.get.in("ws" / "echo").out(webSocketBody[Fruit, CodecFormat.Json, Fruit, CodecFormat.Json].apply(streams)),
        port,
        (),
        "ws"
      )
        .flatMap { r =>
          sendAndReceiveLimited(r.toOption.get, 2, List(Fruit("apple"), Fruit("orange")))
        }
        .unsafeRunSync() shouldBe List(Fruit("echo: apple"), Fruit("echo: orange"))
    }

    test("web sockets, client-terminated echo fragmented frame") {
      send(
        endpoint.get.in("ws" / "partial").out(webSocketBody[String, CodecFormat.TextPlain, WebSocketFrame, CodecFormat.TextPlain].apply(streams)),
        port,
        (),
        "ws"
      )
        .flatMap { r =>
          sendAndReceiveLimited(r.toOption.get, 2, List("test"))
        }
        .unsafeRunSync() shouldBe List(WebSocketFrame.Text("fragmented echo: test", true, None))
    }

    // TODO: tests for ping/pong (control frames handling)
  }
}
