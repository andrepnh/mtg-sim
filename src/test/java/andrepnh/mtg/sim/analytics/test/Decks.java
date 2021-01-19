package andrepnh.mtg.sim.analytics.test;

import andrepnh.mtg.sim.model.BasicLand;
import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import java.util.Collections;

public class Decks {
  public static final Deck TEST_DECK = new Deck(
      "Test deck",
      ImmutableList.<Card>builder()
          .addAll(Collections.nCopies(15, BasicLand.MOUNTAIN))
          .addAll(Collections.nCopies(4, Spells.FIREBALL))
          .addAll(Collections.nCopies(4, new Spell("Lightning Bolt", 1)))
          .addAll(Collections.nCopies(4, new Spell("Incinerate", 2)))
          .addAll(Collections.nCopies(4, new Spell("Pillage", 4)))
          .addAll(Collections.nCopies(4, new Spell("Obliterate", 8)))
          .addAll(Collections.nCopies(4, new Spell("Thunderous Wrath", 6)))
          .add(new Spell("Mons's Goblin Raiders", 1))
          .build());
}
