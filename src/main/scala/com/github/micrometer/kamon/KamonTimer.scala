package com.github.micrometer.kamon

import java.util.concurrent.TimeUnit

import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.core.instrument.distribution.pause.PauseDetector
import io.micrometer.core.instrument.util.TimeUtils
import io.micrometer.core.instrument.{AbstractTimer, Meter, Timer}
import kamon.Kamon

object KamonTimer {
  val SupportsAggregablePercentiles = true
}

class KamonTimer(id: Meter.Id, config: DistributionStatisticConfig, pauseDetector: PauseDetector)
  extends AbstractTimer(id, KamonClock, config, pauseDetector, TimeUnit.NANOSECONDS, KamonTimer.SupportsAggregablePercentiles) with Timer {

  val kamonTimer = Kamon.timer(id.getName).refine(asMap(id.getTags))
  val simpleTimer = Timer.builder(id.getName).tags(id.getTags).register(simpleMeterRegistry)

  override def recordNonNegative(amount: Long, unit: TimeUnit): Unit = {
    kamonTimer.record(TimeUtils.convert(amount.toDouble, unit, TimeUnit.NANOSECONDS).toLong)
    simpleTimer.record(amount, unit)
  }

  override def max(unit: TimeUnit): Double = simpleTimer.max(unit)

  override def totalTime(unit: TimeUnit): Double = simpleTimer.totalTime(unit)

  override def count(): Long = simpleTimer.count()
}
