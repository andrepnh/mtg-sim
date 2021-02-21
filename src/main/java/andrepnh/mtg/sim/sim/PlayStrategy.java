package andrepnh.mtg.sim.sim;

import andrepnh.mtg.sim.model.Hand;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.Mana;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import io.vavr.Tuple2;
import io.vavr.Tuple3;

public interface PlayStrategy {
  Line choose(Hand hand, ImmutableList<Land> mana);
}
