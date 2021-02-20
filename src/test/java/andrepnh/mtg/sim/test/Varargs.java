package andrepnh.mtg.sim.test;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

public class Varargs implements ArgumentsAggregator {
  @Override
  public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
      throws ArgumentsAggregationException {
    return accessor.toList().stream()
        .skip(context.getIndex())
        .map(String::valueOf)
        .toArray(String[]::new);
  }
}