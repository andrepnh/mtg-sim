package andrepnh.mtg.sim.sim;

import andrepnh.mtg.sim.model.Battlefield;
import andrepnh.mtg.sim.model.Card;
import com.google.common.collect.ImmutableList;
import lombok.Value;

@Value
public class Event {
  int turn;
  Battlefield battlefield;
  ImmutableList<Card> drawn;
  ImmutableList<Card> played;
}
