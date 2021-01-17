package andrepnh.mtg.sim.analytics.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import andrepnh.mtg.sim.analytics.Report;
import andrepnh.mtg.sim.analytics.TurnChartReport;
import andrepnh.mtg.sim.analytics.TurnTableReport;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormatterFactoryTest {
  private final FormatterFactory factory = new FormatterFactory();

  @Test
  void getInstanceShouldReturnHtmlFormatterForTurnChartReports() {
    assertThat(factory.getInstance(mock(TurnChartReport.class)))
        .isInstanceOf(HtmlChartFormatter.class);
  }

  @Test
  void getInstanceShouldReturnCsvFormatterForTurnTableReports() {
    assertThat(factory.getInstance(mock(TurnTableReport.class)))
        .isInstanceOf(CsvTableFormatter.class);
  }

  @Test
  void getInstanceShouldFailForUnknownReports() {
    var unknownReport = new Report() {
      @Override
      public String getDataLabel() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public List<String> toHumanReadableText() {
        return null;
      }
    };

    assertThatThrownBy(() -> factory.getInstance(unknownReport))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(unknownReport.toString());
  }
}