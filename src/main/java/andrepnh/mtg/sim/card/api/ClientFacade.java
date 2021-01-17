package andrepnh.mtg.sim.card.api;

import andrepnh.mtg.sim.card.api.cache.CachedClient;
import andrepnh.mtg.sim.card.api.cache.FileSystemCache;
import andrepnh.mtg.sim.card.api.cache.io.mtg.IoMtgCard;
import andrepnh.mtg.sim.card.api.cache.io.mtg.IoMtgClient;
import andrepnh.mtg.sim.model.Card;
import com.google.gson.Gson;
import io.vavr.control.Try;

public final class ClientFacade {
  private final IoMtgClient ioMtgClient;
  private final CachedClient cachedClient;

  public ClientFacade() {
    cachedClient = new CachedClient(new FileSystemCache(new Gson()));
    ioMtgClient = new IoMtgClient();
  }

  public Try<Card> findByName(String name) {
    return Try.ofSupplier(() -> cachedClient
        .findByName(ioMtgClient, name, IoMtgCard.class)
        .map(RemoteCard::toCard)
        .orElseThrow(() -> new IllegalStateException("No card found with name " + name)));
  }
}
