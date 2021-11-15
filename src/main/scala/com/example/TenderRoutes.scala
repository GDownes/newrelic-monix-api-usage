package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.time.Duration
import scala.collection.immutable
import DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.newrelic.monix.api.TraceOps.{asyncTrace, txn}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

final case class Tender(title: String, id: Int)
final case class Tenders(tenders: immutable.Seq[Tender])
final case class EnrichedTender(description: String, title: String, id: Int)
final case class EnrichedTenders(enrichedTenders: immutable.Seq[EnrichedTender])

class TenderRoutes()(implicit val system: ActorSystem[_]) {

  implicit val tenderJsonFormat: RootJsonFormat[Tender] = jsonFormat2(Tender)
  implicit val tendersJsonFormat: RootJsonFormat[Tenders] = jsonFormat1(Tenders)
  implicit val enrichedTenderJsonFormat: RootJsonFormat[EnrichedTender] = jsonFormat3(EnrichedTender)
  implicit val enrichedTendersJsonFormat: RootJsonFormat[EnrichedTenders] = jsonFormat1(EnrichedTenders)
  implicit val timeout: Timeout = Timeout.create(Duration.ofSeconds(5))

  def getEnricherTenders: EnrichedTenders = {
    val enrichedTenders =
      for {
        tenders <- getTenders
        enrichedTenders <- Task.sequence(tenders.tenders.map(tender => getEnrichedTender(tender)))
      } yield enrichedTenders
    EnrichedTenders(Await.result(enrichedTenders.runToFuture, 10.seconds))
  }

  def getTenders: Task[Tenders] = {
    asyncTrace("getTenders") {
      Task.eval({
        Tenders(Seq(Tender("tenderOne", 1), Tender("tenderTwo", 2)))
      })
    }
  }

  def getEnrichedTender(tender: Tender): Task[EnrichedTender] = {
    asyncTrace(s"getEnrichedTender${tender.id}") {
      Task.eval(EnrichedTender("Description", tender.title, tender.id))
    }
  }

  val tenderRoutes: Route =
    pathPrefix("tenders") {
      pathEnd {
        get {
          complete(getEnricherTenders)
        }
      }
    }
}
