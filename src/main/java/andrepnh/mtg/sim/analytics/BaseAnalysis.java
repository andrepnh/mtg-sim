package andrepnh.mtg.sim.analytics;

import static com.google.common.base.Preconditions.checkState;

import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.sim.GameState;
import andrepnh.mtg.sim.util.EitherFlux;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class BaseAnalysis<T extends Report> implements Analysis<T> {
  @Override
  public final Mono<T> analyze(Deck deck, EitherFlux<GameState> gameStates) {
    checkState(appliesTo(deck),
        "Cannot run analysis %s, it doesn't apply to deck %s",
        getClass(), deck);
    return doAnalyze(gameStates);
  }

  protected abstract Mono<T> doAnalyze(EitherFlux<GameState> gameStates);
}
