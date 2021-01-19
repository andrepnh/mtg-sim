package andrepnh.mtg.sim.analytics.mana;

import andrepnh.mtg.sim.analytics.test.Decks;
import andrepnh.mtg.sim.analytics.test.GameStates;
import andrepnh.mtg.sim.model.BasicLand;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AvailableManaAnalysisTest {
  private final AvailableManaAnalysis analysis = new AvailableManaAnalysis();

  @Test
  void analyzeShouldCalculateAverageAvailableManaPerTurn() {
    var gameStates = Flux
        .just(
            GameStates.turnCompletelyEmpty(0),
            GameStates.turnCompletelyEmpty(0),
            GameStates.turnWithOnlyLandsOnBattlefield(1, BasicLand.FOREST),
            GameStates.turnWithOnlyLandsOnBattlefield(1, BasicLand.FOREST, BasicLand.ISLAND, BasicLand.MOUNTAIN),
            GameStates.turnWithOnlyLandsOnBattlefield(2, BasicLand.PLAINS, BasicLand.PLAINS),
            GameStates.turnWithOnlyLandsOnBattlefield(2, BasicLand.SWAMP, BasicLand.SWAMP, BasicLand.SWAMP))
        .parallel();
    var expectedReport = new AvailableManaReport(ImmutableList.of(0., 2., 2.5));

    Mono<AvailableManaReport> report = analysis.analyze(Decks.TEST_DECK, gameStates);

    StepVerifier.create(report)
        .expectNext(expectedReport)
        .expectComplete()
        .verify(Duration.ofSeconds(1));
  }

  @Test
  void analysisShouldIgnoreLandsPlayedAndConsiderOnlyLandsOnBattlefield() {
    var gameStates = Flux
        .just(
            GameStates.turnCompletelyEmpty(0),
            GameStates.turnWithOneLandOnBattlefieldAndOnePlayed(1, BasicLand.FOREST, BasicLand.PLAINS))
        .parallel();
    var expectedReport = new AvailableManaReport(ImmutableList.of(0., 1.));

    Mono<AvailableManaReport> report = analysis.analyze(Decks.TEST_DECK, gameStates);

    StepVerifier.create(report)
        .expectNext(expectedReport)
        .expectComplete()
        .verify(Duration.ofSeconds(1));
  }

}