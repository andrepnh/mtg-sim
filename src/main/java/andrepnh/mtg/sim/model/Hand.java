package andrepnh.mtg.sim.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.vavr.Tuple2;
import java.util.ArrayList;
import lombok.Value;
import org.apache.logging.log4j.util.Strings;

@Value
public class Hand {
  ImmutableList<? extends Card> cards;

  public Hand(ImmutableList<? extends Card> cards) {
    this.cards = cards;
  }

  public Hand(Card first, Card... rest) {
    this.cards = ImmutableList.copyOf(Lists.asList(first, rest));
  }

  public static Hand empty() {
    return new Hand(ImmutableList.of());
  }

  public Tuple2<Hand, Library> draw(Library library, int amount) {
    return library.draw(amount).map1(this::add);
  }

  public Hand add(ImmutableList<Card> cards) {
    return new Hand(ImmutableList.<Card>builder()
        .addAll(this.cards)
        .addAll(cards)
        .build());
  }

  public Hand remove(ImmutableList<Card> cardsToRemove) {
    var newHand = new ArrayList<>(cards);
    cardsToRemove.forEach(toRemove -> checkArgument(
        newHand.remove(toRemove),
        "Unable to remove card %s from hand, it was not found. Hand: %s",
        toRemove, cards));
    return new Hand(ImmutableList.copyOf(newHand));
  }

  public boolean contains(String cardName) {
    checkArgument(!Strings.isBlank(cardName), "cardName is mandatory");
    return cards.stream().anyMatch(card -> card.getName().equals(cardName));
  }
}
