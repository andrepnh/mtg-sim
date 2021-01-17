package andrepnh.mtg.sim.analytics;

import com.google.common.collect.ImmutableList;

public interface TurnChartReport extends Report {
  ImmutableList<Double> dataPerTurn();

  default int yAxisMaximum() {
    return 15;
  }
}
