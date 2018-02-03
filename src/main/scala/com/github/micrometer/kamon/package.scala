package com.github.micrometer

import java.lang.{Iterable => JIterable}

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigResolveOptions}

import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

package object kamon {
  implicit def asFiniteDuration(d: java.time.Duration) = Duration.fromNanos(d.toNanos)

  lazy val config = ConfigFactory.load(this.getClass.getClassLoader, ConfigParseOptions.defaults(), ConfigResolveOptions.defaults().setAllowUnresolved(true))
  lazy val simpleMeterRegistry = new SimpleMeterRegistry()
  lazy val scheduledExecutor = new ScheduledExecutor(config)
  lazy val gaugeInterval: FiniteDuration = config.getDuration("micrometer.kamon.gauge.scheduler.interval")

  def asMap(tags: JIterable[Tag]): Map[String, String] = {
    val seq =tags.asScala .map { tag => (tag.getKey, tag.getValue) }
    seq.toMap
  }
}
