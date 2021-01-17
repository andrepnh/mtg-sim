package andrepnh.mtg.sim.analytics.format;

import static org.assertj.core.api.Assertions.assertThat;

import andrepnh.mtg.sim.analytics.TurnChartReport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Value;
import org.junit.jupiter.api.Test;

class HtmlChartFormatterTest {

  @Test
  void extensionIsHtml() {
    var formatter = new HtmlChartFormatter(TestReport.forTurns(1.0));

    assertThat(formatter.getFileExtension()).isEqualTo("html");
  }

  @Test
  void formatShouldUseReportToSetChartSuggestMaxY() {
    int expectedYMax = 101;
    Pattern jsRegex = matchingChartJsYAxisSuggestedMax(expectedYMax);
    var formatter = new HtmlChartFormatter(TestReport.forTurns(expectedYMax, 1.0));

    String formatted = String.join(System.lineSeparator(), formatter.format());

    assertThat(formatted).matches(jsRegex);
  }

  @Test
  void formatShouldUseReportDataLabelToSetChartDataSetLabel() {
    String expectedLabel = "Some serious data";
    Pattern jsRegex = matchingChartJsDataSetLabel(expectedLabel);
    var formatter = new HtmlChartFormatter(TestReport.forTurns(expectedLabel, 1.0));

    String formatted = String.join(System.lineSeparator(), formatter.format());

    assertThat(formatted).matches(jsRegex);
  }

  @Test
  void formatShouldUseTurnNumbersToSetXAxisLabels() {
    var report = TestReport.forTurns(1.0, 1.0, 1.0);
    int expectedTurns = report.getData().size();
    var formatter = new HtmlChartFormatter(report);
    Pattern jsRegex = matchingChartJsXAxisLabelForTurns(expectedTurns);

    String formatted = String.join(System.lineSeparator(), formatter.format());

    assertThat(formatted).matches(jsRegex);
  }

  @Test
  void formatShouldUseReportDataAsChartData() {
    var report = TestReport.forTurns(1.0, 2.0, 3.0);
    var expectedData = report.getData();
    var formatter = new HtmlChartFormatter(report);
    Pattern jsRegex = matchingChartJsData(expectedData);

    String formatted = String.join(System.lineSeparator(), formatter.format());

    assertThat(formatted).matches(jsRegex);
  }

  private Pattern matchingChartJsData(ImmutableList<Double> expectedData) {
    var chartData = expectedData.toString();

    var regex = String.format(
        // datasets: [{ label: 'mana (average)', data: [0.0, 0.9813, 1.907, 2.7381, 3.4573, ...],
        ".*datasets\\s*:\\s*\\[\\s*\\{.*?data\\s*:\\s*%s.*",
        chartData);
    return Pattern.compile(regex);
  }

  private Pattern matchingChartJsXAxisLabelForTurns(int expectedTurns) {
    var labels = IntStream.range(0, expectedTurns)
        .mapToObj(turn -> String.format("'Turn %d'", turn))
        .collect(Collectors.toList())
        .toString();

    var regex = String.format(
        // new Chart(ctx, { type: 'bar', data: { labels: ['Turn 0', 'Turn 1', 'Turn 2', ...]
        ".*new\\s+Chart\\s*\\(.*?data\\s*:\\s*\\{.*?labels\\s*:\\s*%s.*",
        labels);
    return Pattern.compile(regex);
  }

  private Pattern matchingChartJsDataSetLabel(String expectedLabel) {
    // datasets: [{ label: 'mana (average)',
    var regex = String.format(
        ".*datasets\\s*:\\s*\\[\\s*\\{.*?label\\s*:\\s*['\"]%s['\"].*",
        expectedLabel);
    return Pattern.compile(regex);
  }

  private Pattern matchingChartJsYAxisSuggestedMax(int expectedYMax) {
    var regex = String.format(
        // yAxes: [{ ticks: { beginAtZero: true, suggestedMax: 15 }
        ".*yAxes\\s*:\\s*\\[\\s*\\{\\s*ticks\\s*:\\s*\\{.*?suggestedMax\\s*:\\s*%d.*", expectedYMax);
    return Pattern.compile(regex);
  }

  @Value
  private static class TestReport implements TurnChartReport {
    ImmutableList<Double> data;
    Integer yMax;
    String dataLabel;

    public static TestReport forTurns(Double turn0Data, Double... turnsData) {
      return new TestReport(ImmutableList.copyOf(Lists.asList(turn0Data, turnsData)));
    }

    public static TestReport forTurns(int yMax, Double turn0Data, Double... turnsData) {
      return new TestReport(yMax, ImmutableList.copyOf(Lists.asList(turn0Data, turnsData)));
    }

    public static TestReport forTurns(String dataLabel, Double turn0Data, Double... turnsData) {
      return new TestReport(null, dataLabel, ImmutableList.copyOf(Lists.asList(turn0Data, turnsData)));
    }

    public TestReport(ImmutableList<Double> data) {
      this(null, data);
    }

    public TestReport(Integer yMax, ImmutableList<Double> data) {
      this(yMax, "Data Label", data);
    }

    public TestReport(Integer yMax, String dataLabel, ImmutableList<Double> data) {
      this.data = data;
      this.yMax = yMax;
      this.dataLabel = dataLabel;
    }

    @Override
    public ImmutableList<Double> dataPerTurn() {
      return data;
    }

    @Override
    public int yAxisMaximum() {
      return yMax == null ? TurnChartReport.super.yAxisMaximum() : yMax;
    }

    @Override
    public String getDataLabel() {
      return dataLabel;
    }

    @Override
    public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> toHumanReadableText() {
      throw new UnsupportedOperationException();
    }
  }
}