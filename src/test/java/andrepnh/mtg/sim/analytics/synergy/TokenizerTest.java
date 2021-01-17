package andrepnh.mtg.sim.analytics.synergy;


import static org.assertj.core.api.Assertions.assertThat;

import andrepnh.mtg.sim.analytics.synergy.bool.Token;
import andrepnh.mtg.sim.analytics.synergy.bool.Tokenizer;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

class TokenizerTest {

  @Test
  void shouldTokenizeCommonCases() {
    assertThat(new Tokenizer().extractTokens("Aboroth AND Fireball"))
        .isEqualTo(Lists.newArrayList(
            Token.card("Aboroth"),
            Token.and(),
            Token.card("Fireball")));

    assertThat(new Tokenizer().extractTokens("Aboroth AND Fireball OR Counterspell"))
        .isEqualTo(Lists.newArrayList(
            Token.card("Aboroth"),
            Token.and(),
            Token.card("Fireball"),
            Token.or(),
            Token.card("Counterspell")));

    assertThat(new Tokenizer().extractTokens("(Me OR I)"))
        .isEqualTo(Lists.newArrayList(
            Token.openParenthesis(),
            Token.card("Me"),
            Token.or(),
            Token.card("I"),
            Token.closeParenthesis()));
  }

  @Test
  void shouldTokenizeUnusualCardNames() {
    assertThat(
        new Tokenizer().extractTokens(
            "Zzzyxas's Abyss AND Niv-Mizzet, the Firemind OR Kaboom! AND Evil Eye of Orms-by-Gore"))
        .isEqualTo(Lists.newArrayList(
            Token.card("Zzzyxas's Abyss"),
            Token.and(),
            Token.card("Niv-Mizzet, the Firemind"),
            Token.or(),
            Token.card("Kaboom!"),
            Token.and(),
            Token.card("Evil Eye of Orms-by-Gore")));
  }

  @Test
  void shouldTokenizeNestedParenthesis() {
    assertThat(new Tokenizer().extractTokens("0 OR (A AND (B OR C AND (D AND (E OR F) AND G AND H))) AND 9"))
        .isEqualTo(Lists.newArrayList(
            Token.card("0"),
            Token.or(),
            Token.openParenthesis(),
            Token.card("A"),
            Token.and(),
            Token.openParenthesis(),
            Token.card("B"),
            Token.or(),
            Token.card("C"),
            Token.and(),
            Token.openParenthesis(),
            Token.card("D"),
            Token.and(),
            Token.openParenthesis(),
            Token.card("E"),
            Token.or(),
            Token.card("F"),
            Token.closeParenthesis(),
            Token.and(),
            Token.card("G"),
            Token.and(),
            Token.card("H"),
            Token.closeParenthesis(),
            Token.closeParenthesis(),
            Token.closeParenthesis(),
            Token.and(),
            Token.card("9")));
  }
}