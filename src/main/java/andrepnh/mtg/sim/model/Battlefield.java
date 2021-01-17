package andrepnh.mtg.sim.model;

import static andrepnh.mtg.sim.util.Util.cat;

import com.google.common.collect.ImmutableList;
import lombok.Value;

@Value
public class Battlefield {
  ImmutableList<Land> lands;
  ImmutableList<Spell> spells;
  ImmutableList<Card> all;

  public Battlefield(ImmutableList<Land> lands,
      ImmutableList<Spell> spells) {
    this.lands = lands;
    this.spells = spells;
    this.all = cat(lands, spells);
  }

  public static Battlefield empty() {
    return new Battlefield(ImmutableList.of(), ImmutableList.of());
  }

  public boolean contains(String cardName) {
    return all.stream()
        .anyMatch(card -> card.getName().equals(cardName));
  }

  public int count(String cardName) {
    return (int) all.stream()
        .filter(card -> card.getName().equals(cardName))
        .count();
  }

  public Battlefield add(ImmutableList<Land> lands, ImmutableList<Spell> spells) {
    return new Battlefield(cat(this.lands, lands), cat(this.spells, spells));
  }

}
