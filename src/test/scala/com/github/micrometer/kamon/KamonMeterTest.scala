package com.github.micrometer.kamon

import java.lang

import scala.collection.JavaConverters._
import scala.concurrent.duration.DurationInt

import org.scalatest.concurrent.Eventually
import org.scalatest.{FlatSpec, Matchers}

import io.micrometer.core.instrument.Tag
import kamon.Kamon

class KamonMeterTest extends FlatSpec with Matchers with Eventually {

  "KamonMeterRegistry" should "report counter data" in {
    val reporter = new KamonReporter
    val registration = Kamon.addReporter(reporter)
    try {
      val registry = new KamonMeterRegistry
      val counter = registry.counter("kamon-test-counter", Seq(Tag.of("tag", "value")).asJava)
      counter.increment(12.5)
      eventually(timeout(1.minute), interval(5.seconds)) {
        val counters = reporter.periodSnapshot.metrics.counters
        counters should not be empty
        counters.filter(_.name == "kamon-test-counter") match {
          case Seq() => fail("metric not found")
          case Seq(counter) => {
            //kamon only supports longs (so the 12.5 is rounded to 13)
            counter.value shouldEqual 13
            counter.tags shouldEqual Map("tag" -> "value")
          }
          case _ => fail("multiple metrics found with same name")
        }
      }
    } finally {
      registration.cancel()
    }
  }

  "KamonMeterRegistry" should "report gauge data" in {
    val reporter = new KamonReporter
    val registration = Kamon.addReporter(reporter)
    try {
      val registry = new KamonMeterRegistry
      registry.gauge("kamon-test-gauge", Seq(Tag.of("tag", "value")).asJava, new lang.Double(2.5))
      eventually(timeout(1.minute), interval(5.seconds)) {
        val gauges = reporter.periodSnapshot.metrics.gauges
        gauges should not be empty
        gauges.filter(_.name == "kamon-test-gauge") match {
          case Seq() => fail("metric not found")
          case Seq(gauges) => {
            //kamon only supports longs (so the 2.5 is rounded to 3)
            gauges.value shouldEqual 3
            gauges.tags shouldEqual Map("tag" -> "value")
          }
          case _ => fail("multiple metrics found with same name")
        }
      }
    } finally {
      registration.cancel()
    }
  }
}
