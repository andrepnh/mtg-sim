package andrepnh.mtg.sim.analytics;

import static com.google.common.base.Preconditions.checkState;

import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.sim.GameState;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

@Slf4j
public abstract class BaseAnalysis<T extends Report> implements Analysis<T> {
  @Override
  public final Mono<T> analyze(Deck deck, ParallelFlux<GameState> gameStates) {
    checkState(appliesTo(deck),
        "Cannot run analysis %s, it doesn't apply to deck %s",
        getClass(), deck);
    return doAnalyze(deck, gameStates);
  }

  protected abstract Mono<T> doAnalyze(Deck deck, ParallelFlux<GameState> gameStates);
}
