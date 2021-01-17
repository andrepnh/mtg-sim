package andrepnh.mtg.sim.analytics.mana;

import andrepnh.mtg.sim.analytics.BaseAnalysis;
import andrepnh.mtg.sim.sim.GameState;
import andrepnh.mtg.sim.util.EitherFlux;
import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class AvailableManaAnalysis extends BaseAnalysis<AvailableManaReport> {

  @Override
  protected Mono<AvailableManaReport> doAnalyze(EitherFlux<GameState> gameStates) {
    // Flux.groupBy stalls, cardinality is probably too big
    Mono<Map<Integer, Tuple2<Integer, Integer>>> sumAndCountPerTurn = Mono
        .defer(() -> {
          Map<Integer, Tuple2<Integer, Integer>> map = new ConcurrentHashMap<>();
          gameStates.subscribe(state -> map
              .compute(
                  state.getTurn(),
                  (t, sumCount) -> sumCount == null
                      ? Tuple.of(state.getLands().size(), 1)
                      : Tuple.of(sumCount._1 + state.getLands().size(), sumCount._2 + 1)));
          return Mono.just(map);
        });

    Mono<List<Double>> averageManaPerTurn = sumAndCountPerTurn.map(map -> map
        .entrySet()
        .stream()
        .sorted(Entry.comparingByKey())
        .map(pair -> (double) pair.getValue()._1 / pair.getValue()._2)
        .collect(Collectors.toList()));

    return averageManaPerTurn.map(map -> new AvailableManaReport(ImmutableList.copyOf(map)));
  }
}
