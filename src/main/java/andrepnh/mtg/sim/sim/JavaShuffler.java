package andrepnh.mtg.sim.sim;

import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.model.Library;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;

public class JavaShuffler implements Shuffler {

  @Override
  public Library shuffle(Deck deck) {
    var shuffled = new ArrayList<>(deck.getCards());
    Collections.shuffle(shuffled);
    return new Library(ImmutableList.copyOf(shuffled));
  }
}
