package andrepnh.mtg.sim.analytics.format;

import static org.assertj.core.api.Assertions.assertThat;

import andrepnh.mtg.sim.analytics.TurnTableReport;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table.Cell;
import java.util.Comparator;
import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.Test;

class CsvTableFormatterTest {

  @Test
  void extensionIsCsv() {
    var formatter = new CsvTableFormatter(TestReport
        .rowsWithTwoTurns("whatever", 1, 2));

    assertThat(formatter.getFileExtension()).isEqualTo("csv");
  }

  @Test
  void formatShouldUseTurnsAsColumnsWithHeaders() {
    var formatter = new CsvTableFormatter(
        TestReport.rowsWithTwoTurns("whatever", 1, 2));

    List<String> rows = formatter.format();

    String[] headerColumns = rows.get(0).split(",");
    assertThat(headerColumns).containsExactly("", "Turn 0", "Turn 1");
  }

  @Test
  void formatShouldPlaceCellsValuesOnCorrectPosition() {
    var formatter = new CsvTableFormatter(
        TestReport.rowsWithTwoTurns(
            "whatever", 1.0, 2.0,
            "whatever too", 100.0, 200.0));

    List<String> rows = formatter.format();

    String[] row1 = rows.get(1).split(",");
    assertThat(row1).containsExactly("whatever", "1.0", "2.0");
    String[] row2 = rows.get(2).split(",");
    assertThat(row2).containsExactly("whatever too", "100.0", "200.0");
  }

  @Test
  void formatShouldSortCellsUsingComparator() {
    var dataOutOfOrder = ImmutableTable
        .<String, Integer, Double>builder()
        .put("Z", 1, 26.1)
        .put("A", 0, 1.0)
        .put("Z", 0, 26.0)
        .put("A", 1, 1.1)
        .build();
    var comparator = Comparator
        .<Cell<String, Integer, Double>>comparingInt(Cell::getColumnKey)
        .thenComparing(Cell::getRowKey);
    var formatter = new CsvTableFormatter(new TestReport(dataOutOfOrder, comparator));

    List<String> rows = formatter.format();

    String[] headers = rows.get(0).split(",");
    assertThat(headers).containsExactly("", "Turn 0", "Turn 1");
    String[] row1 = rows.get(1).split(",");
    assertThat(row1).containsExactly("A", "1.0", "1.1");
    String[] row2 = rows.get(2).split(",");
    assertThat(row2).containsExactly("Z", "26.0", "26.1");
  }

  @Value
  private static class TestReport implements TurnTableReport {
    ImmutableTable<String, Integer, Double> data;
    Comparator<Cell<String, Integer, Double>> comparator;

    public static TestReport rowsWithTwoTurns(String rowName, double turn0Value, double turn1Value) {
      return new TestReport(ImmutableTable
          .<String, Integer, Double>builder()
          .put(rowName, 0, turn0Value)
          .put(rowName, 1, turn1Value)
          .build());
    }

    public static TestReport rowsWithTwoTurns(
        String rowName1, double row1Turn0Value, double row1Turn1Value,
        String rowName2, double row2Turn0Value, double row2Turn1Value) {
      return new TestReport(ImmutableTable
          .<String, Integer, Double>builder()
          .put(rowName1, 0, row1Turn0Value)
          .put(rowName1, 1, row1Turn1Value)
          .put(rowName2, 0, row2Turn0Value)
          .put(rowName2, 1, row2Turn1Value)
          .build());
    }

    public TestReport(
        ImmutableTable<String, Integer, Double> data) {
      this.data = data;
      this.comparator = Comparator
          .<Cell<String, Integer, Double>>comparingInt(Cell::getColumnKey)
          .thenComparing(Cell::getColumnKey);
    }

    public TestReport(
        ImmutableTable<String, Integer, Double> data,
        Comparator<Cell<String, Integer, Double>> comparator) {
      this.data = data;
      this.comparator = comparator;
    }

    @Override
    public ImmutableTable<String, Integer, Double> dataPerTurn() {
      return data;
    }

    @Override
    public Comparator<Cell<String, Integer, Double>> cellComparator() {
      return comparator;
    }

    @Override
    public String getDataLabel() {
      throw new UnsupportedOperationException();
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