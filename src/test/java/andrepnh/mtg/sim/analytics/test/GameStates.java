package andrepnh.mtg.sim.analytics.test;

import andrepnh.mtg.sim.model.BasicLand;
import andrepnh.mtg.sim.model.Battlefield;
import andrepnh.mtg.sim.model.Hand;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.Library;
import andrepnh.mtg.sim.model.Spell;
import andrepnh.mtg.sim.sim.GameState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class GameStates {
  public static GameState turnWithOneLandOnBattlefieldAndOnePlayed(
      int turn, BasicLand onBattleField, BasicLand played) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        new Battlefield(ImmutableList.of(onBattleField), ImmutableList.of()),
        ImmutableList.of(),
        ImmutableList.of(played));
  }

  public static GameState turnCompletelyEmpty(int turn) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        new Battlefield(ImmutableList.of(), ImmutableList.of()),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static GameState turnWithOnlyLandsOnBattlefield(int turn, Land first, Land... more) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        Battlefield.empty(),
        ImmutableList.of(),
        ImmutableList.of());
  }

  public static GameState turnWithOnlySpellsPlayed(int turn, Spell first, Spell... more) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        Battlefield.empty(),
        ImmutableList.of(),
        ImmutableList.copyOf(Lists.asList(first, more)));
  }

  public static GameState turnWithOnlyCardsOnBattlefield(int turn, Spell first, Spell... more) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        new Battlefield(ImmutableList.of(), ImmutableList.copyOf(Lists.asList(first, more))),
        ImmutableList.of(),
        ImmutableList.of());
  }
}
