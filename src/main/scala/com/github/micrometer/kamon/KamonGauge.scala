package com.github.micrometer.kamon

import java.util.function.ToDoubleFunction

import io.micrometer.core.instrument.{AbstractMeter, Gauge, Meter}
import kamon.Kamon

class KamonGauge[T](id: Meter.Id, obj: T, valueFunction: ToDoubleFunction[T]) extends AbstractMeter(id) with Gauge {
  val kamonGauge = Kamon.gauge(id.getName).refine(asMap(id.getTags()))
  scheduledExecutor.scheduleAtFixedRate(kamonGauge.set(Math.round(value())))(gaugeInterval)

  override def value(): Double = valueFunction.applyAsDouble(obj)
}
