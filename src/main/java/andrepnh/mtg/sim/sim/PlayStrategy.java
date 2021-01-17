package andrepnh.mtg.sim.sim;

import andrepnh.mtg.sim.model.Hand;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import io.vavr.Tuple2;

public interface PlayStrategy {
  Tuple2<ImmutableList<Land>, ImmutableList<Spell>> choose(Hand hand, ImmutableList<Land> mana);
}
