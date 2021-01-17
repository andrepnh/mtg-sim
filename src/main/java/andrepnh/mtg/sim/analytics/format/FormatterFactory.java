package andrepnh.mtg.sim.analytics.format;

import andrepnh.mtg.sim.analytics.Report;
import andrepnh.mtg.sim.analytics.TurnChartReport;
import andrepnh.mtg.sim.analytics.TurnTableReport;

public class FormatterFactory {
  public ReportFormatter getInstance(Report report) {
    if (report instanceof TurnChartReport) {
      return new HtmlChartFormatter((TurnChartReport) report);
    } else if (report instanceof TurnTableReport) {
      return new CsvTableFormatter((TurnTableReport) report);
    } else {
      throw new IllegalStateException("Unknown report type; report: " + report);
    }
  }
}
