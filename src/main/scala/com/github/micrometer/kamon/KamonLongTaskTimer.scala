package com.github.micrometer.kamon

import java.util.concurrent.TimeUnit

import io.micrometer.core.instrument.internal.DefaultLongTaskTimer
import io.micrometer.core.instrument.{AbstractMeter, LongTaskTimer, Meter}
import kamon.Kamon

class KamonLongTaskTimer[T](id: Meter.Id) extends AbstractMeter(id) with LongTaskTimer {
  val ltt = new DefaultLongTaskTimer(id, KamonClock)
  val activeCountGauge = Kamon.gauge(s"${id.getName}_active_count").refine(asMap(id.getTags()))
  val durationSumGauge = Kamon.gauge(s"${id.getName}_duration_sum").refine(asMap(id.getTags()))

  scheduledExecutor.scheduleAtFixedRate {
    activeCountGauge.set(activeTasks())
    durationSumGauge.set(Math.round(duration(TimeUnit.NANOSECONDS)))
  }(gaugeInterval)

  override def duration(task: Long, unit: TimeUnit): Double = ltt.duration(task, unit)

  override def duration(unit: TimeUnit): Double = ltt.duration(unit)

  override def start(): LongTaskTimer.Sample = ltt.start()

  override def stop(task: Long): Long = ltt.stop(task)

  override def activeTasks(): Int = ltt.activeTasks()
}
