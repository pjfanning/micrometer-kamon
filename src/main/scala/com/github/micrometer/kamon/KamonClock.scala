package com.github.micrometer.kamon

import io.micrometer.core.instrument.Clock
import kamon.Kamon

object KamonClock extends Clock {
  val kamonClock = Kamon.clock()

  override def monotonicTime(): Long = kamonClock.nanos()

  override def wallTime(): Long = kamonClock.millis()
}
