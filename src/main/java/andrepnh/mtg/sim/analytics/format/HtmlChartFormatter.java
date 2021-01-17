package andrepnh.mtg.sim.analytics.format;

import andrepnh.mtg.sim.analytics.TurnChartReport;
import com.google.common.io.Resources;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class HtmlChartFormatter implements ReportFormatter {
  private static final String HTML_TEMPLATE;
  private final TurnChartReport report;

  static {
    try {
      List<String> lines = Resources.readLines(
          HtmlChartFormatter.class.getResource("/chart/html/template.html"),
          StandardCharsets.UTF_8);
      HTML_TEMPLATE = String.join("", lines);
    } catch (Exception ex) {
      log.error("Could not read HTML template file", ex);
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public String getFileExtension() {
    return "html";
  }

  @Override
  public List<String> format() {
    List<String> seriesLabels = IntStream.range(0, report.dataPerTurn().size())
        .mapToObj(turn -> "'Turn " + turn + "'")
        .collect(Collectors.toList());
    String html = HTML_TEMPLATE
        .replace("%series_labels%", seriesLabels.toString())
        .replace("%data_hover_label%", report.getDataLabel())
        .replace("%data%", report.dataPerTurn().toString())
        .replace("%y_axis_suggested_max%", String.valueOf(report.yAxisMaximum()));
    return Arrays.asList(html.split("\n"));
  }
}
