package andrepnh.mtg.sim.sim;

import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.model.Library;

public interface Shuffler {
  Library shuffle(Deck deck);
}
