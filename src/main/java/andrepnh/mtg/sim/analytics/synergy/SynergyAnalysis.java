package andrepnh.mtg.sim.analytics.synergy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import andrepnh.mtg.sim.analytics.BaseAnalysis;
import andrepnh.mtg.sim.analytics.synergy.count.SynergyPredicate;
import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.sim.GameState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

@Slf4j
public class SynergyAnalysis extends BaseAnalysis<SynergyReport> {
  private final Deck deck;
  private final Flux<SynergyPredicate> predicates;
  private final int gamesSimulated;

  public SynergyAnalysis(Deck deck, ImmutableList<SynergyPredicate> predicates, int gamesSimulated) {
    checkArgument(!checkNotNull(predicates).isEmpty());
    this.deck = checkNotNull(deck);
    this.predicates = Flux.fromIterable(predicates);
    this.gamesSimulated = gamesSimulated;
  }

  @Override
  public boolean appliesTo(Deck deck) {
    return this.deck.equals(deck);
  }

  @Override
  protected Mono<SynergyReport> doAnalyze(Deck deck, ParallelFlux<GameState> gameStates) {
    Mono<ImmutableTable<String, Integer, Double>> percentagesPerTurnAndSynergy = gameStates
        .flatMap(
            gameState -> predicates
                .map(predicate -> Tuple.of(
                    predicate.getName(),
                    gameState.getTurn(),
                    predicate.test(gameState.getBattlefield()) ? 1 : 0)))
        .sequential()
        .groupBy(triple -> triple.apply((a, b, c) -> Tuple.of(a, b)))
        // Don't ask me why, but just making it a parallel flux, even without runOn, keeps the app
        // from stalling
        .parallel()
        .flatMap(
            countsByTurnAndPredicate -> countsByTurnAndPredicate
                .map(Tuple3::_3)
                .collect(Collectors.collectingAndThen(
                    Collectors.summingInt(i -> i),
                    sum -> round((double) sum / gamesSimulated * 100)))
                .map(sum -> Tuple.of(
                    countsByTurnAndPredicate.key()._1,
                    countsByTurnAndPredicate.key()._2,
                    sum)))
        .sequential()
        .collect(ImmutableTable.toImmutableTable(Tuple3::_1, Tuple3::_2, Tuple3::_3));
    return percentagesPerTurnAndSynergy.map(SynergyReport::fromSynergyPercentages);
  }

  private double round(double v) {
    return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
