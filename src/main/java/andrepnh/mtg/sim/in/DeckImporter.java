package andrepnh.mtg.sim.in;

import andrepnh.mtg.sim.card.api.ClientFacade;
import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Deck;
import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import java.util.Collections;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class DeckImporter {
  private static final Pattern CARD_PATTERN = Pattern.compile("^(\\d+)\\s+(.*?)\\s*(?:\\(.*?\\)\\s*\\d+)?$");

  private final ClientFacade client;

  public Try<Deck> importDeck(String deckName, Iterable<String> deckString) {
    var builder = ImmutableList.<Card>builder();
    for (String line: deckString) {
      var matcher = CARD_PATTERN.matcher(line);
      if (matcher.find()) {
        var amount = Integer.parseInt(matcher.group(1));
        String cardName = matcher.group(2);
        Try<Card> card = client.findByName(cardName);
        if (card.isFailure()) {
          log.error("Could not get card {} for deck {}; it will be ignored",
              cardName, deckName, card.getCause());
          return Try.failure(card.getCause());
        }
        builder.addAll(Collections.nCopies(amount, card.get()));
      }

      if (line.trim().equalsIgnoreCase("sideboard")) {
        break;
      }
    }

    return Try.success(new Deck(deckName, builder.build()));
  }
}
