package andrepnh.mtg.sim.analytics.synergy;

import andrepnh.mtg.sim.Main;
import andrepnh.mtg.sim.analytics.synergy.count.SynergyPredicate;
import andrepnh.mtg.sim.analytics.synergy.count.Tokenizer;
import andrepnh.mtg.sim.model.Deck;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

@Slf4j
public class SynergyInputImporter {
  private static final String SYNERGY_FILE_EXTENSION = ".synergies";
  private final Tokenizer tokenizer = new Tokenizer();



  public ImmutableList<SynergyPredicate> forDeck(Deck deck) {
    Path synergiesFile = Main.INPUT_DIR.resolve(deck.getName() + SYNERGY_FILE_EXTENSION);
    return Optional.of(synergiesFile)
        .filter(path -> Files.exists(path))
        .map(this::read)
        .map(synergies -> synergies
            .stream()
            .map(this::parse)
            .collect(ImmutableList.toImmutableList()))
        .orElse(ImmutableList.of());
  }

  private SynergyPredicate parse(String raw) {
    return SynergyPredicate.of(raw, tokenizer.extractTokens(raw));
  }

  private List<String> read(Path path) {
    try {
      return Files.readAllLines(path).stream()
          .filter(Predicate.not(Strings::isBlank))
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error(
          "Could not read synergies file {}; skipping synergy analysis for the corresponding deck",
          path, e);
      return Collections.emptyList();
    }
  }
}
