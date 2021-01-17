package andrepnh.mtg.sim.card.api;

import andrepnh.mtg.sim.model.Card;
import java.util.Optional;

public interface Client<T extends RemoteCard> {
  String getApiName();

  Optional<T> findByName(String name);

  default T find(String name) {
    return findByName(name)
        .orElseThrow(() -> new IllegalStateException(String.format(
            "API %s returned no card with name %s",
            getClass(), name)));
  }
}
