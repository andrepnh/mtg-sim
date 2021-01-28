package andrepnh.mtg.sim.analytics.test;

import static andrepnh.mtg.sim.model.Mana.*;

import andrepnh.mtg.sim.model.BasicLand;
import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.model.ManaCost;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import java.util.Collections;

public class Decks {
  public static final Deck TEST_DECK = new Deck(
      "Test deck",
      ImmutableList.<Card>builder()
          .addAll(Collections.nCopies(15, BasicLand.MOUNTAIN))
          .addAll(Collections.nCopies(4, Spells.FIREBALL))
          .addAll(Collections.nCopies(4, new Spell("Lightning Bolt", ManaCost.of(0, R))))
          .addAll(Collections.nCopies(4, new Spell("Incinerate", ManaCost.of(1, R))))
          .addAll(Collections.nCopies(4, new Spell("Pillage", ManaCost.of(1, R, R))))
          .addAll(Collections.nCopies(4, new Spell("Obliterate", ManaCost.of(6, R, R))))
          .addAll(Collections.nCopies(4, new Spell("Thunderous Wrath", ManaCost.of(4, R, R))))
          .add(new Spell("Mons's Goblin Raiders", ManaCost.of(0, R)))
          .build());
}
