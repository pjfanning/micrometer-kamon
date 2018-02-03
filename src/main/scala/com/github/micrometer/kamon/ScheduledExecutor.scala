package com.github.micrometer.kamon

import java.util.concurrent._
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

import com.typesafe.config.Config

// based on https://gist.github.com/platy/8f0e634c64d9fb54559c
private[kamon] object ScheduledExecutor {
  private val defaultHandler: RejectedExecutionHandler = new AbortPolicy
}

private[kamon] class ScheduledExecutor(config: Config,
                        threadFactory: ThreadFactory = Executors.defaultThreadFactory,
                        handler: RejectedExecutionHandler = ScheduledExecutor.defaultHandler) {

  private val corePoolSize = config.getInt("micrometer.kamon.gauge.scheduler.threads")
  private val underlying: ScheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler)

  def scheduleAtFixedRate(operation: ⇒ Unit)(interval: FiniteDuration): Unit = {
    underlying.scheduleAtFixedRate(new Runnable {
      override def run() = operation
    }, interval.length, interval.length, interval.unit)
  }
}
