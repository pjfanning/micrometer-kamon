package com.github.micrometer.kamon

import java.util.concurrent._
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

// based on https://gist.github.com/platy/8f0e634c64d9fb54559c
object ScheduledExecutor {
  private val defaultHandler: RejectedExecutionHandler = new AbortPolicy
}

/**
  * A thread pool backed executor for tasks scheduled in the future
  * @param corePoolSize the number of threads to keep in the pool, even
  *                     if they are idle, unless { @code allowCoreThreadTimeOut} is set
  * @param threadFactory the factory to use when the executor
  *                      creates a new thread
  * @param handler the handler to use when execution is blocked
  *                because the thread bounds and queue capacities are reached
  * @throws IllegalArgumentException if { @code corePoolSize < 0}
  * @throws NullPointerException if { @code threadFactory} or
  *                                         { @code handler} is null
  */
class ScheduledExecutor(corePoolSize: Int,
                        threadFactory: ThreadFactory = Executors.defaultThreadFactory,
                        handler: RejectedExecutionHandler = ScheduledExecutor.defaultHandler) {

  private val underlying: ScheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler)

  def scheduleAtFixedRate(operation: ⇒ Unit)(interval: FiniteDuration): Unit = {
    underlying.scheduleAtFixedRate(new Runnable {
      override def run() = operation
    }, interval.length, interval.length, interval.unit)
  }
}
