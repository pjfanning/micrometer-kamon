package com.github.micrometer.kamon

import io.micrometer.core.instrument.{AbstractMeter, Counter, Meter}
import io.micrometer.core.instrument.util.MeterEquivalence
import kamon.Kamon

class KamonCounter(id: Meter.Id) extends AbstractMeter(id) with Counter {
  val kamonCounter = Kamon.counter(id.getName).refine(asMap(id.getTags))
  val simpleCounter = simpleMeterRegistry.counter(id.getName, id.getTags)

  override def count(): Double = simpleCounter.count()

  /**
    * @param amount value is converted to Long (using Math.round) as Kamon Counter only supports Longs
    */
  override def increment(amount: Double): Unit = {
    simpleCounter.increment(amount)
    kamonCounter.increment(Math.round(amount))
  }

  override def equals(o: Any): Boolean = MeterEquivalence.equals(this, o)

  override def hashCode: Int = MeterEquivalence.hashCode(this)
}
