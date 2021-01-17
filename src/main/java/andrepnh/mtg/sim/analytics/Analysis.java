package andrepnh.mtg.sim.analytics;

import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.sim.GameState;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

public interface Analysis<T extends Report> {
  default boolean appliesTo(Deck deck) {
    return true;
  }

  Mono<T> analyze(Deck deck, ParallelFlux<GameState> gameStates);
}
