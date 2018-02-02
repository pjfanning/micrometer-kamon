package com.github.micrometer.kamon

import java.lang
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import java.util.function.{ToDoubleFunction, ToLongFunction}

import io.micrometer.core.instrument.histogram.HistogramConfig
import io.micrometer.core.instrument.histogram.pause.PauseDetector
import io.micrometer.core.instrument._
import io.micrometer.core.instrument.cumulative.{CumulativeDistributionSummary, CumulativeFunctionCounter, CumulativeFunctionTimer}
import io.micrometer.core.instrument.internal.{DefaultLongTaskTimer, DefaultMeter}
import io.micrometer.core.instrument.simple.{CountingMode, SimpleConfig}
import io.micrometer.core.instrument.step.StepDistributionSummary

class KamonMeterRegistry extends MeterRegistry(KamonClock) {
  override protected def defaultHistogramConfig(): HistogramConfig = {
    HistogramConfig.builder.histogramExpiry(SimpleConfig.DEFAULT.step).build.merge(HistogramConfig.DEFAULT)
  }

  override protected def newGauge[T](id: Meter.Id, obj: T, valueFunction: ToDoubleFunction[T]): Gauge =
    new KamonGauge(id, obj, valueFunction)

  override protected def newLongTaskTimer(id: Meter.Id): LongTaskTimer = new DefaultLongTaskTimer(id, clock)

  override protected def newMeter(id: Meter.Id, `type`: Meter.Type, measurements: lang.Iterable[Measurement]): Meter =
    new DefaultMeter(id, `type`, measurements)

  override protected def newFunctionTimer[T](id: Meter.Id, obj: T, countFunction: ToLongFunction[T],
                                             totalTimeFunction: ToDoubleFunction[T], totalTimeFunctionUnits: TimeUnit) =
    new CumulativeFunctionTimer[T](id, obj, countFunction, totalTimeFunction, totalTimeFunctionUnits, getBaseTimeUnit)

  override protected def newFunctionCounter[T](id: Meter.Id, obj: T, valueFunction: ToDoubleFunction[T]) =
    new CumulativeFunctionCounter[T](id, obj, valueFunction)

  override protected def newCounter(id: Meter.Id) = new KamonCounter(id)

  override protected def newDistributionSummary(id: Meter.Id, histogramConfig: HistogramConfig): DistributionSummary = {
    val percentileFormat = new DecimalFormat("#.####")
    val merged = histogramConfig.merge(HistogramConfig.builder.histogramExpiry(SimpleConfig.DEFAULT.step).build)
    val summary = SimpleConfig.DEFAULT.mode match {
      case CountingMode.CUMULATIVE => new CumulativeDistributionSummary(id, clock, merged)
      case _ => new StepDistributionSummary(id, clock, merged)
    }
    if (histogramConfig.getPercentiles != null) for (percentile <- histogramConfig.getPercentiles) {
      gauge(id.getName, Tags.concat(getConventionTags(id), "percentile", percentileFormat.format(percentile)), summary, (s: DistributionSummary) => summary.percentile(percentile))
    }
    if (histogramConfig.isPublishingHistogram) {
      import scala.collection.JavaConverters._
      for (bucket <- histogramConfig.getHistogramBuckets(false).asScala) {
        more.counter(getConventionName(id), Tags.concat(getConventionTags(id), "bucket", bucket.toString),
          summary, (s: DistributionSummary) => s.histogramCountAtValue(bucket))
      }
    }
    summary
  }

  override protected def getBaseTimeUnit = TimeUnit.NANOSECONDS

  override protected def newTimer(id: Meter.Id, histogramConfig: HistogramConfig, pauseDetector: PauseDetector): Timer =
    new KamonTimer(id, histogramConfig, pauseDetector)
}
