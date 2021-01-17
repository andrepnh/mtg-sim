package andrepnh.mtg.sim.analytics.synergy.bool;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

  public List<Token> extractTokens(String text) {
    var tokens = new ArrayList<Token>();
    var currentToken = new StringBuilder();

    for (int c: (text + "\0").codePoints().toArray()) {
      char chr = (char) c;
      if (chr == '\0' && currentToken.length() != 0) {
        tokens.add(Token.card(currentToken.toString()));
      } else if (chr == ' ' && currentToken.length() != 0) {
        Tuple2<String, String> slices = sliceBack(currentToken, -4);
        if (slices._2.equals(" AND")) {
          if (!slices._1.isBlank()) {
            tokens.add(Token.card(slices._1));
          }
          tokens.add(Token.and());
          currentToken = new StringBuilder();
          continue;
        }
        slices = sliceBack(currentToken, -3);
        if (slices._2.equals(" OR")) {
          if (!slices._1.isBlank()) {
            tokens.add(Token.card(slices._1));
          }
          tokens.add(Token.or());
          currentToken = new StringBuilder();
          continue;
        }
        currentToken.append(chr);
      } else if (chr == '(') {
        if (currentToken.length() != 0) {
          tokens.add(Token.card(currentToken.toString()));
          currentToken = new StringBuilder();
        }
        tokens.add(Token.openParenthesis());
      } else if (chr == ')') {
        if (currentToken.length() != 0) {
          tokens.add(Token.card(currentToken.toString()));
          currentToken = new StringBuilder();
        }
        tokens.add(Token.closeParenthesis());
      } else {
        currentToken.append(chr);
      }
    }

    return tokens;
  }

  private Tuple2<String, String> sliceBack(StringBuilder sb, int position) {
    return Tuple.of(
        sb.substring(0, Math.max(0, Math.min(sb.length(), sb.length() + position))),
        sb.substring(Math.max(0, sb.length() + position)));
  }
}
