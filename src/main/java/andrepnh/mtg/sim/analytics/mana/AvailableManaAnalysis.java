package andrepnh.mtg.sim.analytics.mana;

import andrepnh.mtg.sim.analytics.BaseAnalysis;
import andrepnh.mtg.sim.model.Deck;
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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

@Slf4j
public class AvailableManaAnalysis extends BaseAnalysis<AvailableManaReport> {

  @Override
  protected Mono<AvailableManaReport> doAnalyze(Deck deck, ParallelFlux<GameState> gameStates) {
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
                          ? Tuple.of(state.getLands().size(), 1)
                          : Tuple.of(sumCount._1 + state.getLands().size(), sumCount._2 + 1)),
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

    Mono<List<Double>> averageManaPerTurn = sumAndCountPerTurn.map(map -> map
        .entrySet()
        .stream()
        .sorted(Entry.comparingByKey())
        .map(pair -> (double) pair.getValue()._1 / pair.getValue()._2)
        .collect(Collectors.toList()));

    return averageManaPerTurn.map(map -> new AvailableManaReport(ImmutableList.copyOf(map)));
  }
}
