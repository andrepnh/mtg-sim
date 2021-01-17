package andrepnh.mtg.sim.in;

import andrepnh.mtg.sim.Main;
import andrepnh.mtg.sim.model.Deck;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Data
@Slf4j
public class FileSystemDeckImporter {
  private static final String DECK_EXTENSION = ".deck";
  private final DeckImporter delegate;

  static {
    try {
      Files.createDirectories(Main.INPUT_DIR);
    } catch (IOException e) {
      log.error("Exception when trying to create directory {}", Main.INPUT_DIR, e);
      throw new IllegalStateException(e);
    }
  }

  @SneakyThrows
  public int countDecks() {
    return (int) Files.list(Main.INPUT_DIR).count();
  }

  @SneakyThrows
  public Flux<Deck> importDecks() {
    var fileNames = Files.list(Main.INPUT_DIR)
        .filter(path -> path.toFile().getName().endsWith(DECK_EXTENSION))
        .collect(Collectors.toList());
    Flux<Deck> decks = Flux.fromIterable(fileNames)
        .map(deckFile -> delegate.importDeck(extractDeckName(deckFile), read(deckFile)))
        .filter(Try::isSuccess)
        .map(Try::get)
        .cache(fileNames.size());
    return decks;
  }

  private String extractDeckName(Path deckFile) {
    String fileName = deckFile.getFileName().toString();
    int extensionStart = fileName.lastIndexOf('.');
    return fileName.substring(0, extensionStart == -1 ? fileName.length() : extensionStart);
  }

  @SneakyThrows
  private List<String> read(Path deckFile) {
    log.debug("Importing deck {}", deckFile);
    return Files.readAllLines(deckFile);
  }
}
