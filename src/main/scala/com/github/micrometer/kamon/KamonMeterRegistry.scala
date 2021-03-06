package com.github.micrometer.kamon

import java.lang
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import java.util.function.{ToDoubleFunction, ToLongFunction}

import scala.collection.JavaConverters._

import io.micrometer.core.instrument._
import io.micrometer.core.instrument.cumulative.CumulativeDistributionSummary
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.core.instrument.distribution.pause.PauseDetector
import io.micrometer.core.instrument.internal.DefaultMeter
import io.micrometer.core.instrument.simple.{CountingMode, SimpleConfig}
import io.micrometer.core.instrument.step.StepDistributionSummary

class KamonMeterRegistry extends MeterRegistry(KamonClock) {
  override protected def defaultHistogramConfig(): DistributionStatisticConfig = {
    DistributionStatisticConfig.builder.expiry(SimpleConfig.DEFAULT.step).build.merge(DistributionStatisticConfig.DEFAULT)
  }

  override protected def newGauge[T](id: Meter.Id, obj: T, valueFunction: ToDoubleFunction[T]): Gauge =
    KamonGauge(id, obj, valueFunction)

  override protected def newLongTaskTimer(id: Meter.Id): LongTaskTimer = new KamonLongTaskTimer(id)

  override protected def newMeter(id: Meter.Id, `type`: Meter.Type, measurements: lang.Iterable[Measurement]): Meter =
    new DefaultMeter(id, `type`, measurements)

  override protected def newFunctionTimer[T](id: Meter.Id, obj: T, countFunction: ToLongFunction[T],
                                             totalTimeFunction: ToDoubleFunction[T], totalTimeFunctionUnits: TimeUnit): FunctionTimer =
    KamonFunctionTimer[T](id, obj, countFunction, totalTimeFunction, totalTimeFunctionUnits, getBaseTimeUnit)

  override protected def newFunctionCounter[T](id: Meter.Id, obj: T, valueFunction: ToDoubleFunction[T]): FunctionCounter =
    KamonFunctionCounter[T](id, obj, valueFunction)

  override protected def newCounter(id: Meter.Id) = new KamonCounter(id)

  override protected def newDistributionSummary(id: Meter.Id, config: DistributionStatisticConfig, scale: Double): DistributionSummary = {
    val percentileFormat = new DecimalFormat("#.####")
    val merged = config.merge(DistributionStatisticConfig.builder.expiry(SimpleConfig.DEFAULT.step).build)
    val summary = SimpleConfig.DEFAULT.mode match {
      case CountingMode.CUMULATIVE => new CumulativeDistributionSummary(id, clock, merged, scale)
      case _ => new StepDistributionSummary(id, clock, merged, scale)
    }
    if (config.getPercentiles != null) for (percentile <- config.getPercentiles) {
      val op = asJava((ds: DistributionSummary) => ds.percentile(percentile))
      gauge(id.getName, Tags.concat(getConventionTags(id), "percentile", percentileFormat.format(percentile)), summary, op)
    }
    if (config.isPublishingHistogram) {
      for (bucket <- config.getHistogramBuckets(false).asScala) {
        val op = asJava((s: DistributionSummary) => s.histogramCountAtValue(bucket))
        more.counter(getConventionName(id), Tags.concat(getConventionTags(id), "bucket", bucket.toString), summary, op)
      }
    }
    summary
  }

  override protected def getBaseTimeUnit = TimeUnit.NANOSECONDS

  override protected def newTimer(id: Meter.Id, config: DistributionStatisticConfig, pauseDetector: PauseDetector): Timer =
    new KamonTimer(id, config, pauseDetector)
}
