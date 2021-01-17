package andrepnh.mtg.sim.analytics.mana;

import andrepnh.mtg.sim.analytics.TurnChartReport;
import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Value;

@Value
public class AvailableManaReport implements TurnChartReport {
  ImmutableList<Double> manaAvailablePerTurn;

  @Override
  public String getDataLabel() {
    return "mana (average)";
  }

  @Override
  public String getName() {
    return "Available mana per turn";
  }

  @Override
  public ImmutableList<Double> dataPerTurn() {
    return manaAvailablePerTurn;
  }

  @Override
  public List<String> toHumanReadableText() {
    var lines = new ArrayList<String>(1 + manaAvailablePerTurn.size()); // Includes report name
    lines.add(getName());
    IntStream.range(0, manaAvailablePerTurn.size())
        .mapToObj(turn -> Tuple.of(turn, manaAvailablePerTurn.get(turn)))
        .map(turnManaPair -> String.format(
            "  Turn %d: %.2f %s", turnManaPair._1, turnManaPair._2, getDataLabel()))
        .forEachOrdered(lines::add);
    return lines;
  }
}
