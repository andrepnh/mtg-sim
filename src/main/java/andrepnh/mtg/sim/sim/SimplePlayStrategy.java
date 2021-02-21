package andrepnh.mtg.sim.sim;

import static andrepnh.mtg.sim.util.Util.cat;

import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Hand;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.Mana;
import andrepnh.mtg.sim.model.ManaCost;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Delegate;

/**
 * Favors spending as much mana as possible, then as many cards as possible.
 */
public enum SimplePlayStrategy implements PlayStrategy {
  INSTANCE;

  private static final Comparator<Line> HIGHEST_CMC = Comparator
      .<Line>comparingInt(line -> line.getManaUsed().size())
      .reversed();

  private static final Comparator<Line> MORE_CARDS_PLAYED = Comparator
      .<Line>comparingInt(line -> line.getSpellsPlayed().size())
      .reversed();

  private static final Comparator<Line> HAS_NO_X = Comparator
      .comparingInt(line -> {
        boolean anyX = line.getSpellsPlayed()
            .stream()
            .anyMatch(spell -> spell.getCost().hasX());
        return anyX ? 1 : 0;
      });

  private static final Comparator<Line> LINE_COMPARATOR = HIGHEST_CMC
      .thenComparing(MORE_CARDS_PLAYED)
      .thenComparing(HAS_NO_X);

  @Override
  public Line choose(Hand hand, ImmutableList<Land> mana) {
    Line line = hand
        .getCards()
        .stream()
        .filter(Card::isLand)
        .distinct()
        .map(land -> {
          ImmutableList<Land> manaAvailable = cat(mana, ImmutableList.of((Land) land));
          var noLandLine = pickSpells(hand, manaAvailable);
          return new Line(
              ImmutableList.of((Land) land), noLandLine.getSpellsPlayed(), noLandLine.getManaUsed());
        }).min(LINE_COMPARATOR)
        .orElseGet(() -> pickSpells(hand, mana));

    return line;
  }

  private Line pickSpells(Hand hand, ImmutableList<Land> manaAvailable) {
    ImmutableMap<Mana, Integer> manaPerType = ImmutableMap.copyOf(manaAvailable
        .stream()
        .flatMap(land -> land.generate().stream())
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue))));
    List<Spell> spells = hand.getCards()
        .stream()
        .filter(Predicate.not(Card::isLand))
        .map(spell -> (Spell) spell)
        .collect(Collectors.toList());
    Set<SetFriendlySpell> spellsSet = toSetAllowingDuplicates(spells);

    // Brute forcing since cardinality is small
    Stream<Tuple2<ImmutableList<Spell>, ImmutableList<Mana>>> possiblePicks =
        Sets.powerSet(spellsSet).stream()
            .map(
                subSet -> {
                  ManaCost totalCost =
                      subSet.stream()
                          .map(SetFriendlySpell::getCost)
                          .reduce(ManaCost.ZERO, ManaCost::sum);
                  // Back to Spell just in case downstream needs proper hash code logic
                  ImmutableList<Spell> spellList =
                      subSet.stream()
                          .map(SetFriendlySpell::getSpell)
                          .collect(ImmutableList.toImmutableList());
                  return Tuple.of(spellList, totalCost);
                })
            .map(subSetCmcPair -> subSetCmcPair.map2(cost -> cost.payWith(manaPerType)))
            .filter(subSetManaPayedPair -> subSetManaPayedPair._2.isPresent())
            .map(subSetManaPayedPair -> subSetManaPayedPair
                .map2(manaPayed -> ImmutableList.copyOf(manaPayed.get())));

    return possiblePicks
        .map(pick -> new Line(ImmutableList.of(), pick._1, pick._2))
        .min(LINE_COMPARATOR)
        .orElse(Line.empty());
  }

  private Set<SetFriendlySpell> toSetAllowingDuplicates(List<Spell> spells) {
    Stream<Tuple2<Integer, Spell>> indexSpellPair = Streams
        .zip(spells.stream(),
            IntStream.range(0, spells.size()).boxed(),
            (spell, i) -> Tuple.of(i, spell));

    return indexSpellPair
        .map(pair -> new SetFriendlySpell(pair._1, pair._2))
        .collect(Collectors.toSet());
  }

  @Value
  private static class SetFriendlySpell {
    @Getter(AccessLevel.NONE)
    int unique;

    @Delegate
    Spell spell;

    private SetFriendlySpell(int unique, Spell spell) {
      this.unique = unique;
      this.spell = spell;
    }
  }
}
