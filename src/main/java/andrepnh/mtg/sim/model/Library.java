package andrepnh.mtg.sim.model;

import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Value;

@Value
public class Library {
  ImmutableList<Card> cards;

  public Tuple2<ImmutableList<Card>, Library> draw(int amount) {
    ImmutableList<Card> drawn = cards
        .stream()
        .limit(amount)
        .collect(ImmutableList.toImmutableList());
    ImmutableList<Card> remaining = cards
        .stream()
        .skip(amount)
        .collect(ImmutableList.toImmutableList());
    return Tuple.of(drawn, new Library(remaining));
  }
}
