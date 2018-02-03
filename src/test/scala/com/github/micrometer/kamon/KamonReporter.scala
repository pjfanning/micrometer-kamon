package com.github.micrometer.kamon

import java.time.{Duration, Instant}

import com.typesafe.config.Config

import kamon.MetricReporter
import kamon.metric._

class KamonReporter extends MetricReporter {
  private val snapshotAccumulator = new PeriodSnapshotAccumulator(Duration.ofDays(365 * 5), Duration.ZERO)
  var periodSnapshot = new PeriodSnapshot(Instant.now(), Instant.now(),
    MetricsSnapshot(Seq.empty, Seq.empty, Seq.empty, Seq.empty))

  override def start(): Unit = {}
  override def stop(): Unit = {}
  override def reconfigure(config: Config): Unit = {}

  override def reportPeriodSnapshot(snapshot: PeriodSnapshot): Unit = {
    snapshotAccumulator.add(snapshot)
    periodSnapshot = snapshotAccumulator.peek()
  }
}


