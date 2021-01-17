package andrepnh.mtg.sim.card.api.cache.io.mtg;

import andrepnh.mtg.sim.card.api.Client;
import io.magicthegathering.javasdk.api.CardAPI;
import java.util.Collections;
import java.util.Optional;

public class IoMtgClient implements Client<IoMtgCard> {

  @Override
  public String getApiName() {
    return "io.mtg";
  }

  @Override
  public Optional<IoMtgCard> findByName(String name) {
    return CardAPI.getAllCards(Collections.singletonList("name=" + name))
        .stream()
        .findFirst()
        .map(IoMtgCard::new);
  }
}
