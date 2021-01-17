package andrepnh.mtg.sim.card.api.cache.io.mtg;

import andrepnh.mtg.sim.card.api.RemoteCard;
import andrepnh.mtg.sim.model.BasicLand;
import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.NonBasicLand;
import andrepnh.mtg.sim.model.Spell;
import java.util.Arrays;
import lombok.Value;

@Value
public class IoMtgCard implements RemoteCard {
  io.magicthegathering.javasdk.resource.Card card;

  @Override
  public Card toCard() {
    if (card.getType().contains("Basic Land")) {
      return BasicLand.ofName(card.getName());
    } else if (contains(card.getTypes(), "Land")) {
      return new NonBasicLand(card.getName());
    }
    return new Spell(card.getName(), (int) card.getCmc());
  }

  private boolean contains(String[] types, String type) {
    return Arrays.asList(types).contains(type);
  }
}
