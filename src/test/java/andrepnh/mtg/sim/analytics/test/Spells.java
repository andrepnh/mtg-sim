package andrepnh.mtg.sim.analytics.test;

import static andrepnh.mtg.sim.model.Mana.*;

import andrepnh.mtg.sim.model.ManaCost;
import andrepnh.mtg.sim.model.Spell;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Spells {
  public final Spell OW = new Spell("Ow", ManaCost.of(0, B));
  public final Spell FIREBALL = new Spell("Fireball", ManaCost.withX(1, 0, R));
  public final Spell INFERNO = new Spell("Inferno", ManaCost.of(5, R, R));

  public Spell creatureWithCost(ManaCost cost) {
    return new Spell("Creature " + cost.cmc(), cost);
  }
}

