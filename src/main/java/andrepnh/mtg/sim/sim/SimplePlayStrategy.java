package andrepnh.mtg.sim.sim;

import static andrepnh.mtg.sim.util.Util.asList;

import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Hand;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

  private static final Comparator<Tuple2<ImmutableList<Spell>, Integer>> HIGHEST_CMC = Comparator
      .<Tuple2<ImmutableList<Spell>, Integer>>comparingInt(subSetCmcPair -> subSetCmcPair._2)
      .reversed();

  private static final Comparator<Tuple2<ImmutableList<Spell>, Integer>> MORE_CARDS_PLAYED = Comparator
      .<Tuple2<ImmutableList<Spell>, Integer>>comparingInt(subSetCmcPair -> subSetCmcPair._1.size())
      .reversed();

  @Override
  public Tuple2<ImmutableList<Land>, ImmutableList<Spell>> choose(Hand hand, ImmutableList<Land> mana) {
    ImmutableList<Land> manaPlayed = pickLand(hand);
    final int manaAvailable = mana.size() + manaPlayed.size();
    List<Spell> spells = hand.getCards()
        .stream()
        .filter(Predicate.not(Card::isLand))
        .map(spell -> (Spell) spell)
        .collect(Collectors.toList());
    Set<SetFriendlySpell> spellsSet = toSetAllowingDuplicates(spells);

    // Brute forcing since cardinality is small
    Stream<Tuple2<ImmutableList<Spell>, Integer>> possiblePicks = Sets.powerSet(spellsSet)
        .stream()
        .map(subSet -> {
          int totalCmc = subSet.stream()
              .mapToInt(SetFriendlySpell::getCmc)
              .sum();
          // Back to Spell just in case downstream needs proper hash code logic
          ImmutableList<Spell> spellList = subSet.stream()
              .map(SetFriendlySpell::getSpell)
              .collect(ImmutableList.toImmutableList());
          return Tuple.of(spellList, totalCmc);
        }).filter(subSetCmcPair -> subSetCmcPair._2 <= manaAvailable);

    ImmutableList<Spell> spellsPlayed = possiblePicks
        .sorted(HIGHEST_CMC.thenComparing(MORE_CARDS_PLAYED))
        .findFirst()
        .map(spellsCmcPair -> spellsCmcPair._1)
        .orElse(ImmutableList.of());

    return Tuple.of(manaPlayed, spellsPlayed);
  }

  private ImmutableList<Land> pickLand(Hand hand) {
    Optional<Land> landPlayed = hand.getCards()
        .stream()
        .filter(Card::isLand)
        .map(land -> (Land) land)
        .findAny();
    return asList(landPlayed);
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
