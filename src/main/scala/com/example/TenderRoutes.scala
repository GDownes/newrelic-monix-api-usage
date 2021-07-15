package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.time.Duration
import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import DefaultJsonProtocol._
import spray.json._
import sttp.client3._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import cats.effect.{Blocker, ContextShift, IO}
import org.http4s.client.blaze.BlazeClientBuilder
import sttp.client3.akkahttp.AkkaHttpBackend
import sttp.client3.http4s.Http4sBackend
import sttp.client3.{UriContext, basicRequest}

final case class Tender(title: String, deadline: String, value: Int, published: String, procedure: String, cycle: Int, status: String, descriptionTruncated: String, id: Int, ca: String)

final case class Tenders(tenders: immutable.Seq[Tender])

class TenderRoutes()(implicit val system: ActorSystem[_]) {

  implicit val tenderJsonFormat: RootJsonFormat[Tender] = jsonFormat10(Tender)
  implicit val tendersJsonFormat: RootJsonFormat[Tenders] = jsonFormat1(Tenders)
  implicit val timeout: Timeout = Timeout.create(Duration.ofSeconds(5))

  def getTenders: Tenders = {
    def parseJson(json: String) = json.parseJson.convertTo[List[Tender]]

    Tenders(basicRequest.get(uri"https://api.tender-ni.com/tender").response(asStringAlways.map(parseJson)).send(HttpURLConnectionBackend()).body)
  }

  def getTendersAkka: Future[Tenders] = {
    def parseJson(json: String) = json.parseJson.convertTo[List[Tender]]

    basicRequest.get(uri"https://api.tender-ni.com/tender").response(asStringAlways.map(parseJson)).send(AkkaHttpBackend()).map(x => Tenders(x.body))
  }

  def getTendersHttp4s: Tenders = {
    def parseJson(json: String) = json.parseJson.convertTo[List[Tender]]

    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    Blocker[IO].use(blocker => {
      BlazeClientBuilder[IO](blocker.blockingContext).resource.use(client => {
        basicRequest.get(uri"https://api.tender-ni.com/tender").response(asStringAlways.map(parseJson)).send(Http4sBackend.usingClient(client, blocker)).map(x => Tenders(x.body))
      })
    }).unsafeRunSync()
  }

  val tenderRoutes: Route =
    pathPrefix("tenders") {
      concat(
        pathPrefix("sync") {
          pathEnd {
            get {
              complete(getTenders)
            }
          }
        },
        pathPrefix("akka") {
          pathEnd {
            get {
              complete(getTendersAkka)
            }
          }
        },
        pathPrefix("http4s") {
          pathEnd {
            get {
              complete(getTendersHttp4s)
            }
          }
        }
      )
    }
}
