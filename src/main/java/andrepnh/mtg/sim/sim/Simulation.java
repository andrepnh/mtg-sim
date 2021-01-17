package andrepnh.mtg.sim.sim;

import andrepnh.mtg.sim.Main;
import andrepnh.mtg.sim.jmh.ParallelStrategy;
import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.model.Library;
import andrepnh.mtg.sim.util.EitherFlux;
import java.util.stream.Stream;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

@Value
@Slf4j
public class Simulation {
  Shuffler shuffler;
  Deck deck;
  PlayStrategy playStrategy;
  ParallelStrategy parallelStrategy;

  public EitherFlux<GameState> runFor(int games, int turns) {
    if (parallelStrategy == ParallelStrategy.NONE) {
      Flux<GameState> sequential = Flux.range(1, games)
          .flatMap(game -> {
            log.trace("{} - simulating game {}", deck.getName(), game);
            return runFor(turns);
          }, Runtime.getRuntime().availableProcessors())
          // The data doesn't consume much memory, so let's cache all of it
          .cache(games * turns);
      return EitherFlux.of(sequential);
    } else {
      int statesPerRail = (int) Math
          .round((double) games * turns / Runtime.getRuntime().availableProcessors());
      ParallelFlux<GameState> parallel = Flux.range(1, games)
          .parallel()
          .runOn(Schedulers.parallel())
          .flatMap(game -> {
            log.trace("{} - simulating game {}", deck.getName(), game);
            return runFor(turns);
          }).transformGroups(rail -> rail.cache(statesPerRail));
      return EitherFlux.of(parallel);
    }
  }

  private Flux<GameState> runFor(int turns) {
    Library library = shuffler.shuffle(deck);
    var stateStream = Stream
        .iterate(GameState.theOpening(library), previousState -> {
          if (previousState.getTurn() == 0) {
            Main.UNIQUE_LIBRARIES.add(previousState.getLibrary());
          }
          Main.GLOBAL_TURN_COUNTER.incrementAndGet();
          return previousState.next(playStrategy);
        })
        .limit(turns);

    return Flux
        .fromStream(stateStream)
        // TODO eliminate one cache call
        .cache(turns);
  }
}
