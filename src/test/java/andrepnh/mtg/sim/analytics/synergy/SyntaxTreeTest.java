//package andrepnh.mtg.sim.analytics.synergy;
//
//import java.util.Arrays;
//import org.junit.Test;
//
//public class SyntaxTreeTest {
//  @Test
//  public void shouldParseTokens() {
//    var tokens = Arrays.asList(Token.card("a"), Token.and(), Token.card("b"));
//    var tree = SynergyPredicate.of(tokens);
//  }
//
//  @Test
//  public void shouldParseTokens2() {
//    var tokens = Arrays.asList(Token.card("a"), Token.and(), Token.card("b"), Token.or(), Token.card("c"));
//    var tree = SynergyPredicate.of(tokens);
//  }
//
//  @Test
//  public void shouldParseTokens3() {
//    var tokens = Arrays.asList(
//        Token.card("a"), Token.and(),
//        Token.openParenthesis(),
//            Token.card("b"), Token.or(),
//            Token.openParenthesis(),
//                Token.card("c"), Token.or(), Token.card("d"),
//            Token.closeParenthesis(),
//        Token.closeParenthesis(),
//        Token.and(), Token.card("e"));
//    var tree = SynergyPredicate.of(tokens);
//  }
//
//  @Test
//  public void shouldParseTokens4() {
//    var tokens = Arrays.asList(
//        Token.openParenthesis(), Token.card("a"), Token.or(), Token.card("b"), Token.closeParenthesis(),
//        Token.and(),
//        Token.openParenthesis(), Token.card("c"), Token.or(), Token.card("d"), Token.closeParenthesis());
//    var tree = SynergyPredicate.of(tokens);
//  }
//}