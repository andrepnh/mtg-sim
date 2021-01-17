package andrepnh.mtg.sim.analytics.synergy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import andrepnh.mtg.sim.analytics.TurnTableReport;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Value;
import reactor.core.publisher.Flux;

@Value
public class SynergyReport implements TurnTableReport {
  private static final String TOTAL_FIELD = "Total";
  private static final Comparator<Cell<String, Integer, Double>> REPORT_CELL_COMPARATOR
      = Comparator.<Cell<String, Integer, Double>>comparingInt(Cell::getColumnKey)
          .thenComparing((c1, c2) -> {
            if (c1.getRowKey().equals(TOTAL_FIELD)) {
              return Integer.MAX_VALUE;
            } else if (c2.getRowKey().equals(TOTAL_FIELD)) {
              return Integer.MIN_VALUE;
            } else {
              return c1.getRowKey().compareTo(c2.getRowKey());
            }
          });
  ImmutableTable<String, Integer, Double> synergiesPerTurn;

  private SynergyReport(ImmutableTable<String, Integer, Double> synergiesPerTurn) {
    checkArgument(!checkNotNull(synergiesPerTurn).isEmpty());
    this.synergiesPerTurn = synergiesPerTurn;
  }

  public static SynergyReport fromSynergyPercentages(
      ImmutableTable<String, Integer, Double> percentagesPerSynergyAndTurn) {
    Table<String, Integer, Double> allSynergiesPercentagesPerTurn
        = calculateTotalSynergiesPercentagePerTurn(percentagesPerSynergyAndTurn);
    allSynergiesPercentagesPerTurn.putAll(percentagesPerSynergyAndTurn);

    return new SynergyReport(ImmutableTable.copyOf(allSynergiesPercentagesPerTurn));
  }

  private static Table<String, Integer, Double> calculateTotalSynergiesPercentagePerTurn(
      Table<String, Integer, Double> percPerNameAndTurn) {
    Map<Integer, Double> totalPercPerTurn = percPerNameAndTurn
        .cellSet()
        .stream()
        .collect(Collectors.groupingBy(
            Cell::getColumnKey,
            Collectors.summingDouble(Cell::getValue)));

    return totalPercPerTurn
        .entrySet()
        .stream()
        .map(entry -> Tables.immutableCell(TOTAL_FIELD, entry.getKey(), entry.getValue()))
        .collect(Tables.toTable(
            Cell::getRowKey,
            Cell::getColumnKey,
            Cell::getValue,
            HashBasedTable::create));
  }

  @Override
  public String getDataLabel() {
    return "% of games";
  }

  @Override
  public ImmutableTable<String, Integer, Double> dataPerTurn() {
    return synergiesPerTurn;
  }

  @Override
  public Comparator<Cell<String, Integer, Double>> cellComparator() {
    return REPORT_CELL_COMPARATOR;
  }

  @Override
  public String getName() {
    return "Synergies achieved per turn";
  }

  @Override
  public List<String> toHumanReadableText() {
    var lines = new ArrayList<String>(1 + synergiesPerTurn.size()); // Includes report name
    lines.add(getName());
    String turnHeader = IntStream.range(0, synergiesPerTurn.rowKeySet().size())
        .mapToObj(String::valueOf)
        .collect(Collectors.joining("\t", "\t", ""));
    lines.add(turnHeader);
    Flux.fromStream(synergiesPerTurn.cellSet().stream())
        .sort(REPORT_CELL_COMPARATOR)
        .groupBy(Cell::getColumnKey)
        .flatMap(
            row -> row
                .map(cell -> String.format("%.2f%%", cell.getValue() * 100))
                .collect(Collectors.joining("\t", row.key() + "\t", "")),
            Runtime.getRuntime().availableProcessors())
        .collectList()
        .subscribe(nonHeaderLines -> lines.addAll(nonHeaderLines));

    return lines;
  }

}
