package andrepnh.mtg.sim.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public class Deck {
  String name;
  ImmutableList<Card> cards;

  public Deck(String name, ImmutableList<Card> cards) {
    checkArgument(!name.isBlank());
    this.name = name;
    checkArgument(checkNotNull(cards).size() >= 40);
    List<String> moreThan4Copies = cards.stream()
        .filter(Predicate.not(Card::isLand))
        .collect(Collectors.groupingBy(Card::getName))
        .values()
        .stream()
        .filter(copies -> copies.size() > 4)
        .map(copies -> copies.get(0).getName())
        .collect(Collectors.toList());
    checkArgument(moreThan4Copies.isEmpty(),
        "The following cards have more than 4 copies: %s",
        moreThan4Copies);
    this.cards = cards;
  }
}
