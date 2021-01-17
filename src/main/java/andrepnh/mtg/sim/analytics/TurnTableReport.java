package andrepnh.mtg.sim.analytics;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table.Cell;
import java.util.Comparator;

public interface TurnTableReport extends Report {
  ImmutableTable<String, Integer, Double> dataPerTurn();

  Comparator<Cell<String, Integer, Double>> cellComparator();
}
