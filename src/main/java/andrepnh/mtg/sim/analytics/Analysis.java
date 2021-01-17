package andrepnh.mtg.sim.analytics;

import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.sim.GameState;
import andrepnh.mtg.sim.util.EitherFlux;
import reactor.core.publisher.Mono;

public interface Analysis<T extends Report> {
  default boolean appliesTo(Deck deck) {
    return true;
  }

  Mono<T> analyze(Deck deck, EitherFlux<GameState> gameStates);
}
