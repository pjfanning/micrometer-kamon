package com.github.micrometer.kamon

import java.lang.ref.WeakReference
import java.util.function.ToDoubleFunction

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer

import io.micrometer.core.instrument.{AbstractMeter, Gauge, Meter}
import kamon.Kamon

object KamonGauge {
  private[kamon] val map = TrieMap[Meter.Id, KamonGauge[_]]()

  def apply[T](id: Meter.Id, obj: T, valueFunction: ToDoubleFunction[T]): Gauge = {
    val gauge = map.getOrElseUpdate(id, new KamonGauge(id))
    gauge.track(obj, valueFunction)
  }
}

private[kamon] class KamonGauge[T](id: Meter.Id) extends AbstractMeter(id) with Gauge {

  val kamonGauge = Kamon.gauge(id.getName).refine(asMap(id.getTags()))
  val tracked = ListBuffer[() => Double]()

  def track[T](obj: T, valueFunction: ToDoubleFunction[T]): Gauge = {
    val ref = new WeakReference(obj)
    val fn = () => {
      Option(ref.get) match {
        case Some(obj) => valueFunction.applyAsDouble(obj)
        case _ => 0.0
      }
    }
    tracked.+=(fn)
    this
  }

  scheduledExecutor.scheduleAtFixedRate(kamonGauge.set(Math.round(value())))(gaugeInterval)

  override def value(): Double = {
    tracked.foldLeft(0.0) { (acc, t) => acc + t.apply() }
  }
}
