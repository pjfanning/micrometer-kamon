package com.github.micrometer.kamon

import java.util.concurrent.TimeUnit
import java.util.function.{ToDoubleFunction, ToLongFunction}

import io.micrometer.core.instrument.cumulative.CumulativeFunctionTimer
import io.micrometer.core.instrument.{AbstractMeter, FunctionTimer, Meter}
import kamon.Kamon

object KamonFunctionTimer{
  def apply[T](id: Meter.Id, obj: T, countFunction: ToLongFunction[T], totalTimeFunction: ToDoubleFunction[T],
               totalTimeFunctionUnits: TimeUnit, baseTimeUnit: TimeUnit): KamonFunctionTimer[T] = {
    val cumulativeFunctionTimer = new CumulativeFunctionTimer(id, obj, countFunction, totalTimeFunction,
      totalTimeFunctionUnits, baseTimeUnit)
    new KamonFunctionTimer(id, cumulativeFunctionTimer)
  }
}

class KamonFunctionTimer[T](id: Meter.Id, cumulativeFunctionTimer: CumulativeFunctionTimer[T])
  extends AbstractMeter(id) with FunctionTimer {
  val countGauge = Kamon.gauge(s"${id.getName}_count").refine(asMap(id.getTags()))
  val timeGauge = Kamon.gauge(s"${id.getName}_sum").refine(asMap(id.getTags()))
  scheduledExecutor.scheduleAtFixedRate {
    countGauge.set(Math.round(count()))
    timeGauge.set(Math.round(totalTime(baseTimeUnit())))
  }(gaugeInterval)

  override def baseTimeUnit(): TimeUnit = cumulativeFunctionTimer.baseTimeUnit()

  override def totalTime(unit: TimeUnit): Double = cumulativeFunctionTimer.totalTime(unit)

  override def count(): Double = cumulativeFunctionTimer.count()
}
