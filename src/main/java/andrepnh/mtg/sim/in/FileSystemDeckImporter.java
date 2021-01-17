package andrepnh.mtg.sim.in;

import andrepnh.mtg.sim.Main;
import andrepnh.mtg.sim.jmh.ParallelStrategy;
import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.util.EitherFlux;
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
import reactor.core.scheduler.Schedulers;

@Data
@Slf4j
public class FileSystemDeckImporter {
  private static final String DECK_EXTENSION = ".deck";
  private final DeckImporter delegate;
  private final ParallelStrategy strategy;

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
  public EitherFlux<Deck> importDecks() {
    var fileNames = Files.list(Main.INPUT_DIR)
        .filter(path -> path.toFile().getName().endsWith(DECK_EXTENSION))
        .collect(Collectors.toList());
    Flux<Deck> sequential = Flux.fromIterable(fileNames)
        .map(deckFile -> delegate.importDeck(extractDeckName(deckFile), read(deckFile)))
        .filter(Try::isSuccess)
        .map(Try::get)
        .cache(fileNames.size());
    return strategy == ParallelStrategy.PARALLEL
        ? EitherFlux.of(sequential.parallel().runOn(Schedulers.parallel()))
        : EitherFlux.of(sequential);
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
