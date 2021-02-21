package andrepnh.mtg.sim.sim;

import static andrepnh.mtg.sim.model.BasicLand.FOREST;
import static andrepnh.mtg.sim.model.BasicLand.ISLAND;
import static andrepnh.mtg.sim.model.BasicLand.MOUNTAIN;
import static andrepnh.mtg.sim.model.BasicLand.PLAINS;
import static andrepnh.mtg.sim.model.BasicLand.SWAMP;
import static andrepnh.mtg.sim.model.BicolorHybridMana.BR;
import static andrepnh.mtg.sim.model.BicolorHybridMana.WG;
import static andrepnh.mtg.sim.model.Mana.B;
import static andrepnh.mtg.sim.model.Mana.G;
import static andrepnh.mtg.sim.model.Mana.U;
import static andrepnh.mtg.sim.model.Mana.W;
import static andrepnh.mtg.sim.model.MonocoloredHybridMana._2G;
import static org.assertj.core.api.Assertions.assertThat;

import andrepnh.mtg.sim.analytics.test.Spells;
import andrepnh.mtg.sim.model.Hand;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.ManaCost;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

class SimplePlayStrategyTest {
  @Test
  void shouldOnlyPickSpellsThatCanBePlayedWithAvailableMana() {
    Spell oneColorless = Spells.creatureWithCost(ManaCost.of(1)),
        oneGreen = Spells.creatureWithCost(ManaCost.of(0, G)),
        oneWhite = Spells.creatureWithCost(ManaCost.of(0, W)),
        blackOrRed = Spells.creatureWithCost(ManaCost.ofHybrid(0, BR)),
        oneBlack = Spells.creatureWithCost(ManaCost.of(0, B)),
        _2colorlessOrGreen = Spells.creatureWithCost(ManaCost.ofHybrid(0, _2G));

    Line chosen = SimplePlayStrategy
        .INSTANCE
        .choose(new Hand(oneColorless, oneGreen), ImmutableList.of());
    assertThat(chosen.getSpellsPlayed()).isEmpty();
    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(oneColorless, oneWhite), ImmutableList.of(FOREST));
    assertThat(chosen.getSpellsPlayed()).containsExactly(oneColorless);

    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(oneGreen, oneWhite), ImmutableList.of(MOUNTAIN));
    assertThat(chosen.getSpellsPlayed()).isEmpty();

    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(blackOrRed), ImmutableList.of(FOREST));
    assertThat(chosen.getSpellsPlayed()).isEmpty();
    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(blackOrRed, oneBlack), ImmutableList.of(MOUNTAIN));
    assertThat(chosen.getSpellsPlayed()).containsExactly(blackOrRed);

    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(_2colorlessOrGreen), ImmutableList.of(MOUNTAIN));
    assertThat(chosen.getSpellsPlayed()).isEmpty();
    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(_2colorlessOrGreen), ImmutableList.of(MOUNTAIN, PLAINS));
    assertThat(chosen.getSpellsPlayed()).containsExactly(_2colorlessOrGreen);
    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(_2colorlessOrGreen), ImmutableList.of(FOREST));
    assertThat(chosen.getSpellsPlayed()).containsExactly(_2colorlessOrGreen);
  }

  @Test
  void shouldPickLandThatHelpsPlayingCards() {
    Spell oneGreen = Spells.creatureWithCost(ManaCost.of(0, G)),
        oneWhite = Spells.creatureWithCost(ManaCost.of(0, W)),
        twoWhite = Spells.creatureWithCost(ManaCost.of(0, W, W)),
        oneWhiteOrGreen = Spells.creatureWithCost(ManaCost.ofHybrid(0, WG)),
        _2colorlessOrGreen = Spells.creatureWithCost(ManaCost.ofHybrid(0, _2G));

    var chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(oneGreen, PLAINS, FOREST, PLAINS), ImmutableList.of());
    assertThat(chosen.getLandsPlayed()).containsExactly(FOREST);

    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(_2colorlessOrGreen, PLAINS, FOREST, PLAINS), ImmutableList.of());
    assertThat(chosen.getLandsPlayed()).containsExactly(FOREST);

    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(oneWhiteOrGreen, MOUNTAIN, FOREST, ISLAND, SWAMP, PLAINS), ImmutableList.of());
    assertThat(chosen.getLandsPlayed()).containsAnyOf(FOREST, PLAINS);

    chosen = SimplePlayStrategy.INSTANCE
        .choose(new Hand(twoWhite, oneWhite, oneGreen, FOREST, PLAINS), ImmutableList.of(PLAINS));
    assertThat(chosen.getLandsPlayed()).containsExactly(FOREST);
    assertThat(chosen.getSpellsPlayed()).containsExactlyInAnyOrder(oneGreen, oneWhite);
  }

  @Test
  void shouldIncludeCostsOfCardsPlayed() {
    Spell first = Spells.creatureWithCost(ManaCost.ofHybrid(0, _2G)),
        second = Spells.creatureWithCost(ManaCost.of(2, W, W));
    var hand = new Hand(first, second);
    var availableMana = ImmutableList
        .<Land>of(PLAINS, PLAINS, ISLAND, ISLAND, ISLAND, ISLAND, ISLAND, ISLAND);
    var expectedManaUsed = ImmutableList.of(W, W, U, U, U, U);

    var chosen = SimplePlayStrategy.INSTANCE
        .choose(hand, availableMana);

    assertThat(chosen.getLandsPlayed()).isEmpty();
    assertThat(chosen.getSpellsPlayed()).containsExactlyInAnyOrder(first, second);
    assertThat(chosen.getManaUsed()).containsExactlyInAnyOrderElementsOf(expectedManaUsed);
  }

  @Test
  void shouldNotFavorCardsWithXIfThereIsAnOptionWithHigherCmc() {
    Spell first = Spells.creatureWithCost(ManaCost.withX(1, 0, G)),
        second = Spells.creatureWithCost(ManaCost.of(0, G, G));
    var hand = new Hand(first, second);
    var availableMana = ImmutableList.<Land>of(FOREST, FOREST);
    var expectedManaUsed = ImmutableList.of(G, G);

    var chosen = SimplePlayStrategy.INSTANCE
        .choose(hand, availableMana);

    assertThat(chosen.getLandsPlayed()).isEmpty();
    assertThat(chosen.getSpellsPlayed()).containsExactly(second);
    assertThat(chosen.getManaUsed()).containsExactlyInAnyOrderElementsOf(expectedManaUsed);
  }

  @Test
  void shouldNotFavorCardsWithXIfAllOtherOptionsHaveSameCmcAndXWouldBeZero() {
    // Using as many spells with X as possible to ensure we were not lucky
    Spell withX1 = Spells.creatureWithCost(ManaCost.withX(1, 3)),
        withX2 = Spells.creatureWithCost(ManaCost.withX(1, 2, W)),
        withX3 = Spells.creatureWithCost(ManaCost.withX(1, 2, G)),
        withX4 = Spells.creatureWithCost(ManaCost.withX(1, 1, W, G)),
        withX5 = Spells.creatureWithCost(ManaCost.withX(1, 0, W, W, G)),
        spellWithoutX = Spells.creatureWithCost(ManaCost.of(1, W, G));
    var hand = new Hand(withX1, withX2, withX3, spellWithoutX, withX4, withX5);
    var availableMana = ImmutableList.<Land>of(PLAINS, PLAINS, FOREST);
    var expectedManaUsed = ImmutableList.of(W, W, G);

    var chosen = SimplePlayStrategy.INSTANCE
        .choose(hand, availableMana);

    assertThat(chosen.getLandsPlayed()).isEmpty();
    assertThat(chosen.getSpellsPlayed()).containsExactly(spellWithoutX);
    assertThat(chosen.getManaUsed()).containsExactlyInAnyOrderElementsOf(expectedManaUsed);
  }

  @Test
  void shouldFavorCardWithXIfItHadHigherCmcAndXIsNotZero() {
    // Using as many spells as possible to ensure we were not lucky
    Spell withX = Spells.creatureWithCost(ManaCost.withX(1, 1, W, G)),
        spell1 = Spells.creatureWithCost(ManaCost.of(3)),
        spell2 = Spells.creatureWithCost(ManaCost.of(2, G)),
        spell3 = Spells.creatureWithCost(ManaCost.of(2, W)),
        spell4 = Spells.creatureWithCost(ManaCost.of(1, G, W)),
        spell5 = Spells.creatureWithCost(ManaCost.of(1, W, W)),
        spell6 = Spells.creatureWithCost(ManaCost.of(0, W, W, G));
    var hand = new Hand(withX, spell1, spell2, spell3, spell4, spell5, spell6);
    var availableMana = ImmutableList.<Land>of(PLAINS, PLAINS, PLAINS, FOREST, FOREST);
    var expectedManaUsed = ImmutableList.of(W, W, W, G, G);

    var chosen = SimplePlayStrategy.INSTANCE
        .choose(hand, availableMana);

    assertThat(chosen.getLandsPlayed()).isEmpty();
    assertThat(chosen.getSpellsPlayed()).containsExactly(withX);
    assertThat(chosen.getManaUsed()).containsExactlyInAnyOrderElementsOf(expectedManaUsed);
  }

  @Test
  void shouldFavorCardsThatUseMoreMana() {
    Spell cmc1 = Spells.creatureWithCost(ManaCost.of(1)),
        cmc3 = Spells.creatureWithCost(ManaCost.of(3));
    var hand = new Hand(cmc1, cmc1, cmc1, cmc3);

    var chosen = SimplePlayStrategy
        .INSTANCE
        .choose(hand, ImmutableList.of(FOREST, FOREST, FOREST));
    assertThat(chosen.getLandsPlayed()).isEmpty();
    assertThat(chosen.getSpellsPlayed()).containsExactly(cmc1, cmc1, cmc1);
  }

  @Test
  void shouldPlayAsManyCardsAsPossibleThatUseMostMana() {
    Spell cmc1 = Spells.creatureWithCost(ManaCost.of(1)),
        cmc2 = Spells.creatureWithCost(ManaCost.of(2));
    var hand = new Hand(cmc1, cmc2);

    var chosen = SimplePlayStrategy
        .INSTANCE
        .choose(hand, ImmutableList.of(FOREST, FOREST));
    assertThat(chosen.getLandsPlayed()).isEmpty();
    assertThat(chosen.getSpellsPlayed()).containsExactly(cmc2);
  }

  @Test
  void shouldPlayAtMostOneMana() {
    var noManaHand = Hand.empty();
    var twoManaHand = new Hand(FOREST, FOREST);

    var chosen = SimplePlayStrategy
        .INSTANCE
        .choose(noManaHand, ImmutableList.of());
    assertThat(chosen.getLandsPlayed()).isEmpty();
    assertThat(chosen.getSpellsPlayed()).isEmpty();

    chosen = SimplePlayStrategy.INSTANCE
        .choose(twoManaHand, ImmutableList.of());
    assertThat(chosen.getLandsPlayed()).containsExactly(FOREST);
    assertThat(chosen.getSpellsPlayed()).isEmpty();
  }

}