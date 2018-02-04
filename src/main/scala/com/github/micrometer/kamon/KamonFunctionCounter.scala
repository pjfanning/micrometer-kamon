package com.github.micrometer.kamon

import java.util.function.ToDoubleFunction

import io.micrometer.core.instrument.cumulative.CumulativeFunctionCounter
import io.micrometer.core.instrument.{AbstractMeter, FunctionCounter, Meter}
import kamon.Kamon

object KamonFunctionCounter {
  def apply[T](id: Meter.Id, obj: T, op: ToDoubleFunction[T]): KamonFunctionCounter[T] = {
    val cumulativeFunctionCounter = new CumulativeFunctionCounter(id, obj, op)
    new KamonFunctionCounter(id, cumulativeFunctionCounter)
  }
}

class KamonFunctionCounter[T](id: Meter.Id, cumulativeFunctionCounter: CumulativeFunctionCounter[T])
  extends AbstractMeter(id) with FunctionCounter {
  val kamonGauge = Kamon.gauge(id.getName).refine(asMap(id.getTags()))
  scheduledExecutor.scheduleAtFixedRate(kamonGauge.set(Math.round(count())))(gaugeInterval)

  override def count(): Double = cumulativeFunctionCounter.count()
}
