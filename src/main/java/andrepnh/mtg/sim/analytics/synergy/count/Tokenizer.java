package andrepnh.mtg.sim.analytics.synergy.count;

import static com.google.common.base.Preconditions.checkArgument;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Tokenizer {
  private static final Pattern TOKEN_PATTERN = Pattern.compile("\\s*(\\d)(=?)\\s*(.*?)(?:$|;)");

  public List<Tuple3<AmountToken, MatchType, CardToken>> extractTokens(String text) {
    var match = TOKEN_PATTERN.matcher(text);
    checkArgument(match.find(),
        "%s is not a valid synergy expression",
        text);
    match.reset();
    var tokens = new ArrayList<Tuple3<AmountToken, MatchType, CardToken>>();
    while (match.find()) {
      String amount = match.group(1),
          matchType = match.group(2),
          card = match.group(3);
      checkArgument(card != null && !card.isBlank(),
          "%s is missing the card name and is not a valid synergy",
          text);
      tokens.add(Tuple.of(
          new AmountToken(Integer.parseInt(amount)),
          Optional.ofNullable(matchType)
              .map(unused -> MatchType.EXACT)
              .orElse(MatchType.AT_LEAST),
          new CardToken(card)));
    }

    return tokens;
  }
}
