package com.github.micrometer

import java.lang.{Iterable => JIterable}

import scala.collection.JavaConverters._

import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

package object kamon {
  lazy val simpleMeterRegistry = new SimpleMeterRegistry()
  lazy val scheduledExecutor = new ScheduledExecutor(10)

  def asMap(tags: JIterable[Tag]): Map[String, String] = {
    val seq =tags.asScala .map { tag => (tag.getKey, tag.getValue) }
    seq.toMap
  }
}
