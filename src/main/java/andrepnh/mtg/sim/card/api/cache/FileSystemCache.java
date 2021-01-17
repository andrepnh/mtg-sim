package andrepnh.mtg.sim.card.api.cache;

import andrepnh.mtg.sim.card.api.RemoteCard;
import com.google.gson.Gson;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class FileSystemCache {
  private static final Path CACHE_LOCATION = Path.of(
      System.getProperty("user.dir"),
      ".mtg-sim-cache");

  private final Gson gson;

  public <T extends RemoteCard> Optional<T> get(
      String card, String api, Supplier<Optional<T>> ifAbsent, Class<T> cardClass) {
    var filePath = filePath(card, api);
    if (Files.exists(filePath)) {
      Optional<String> rawCard = read(filePath);
      var cachedCard = rawCard.map(raw -> deserialize(raw, cardClass));
      if (cachedCard.isPresent()) {
        return cachedCard;
      }
    }
    Optional<T> supplied = ifAbsent.get();
    supplied.ifPresent(cardObject -> write(filePath, cardObject));
    return supplied;
  }

  private <T> void write(Path filePath, T card) {
    String json = gson.toJson(card);

    try {
      var fileStream = new FileOutputStream(filePath.toFile());
      try (var outputStream = new BufferedOutputStream(fileStream);
          var lock = fileStream.getChannel().lock()) {
        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      log.error("Could not write the following json to the file {}, the card won't be cached: {}",
          filePath, json, e);
    }
  }

  private <T> T deserialize(String rawCard, Class<T> cardClass) {
    return gson.fromJson(rawCard, cardClass);
  }

  private Optional<String> read(Path filePath) {
    try {
      return Optional.of(Files.readString(filePath));
    } catch (IOException e) {
      log.error("Could not read cache for card {}; cache will be ignored", filePath);
      return Optional.empty();
    }
  }

  private Path filePath(String card, String api) {
    return CACHE_LOCATION.resolve(Path.of(api, deClutter(card)));
  }

  private String deClutter(String card) {
    return card.replace(" ", "_")
        .replace(",", "")
        .replace("'", "");
  }
}
