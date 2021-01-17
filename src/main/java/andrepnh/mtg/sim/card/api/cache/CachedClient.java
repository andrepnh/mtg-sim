package andrepnh.mtg.sim.card.api.cache;

import andrepnh.mtg.sim.card.api.Client;
import andrepnh.mtg.sim.card.api.RemoteCard;
import java.util.Optional;
import lombok.Data;

@Data
public class CachedClient {
  private final FileSystemCache cache;

  public <T extends RemoteCard> Optional<T> findByName(Client<T> delegate, String name, Class<T> cardClass) {
    return cache.get(name, delegate.getApiName(), () -> delegate.findByName(name), cardClass);
  }
}
