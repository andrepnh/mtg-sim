package andrepnh.mtg.sim.analytics.mana;

import andrepnh.mtg.sim.analytics.test.Decks;
import andrepnh.mtg.sim.analytics.test.GameStates;
import andrepnh.mtg.sim.analytics.test.Spells;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ManaCurveAnalysisTest {
  private final ManaCurveAnalysis analysis = new ManaCurveAnalysis();

  @Test
  void analyzeShouldCalculateAverageManaUsedPerTurn() {
    var gameStates = Flux
        .just(
            GameStates.turnCompletelyEmpty(0),
            GameStates.turnCompletelyEmpty(0),
            GameStates.turnWithOnlySpellsPlayed(1, Spells.FIREBALL),
            GameStates.turnWithOnlySpellsPlayed(1, Spells.OW, Spells.OW, Spells.OW),
            GameStates.turnWithOnlySpellsPlayed(2, Spells.FIREBALL),
            GameStates.turnWithOnlySpellsPlayed(2, Spells.OW, Spells.OW))
        .parallel();
    var expectedReport = new ManaCurveReport(ImmutableList.of(0., 2., 1.5));

    Mono<ManaCurveReport> report = analysis.analyze(Decks.TEST_DECK, gameStates);

    StepVerifier.create(report)
        .expectNext(expectedReport)
        .expectComplete()
        .verify(Duration.ofSeconds(1));
  }

  @Test
  void analysisShouldIgnoreCardsOnBattlefield() {
    var gameStates = Flux
        .just(
            GameStates.turnCompletelyEmpty(0),
            GameStates.turnWithOnlyCardsOnBattlefield(1, Spells.OW))
        .parallel();
    var expectedReport = new ManaCurveReport(ImmutableList.of(0., 0.));

    Mono<ManaCurveReport> report = analysis.analyze(Decks.TEST_DECK, gameStates);

    StepVerifier.create(report)
        .expectNext(expectedReport)
        .expectComplete()
        .verify(Duration.ofSeconds(1));
  }

}