package andrepnh.mtg.sim.analytics.mana;

import andrepnh.mtg.sim.analytics.BaseAnalysis;
import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Spell;
import andrepnh.mtg.sim.sim.GameState;
import andrepnh.mtg.sim.util.EitherFlux;
import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

public class ManaCurveAnalysis extends BaseAnalysis<ManaCurveReport> {

  @Override
  protected Mono<ManaCurveReport> doAnalyze(EitherFlux<GameState> gameStates) {
    // Flux.groupBy stalls, cardinality is probably too big
    Mono<Map<Integer, Tuple2<Integer, Integer>>> sumAndCountPerTurn = Mono
        .defer(() -> {
          Map<Integer, Tuple2<Integer, Integer>> map = new ConcurrentHashMap<>();
          gameStates.subscribe(state -> map
              .compute(
                  state.getTurn(),
                  (t, sumCount) -> sumCount == null
                      ? Tuple.of(sumPlayedCmc(state), 1)
                      : Tuple.of(sumCount._1 + sumPlayedCmc(state), sumCount._2 + 1)));
          return Mono.just(map);
        });

    Mono<List<Double>> averageManaUsedPerTurn = sumAndCountPerTurn.map(map -> map
        .entrySet()
        .stream()
        .sorted(Entry.comparingByKey())
        .map(pair -> (double) pair.getValue()._1 / pair.getValue()._2)
        .collect(Collectors.toList()));

    return averageManaUsedPerTurn
        .map(map -> new ManaCurveReport(ImmutableList.copyOf(map)));
  }

  private int sumPlayedCmc(GameState state) {
    return state
        .getPlayed()
        .stream()
        .filter(Predicate.not(Card::isLand))
        .mapToInt(card -> ((Spell) card).getCmc())
        .sum();
  }
}
