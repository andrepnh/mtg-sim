package andrepnh.mtg.sim.sim;

import static andrepnh.mtg.sim.util.Util.cat;

import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.Mana;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import lombok.Value;

@Value
public class Line {
  private static final Line EMPTY
      = new Line(ImmutableList.of(), ImmutableList.of(), ImmutableList.of());

  ImmutableList<Land> landsPlayed;

  ImmutableList<Spell> spellsPlayed;

  ImmutableList<Mana> manaUsed;

  ImmutableList<Card> played;
  public Line(ImmutableList<Land> landsPlayed,
      ImmutableList<Spell> spellsPlayed,
      ImmutableList<Mana> manaUsed) {
    this.landsPlayed = landsPlayed;
    this.spellsPlayed = spellsPlayed;
    this.manaUsed = manaUsed;
    this.played = cat(landsPlayed, spellsPlayed);
  }

  public static Line empty() {
    return EMPTY;
  }
}
