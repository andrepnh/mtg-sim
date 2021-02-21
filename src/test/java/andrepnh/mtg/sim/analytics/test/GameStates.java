package andrepnh.mtg.sim.analytics.test;

import andrepnh.mtg.sim.model.BasicLand;
import andrepnh.mtg.sim.model.Battlefield;
import andrepnh.mtg.sim.model.Hand;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.Library;
import andrepnh.mtg.sim.model.Spell;
import andrepnh.mtg.sim.sim.GameState;
import andrepnh.mtg.sim.sim.Line;
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
        new Line(ImmutableList.of(played), ImmutableList.of(), ImmutableList.of()));
  }

  public static GameState turnCompletelyEmpty(int turn) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        new Battlefield(ImmutableList.of(), ImmutableList.of()),
        Line.empty());
  }

  public static GameState turnWithOnlyLandsOnBattlefield(int turn, Land first, Land... more) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        new Battlefield(ImmutableList.copyOf(Lists.asList(first, more)), ImmutableList.of()),
        Line.empty());
  }

  public static GameState turnWithOnlySpellsPlayed(int turn, Spell first, Spell... more) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        new Battlefield(ImmutableList.of(), ImmutableList.copyOf(Lists.asList(first, more))),
        new Line(
            ImmutableList.of(),
            ImmutableList.copyOf(Lists.asList(first, more)),
            ImmutableList.of()));
  }

  public static GameState turnWithOnlyCardsOnBattlefield(int turn, Spell first, Spell... more) {
    return new GameState(
        turn,
        Hand.empty(),
        Library.empty(),
        new Battlefield(ImmutableList.of(), ImmutableList.copyOf(Lists.asList(first, more))),
        Line.empty());
  }
}
