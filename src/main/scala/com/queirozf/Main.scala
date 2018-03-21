package com.queirozf

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import kamon.Kamon
import kamon.prometheus.PrometheusReporter
import akka.util
/**
 * Created by felipe.almeida@vtex.com.br on 29/06/16.
 */
object Main extends App {

  implicit val system = ActorSystem("main-actor-system")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))
  implicit val ec = system.dispatcher

  Kamon.addReporter(new PrometheusReporter())

  val counter = Kamon.counter("number_of_calls_to_endpoints")
  val healthCheckCounter = counter.refine("endpoint", "healthcheck")
  val walrusCounter = counter.refine("endpoint", "walrus")

  val latency = Kamon.timer("latency_for_each_endpoint")
  val healthCheckLatency = latency.refine("endpoint", "healthcheck")
  val walrusLatency = latency.refine("endpoint", "walrus")

  val withCountingMetric =
    extractUri.flatMap { uri =>
      println(s"The path you just called was... drumroll please!!! ${uri.path}")

      uri.path match {
        case Path("/healthcheck") =>
          println("Incremented healthcheck counter")
          healthCheckCounter.increment()
        case Path("/walrus") =>
          println("Incremented walrus counter")
          walrusCounter.increment()
        case _ => Unit
      }
      pass
    }

  def withLatencyMetric(route: Route): Route = { ctx =>
    println(s"Calculating latency for ${ctx.request.uri.path}")

    val timerOpt = ctx.request.uri.path match {
      case Path("/healthcheck") => Some(healthCheckLatency.start())
      case Path("/walrus") => Some(walrusLatency.start())
      case _ => None
    }

    val response = route(ctx)
    timerOpt.foreach(_.stop())
    response
  }

  val healthCheckRoute = {
    path("healthcheck") {
      get {
        Thread.sleep(200)
        complete("Ok")
      }
    }
  }

  val walrusRoute = {
    path("walrus") {
      get {
        Thread.sleep(300)
        complete("Walrus")
      }
    }
  }

  val allRoutes =
    withCountingMetric {
      withLatencyMetric {
        healthCheckRoute ~ walrusRoute
      }
    }

  Http().bindAndHandle(allRoutes, "0.0.0.0", 5000)
}
