package com.github.micrometer.kamon

import java.util.function.ToDoubleFunction

import scala.concurrent.duration.DurationInt

import io.micrometer.core.instrument.{AbstractMeter, Gauge, Meter}
import kamon.Kamon

class KamonGauge[T](id: Meter.Id, obj: T, valueFunction: ToDoubleFunction[T]) extends AbstractMeter(id) with Gauge {
  val kamonGauge = Kamon.gauge(id.getName).refine(asMap(id.getTags()))
  scheduledExecutor.scheduleAtFixedRate(value())(1.minute)

  override def value(): Double = valueFunction.applyAsDouble(obj)
}
