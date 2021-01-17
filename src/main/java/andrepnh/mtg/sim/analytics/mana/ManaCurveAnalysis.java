package andrepnh.mtg.sim.analytics.mana;

import andrepnh.mtg.sim.analytics.BaseAnalysis;
import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.model.Spell;
import andrepnh.mtg.sim.sim.GameState;
import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

@Slf4j
public class ManaCurveAnalysis extends BaseAnalysis<ManaCurveReport> {

  @Override
  protected Mono<ManaCurveReport> doAnalyze(Deck deck, ParallelFlux<GameState> gameStates) {
    // Not pretty, but we can't use anything like groupBy, cardinality is too big and the app stalls
    Mono<Map<Integer, Tuple2<Integer, Integer>>> sumAndCountPerTurn = Mono
        .defer(() -> {
          Map<Integer, Tuple2<Integer, Integer>> map = new ConcurrentHashMap<>();
          var latch = new CountDownLatch(1);
          gameStates.subscribe(
              state -> map
                  .compute(
                      state.getTurn(),
                      (t, sumCount) -> sumCount == null
                          ? Tuple.of(sumPlayedCmc(state), 1)
                          : Tuple.of(sumCount._1 + sumPlayedCmc(state), sumCount._2 + 1)),
              ex -> log.error("Error computing analysis for {}", deck, ex),
              latch::countDown);
          try {
            boolean success = latch.await(10, TimeUnit.SECONDS);
            if (!success) {
              log.error("Analysis timed out, results will be partial or empty for {}", deck);
            }
          } catch (InterruptedException e) {
            log.error("Analysis interrupted, results will be partial or empty for {}", deck);
          }
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
