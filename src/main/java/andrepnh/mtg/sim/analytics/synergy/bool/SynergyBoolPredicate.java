package andrepnh.mtg.sim.analytics.synergy.bool;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import andrepnh.mtg.sim.model.Battlefield;
import com.google.common.base.Suppliers;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A test to check if cards in the battlefield contain the synergy represented by the instance.
 * Created directly from parsed {@link Token}s.
 *
 * <p>This is not used anymore and is an older synergy format based on boolean logic. It cannot
 * represent synergy between multiple copies of cards, so it was replaced with {@link SynergyPredicate}.
 * Below are some example of the syntax that it uses, with each line being a synergy:
 *
 * Fervent Champion AND Rimrock Knight
 * Torbran, Thane of Red Fell AND Fireblade Charger
 * Embercleave AND Fervent Champion AND Fireblade Charger AND (Robber of the Rich OR Rimrock Knight)
 */
public class SynergyBoolPredicate implements Predicate<Battlefield> {
  @Getter
  private final String name;
  private final Node root;
  // Instances are effectively final once created, so it's ok to memoize the predicate
  private final Supplier<Predicate<Battlefield>> predicate = Suppliers.memoize(this::toPredicate);

  public SynergyBoolPredicate(String name, Node root) {
    checkArgument(!checkNotNull(name).isBlank());
    this.name = name;
    this.root = checkNotNull(root);
  }

  static SynergyBoolPredicate of(String name, List<Token> tokens) {
    return new SynergyBoolPredicate(name, rootNode(tokens));
  }

  private static Node rootNode(List<Token> tokens) {
    Node root = null;
    var it = tokens.iterator();
    while (it.hasNext()) {
      var token = it.next();
      Node node;
      if (token.isOpenParenthesis()) {
        node = rootNode(extractSubExpression(it));
      } else {
        node = toSimpleNode(token);
      }
      if (root == null) {
        root = node;
      } else if (!root.isOperator() || (root.left().isPresent() && root.right().isPresent())) {
        checkState(node.isOperator(),
            "Root node is full, but cannot introduce new root since it wouldn't be an operator."
                + "Current root: %s; new node: %s",
            root, node);
        var oldRoot = root;
        root = node;
        node.left = oldRoot;
      } else if (root.left().isEmpty()) {
        root.left = node;
      } else if (root.right().isEmpty()) {
        root.right = node;
      } else {
        checkState(false,
            "Cannot include node %s on tree rooted at %s; no room left",
            node, root);
      }
    }
    return root;
  }

  private static List<Token> extractSubExpression(Iterator<Token> it) {
    int nestingLevel = 1;
    var subExpression = new ArrayList<Token>();
    while (nestingLevel > 0 && it.hasNext()) {
      var token = it.next();
      if (token.isCloseParenthesis()) {
        nestingLevel--;
      } else {
        subExpression.add(token);
        if (token.isOpenParenthesis()) {
          nestingLevel++;
        }
      }
    }
    return subExpression;
  }

  private static Node toSimpleNode(Token token) {
    if (token.isCard()) {
      return new Name(token.getValue());
    } else if (token.isAnd()) {
      return new And();
    } else if (token.isOr()) {
      return new Or();
    } else {
      throw new IllegalStateException("Unexpected token type " + token);
    }
  }

  @Override
  public boolean test(Battlefield battlefield) {
    return predicate.get().test(battlefield);
  }

  private Predicate<Battlefield> toPredicate() {
    return toPredicate(root);
  }

  private Predicate<Battlefield> toPredicate(Node node) {
    if (!node.isOperator()) {
      return battlefield -> battlefield.contains(((Name) node).name);
    } else {
      if (node instanceof And) {
        return toPredicate(node.left).and(toPredicate(node.right));
      } else if (node instanceof Or) {
        return toPredicate(node.left).or(toPredicate(node.right));
      } else {
        throw new IllegalStateException("Unknown node type " + node);
      }
    }
  }

  @ToString
  @RequiredArgsConstructor
  private static class Name extends Node {
    final String name;
  }

  private abstract static class Op extends Node {

  }

  @ToString
  private static class And extends Op {

  }

  @ToString
  private static class Or extends Op {

  }

  @NoArgsConstructor
  @AllArgsConstructor
  private static abstract class Node {
    Node left;
    Node right;

    boolean isOperator() {
      return this instanceof Op;
    }
    public Optional<Node> left() {
      return Optional.ofNullable(left);
    }

    public Optional<Node> right() {
      return Optional.ofNullable(right);
    }
  }
}
