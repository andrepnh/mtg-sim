package andrepnh.mtg.sim.analytics.synergy.bool;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Token {
  String value;
  Type type;

  public static Token card(String name) {
    return new Token(name.trim(), Type.CARD);
  }

  public static Token and() {
    return new Token("AND", Type.AND);
  }

  public static Token or() {
    return new Token("OR", Type.OR);
  }

  public static Token openParenthesis() {
    return new Token("(", Type.OPEN_PARENTHESIS);
  }

  public static Token closeParenthesis() {
    return new Token(")", Type.CLOSE_PARENTHESIS);
  }

  boolean isCard() {
    return type == Type.CARD;
  }

  boolean isAnd() {
    return type == Type.AND;
  }

  boolean isOr() {
    return type == Type.OR;
  }

  boolean isOpenParenthesis() {
    return type == Type.OPEN_PARENTHESIS;
  }

  boolean isCloseParenthesis() {
    return type == Type.CLOSE_PARENTHESIS;
  }

  public enum Type {
    CARD, AND, OR, OPEN_PARENTHESIS, CLOSE_PARENTHESIS;
  }
}
