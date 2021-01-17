package andrepnh.mtg.sim.analytics.format;

import andrepnh.mtg.sim.analytics.TurnTableReport;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class CsvTableFormatter implements ReportFormatter {
  private final TurnTableReport report;

  @Override
  public String getFileExtension() {
    return "csv";
  }

  @Override
  public List<String> format() {
    List<String> headers = Lists.newArrayList("");
    report.dataPerTurn()
        .columnKeySet()
        .stream()
        .sorted()
        .map(turn -> "Turn " + turn)
        .forEachOrdered(headers::add);

    Stream<ArrayList<String>> dataRows = report.dataPerTurn()
        .rowMap()
        .entrySet()
        .stream()
        .sorted(Entry.comparingByKey())
        .map(row -> {
          var csvRow = Lists.newArrayList(row.getKey());
          row.getValue()
              .entrySet()
              .stream()
              .sorted(Entry.comparingByKey())
              .forEachOrdered(entry -> csvRow.add(String.valueOf(entry.getValue())));
          return csvRow;
        });

    List<String> csv = Lists.newArrayList(String.join(",", headers));
    dataRows.forEachOrdered(row -> csv.add(String.join(",", row)));

    return csv;
  }
}
