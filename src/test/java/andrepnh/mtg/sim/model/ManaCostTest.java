package andrepnh.mtg.sim.model;

import static andrepnh.mtg.sim.model.BicolorHybridMana.*;
import static andrepnh.mtg.sim.model.Mana.*;
import static andrepnh.mtg.sim.model.MonocoloredHybridMana.*;
import static andrepnh.mtg.sim.test.ListAssertion.assertThatList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import andrepnh.mtg.sim.analytics.test.ManaCosts;
import andrepnh.mtg.sim.test.Varargs;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ManaCostTest {

  @Test
  void payWithShouldBeConsistent() {
    var cost = ManaCost.parse("1W/UU/B");
    var availableMana = ImmutableMap.of(W, 2, U, 1);

    for (int i = 0; i < 100; i++) {
      assertThat(cost.payWith(availableMana))
          .get(as(InstanceOfAssertFactories.LIST))
          .containsExactlyInAnyOrder(W, W, U);
    }
  }

  @Test
  void payWithShouldPrioritizeManaUsageBasedOnCost() {
    var cost = ManaCost.parse("1W/U2/UW");
    var availableMana = ImmutableMap.of(W, 1, U, 1, B, 3);

    Optional<List<Mana>> manaUsed = cost.payWith(availableMana);

    assertThat(manaUsed)
        .get(as(InstanceOfAssertFactories.LIST))
        .containsExactlyInAnyOrder(W, U, B, B, B);
  }

  @ParameterizedTest(name = "payWithReturnsEmptyListIfNotEnoughManaIsAvailableFor{0}")
  @ValueSource(strings = {"3", "W", "B", "U", "GG", "2G", "2GG", "1RG", "2R", "RR", "RRG", "GGR"})
  void payWithReturnsEmptyListIfNotEnoughManaIsAvailableFor(String rawCost) {
    var availableMana = ImmutableMap.of(G, 1, R, 1);

    assertThat(ManaCost.parse(rawCost).payWith(availableMana)).isEmpty();
  }

  @ParameterizedTest(name = "payWithReturnsManaUsedWhenEnoughIsAvailableFor{0}")
  @ValueSource(strings = {
      "0", "1", "2", "3", "4",
      "W", "1W", "2W", "3W", "WW", "1WW", "2WW",
      "U", "1U", "2U", "3U",
      "B", "1B", "2B", "3B",
      "WU", "1WU", "2WU", "WWU", "1WWU",
      "WB", "1WB", "2WB", "WWB", "1WWB",
      "UB", "1UB", "2UB",
      "WUB", "1WUB", "WWUB"})
  void payWithReturnsManaUsedWhenEnoughIsAvailableFor(String rawCost) {
    var availableMana = ImmutableMap.of(W, 2, U, 1, B, 1);
    int expectedManaUsed = rawCost.matches("\\d+.*")
        ? Integer.parseInt(rawCost.substring(0, 1)) + rawCost.length() - 1
        : rawCost.length();

    assertThat(ManaCost.parse(rawCost).payWith(availableMana))
        .get(as(InstanceOfAssertFactories.LIST))
        .as("Available mana is enough for total cost: %s >= %s", availableMana, rawCost)
        .hasSize(expectedManaUsed);
  }

  @ParameterizedTest(name = "shouldUse[{2}]OutOf[{0}]ToPay[{1}]")
  @CsvSource({
      "WWU,2/WWU,WWU",
      "WWU,1W/UU/B,WWU",
      "WWU,2/G,WW,WU",
      "WWU,2/GW,WWU",
      "WWU,2/GU,WWU",
      "WWU,W/UW/UU/G,WWU",
      "WU,1W/G,WU",
      "WU,W/UU/G,WU"})
  void payWithReturnsManaUsedIfAvailableManaCanBeArrangedToPayHybridCostsOf(
      String mana, String cost, @AggregateWith(Varargs.class) String... expectedManaUsed) {
    List<List<Mana>> options = Stream.of(expectedManaUsed)
        .map(manaUsed -> Stream.of(manaUsed.split(""))
            .map(Mana::valueOf)
            .collect(Collectors.toList()))
        .collect(Collectors.toList());
    var availableMana = ImmutableMap.copyOf(mana.chars()
        .mapToObj(c -> String.valueOf((char) c))
        .map(Mana::valueOf)
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue))));

    var manaUsed = ManaCost.parse(cost).payWith(availableMana);
    assertThat(manaUsed).isNotEmpty();
    manaUsed.get().sort(Comparator.naturalOrder());

    assertThatList(manaUsed.get()).containsExactlyAnyOf(options);
  }

  @ParameterizedTest(name = "payWithReturnsEmptyListWhenThereIsNotManaAvailableForHybridCost{0}")
  @ValueSource(strings = {"2/W", "W/U", "W/B", "W/R", "U/B", "U/R", "B/R"})
  void payWithReturnsEmptyListWhenThereIsNotManaAvailableForHybridCost(String rawCost) {
    var availableMana = ImmutableMap.of(G, 1);

    assertThat(ManaCost.parse(rawCost).payWith(availableMana))
        .as("Available mana is not enough for total cost: %s < %s", availableMana, rawCost)
        .isEmpty();
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -2, Integer.MIN_VALUE})
  void shouldNotAllowInvalidColorlessCosts(int colorlessCost) {
    assertThatThrownBy(() -> ManaCost.of(colorlessCost))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(String.valueOf(colorlessCost));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, Integer.MAX_VALUE})
  void shouldAllowValidColorlessCosts(int colorlessCost) {
    var cost = ManaCost.of(colorlessCost);

    assertThat(cost.getColorlessCost()).isEqualTo(colorlessCost);
  }

  @Test
  void shouldNotAllowNullColoredMana() {
    assertThatThrownBy(() -> ManaCost.of(1, (Mana[]) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ManaCost.of(1, null, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("[null, null]");
  }

  @ParameterizedTest
  @ValueSource(strings = {"W", "U", "B", "R", "G", "WWWWW", "WUBRG", "WWUUUBRRGG"})
  void shouldAllowValidColoredCosts(String coloredCostString) {
    var coloredCost = Stream.of(coloredCostString.split(""))
        .map(Mana::valueOf)
        .toArray(Mana[]::new);
    Map<Mana, Integer> expectedCostPerColor = Stream.of(coloredCost)
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

    var cost = ManaCost.of(0, coloredCost);

    assertThat(cost.getCostPerColoredMana()).isEqualTo(expectedCostPerColor);
  }

  @Test
  void orderDoesNotMatterWhenComparingEqualXs() {
    var sum1 = ManaCost.withX(1, 0)
        .sum(ManaCost.withX(2, 0))
        .sum(ManaCost.withX(3, 0))
        .sum(ManaCost.withX(4, 0));
    var sum2 = ManaCost.withX(4, 0)
        .sum(ManaCost.withX(3, 0))
        .sum(ManaCost.withX(2, 0))
        .sum(ManaCost.withX(1, 0));

    assertThat(sum1).isEqualTo(sum2);
  }

  @Test
  void sumShouldBeCommutative() {
    // Since null.sum(<anything>) will definitely throw NPE
    assertThatThrownBy(() -> ManaCost.ZERO.sum(null))
        .isInstanceOf(NullPointerException.class);

    List<ManaCost> random = randomCosts(1000);

    Lists.partition(random, 5)
        .forEach(costs -> {
          var sum = costs.stream().reduce(ManaCost::sum).orElse(ManaCost.ZERO);
          var shuffledCosts = new ArrayList<>(costs);
          Collections.shuffle(shuffledCosts);
          var shuffledSum = shuffledCosts.stream().reduce(ManaCost::sum).orElse(ManaCost.ZERO);
          assertThat(sum).isEqualTo(shuffledSum);
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "1", "W", "X", "XW", "W/U", "2/U", "XX2B/R2/GU"})
  void sumShouldNotChangeCostWhenZeroIsAdded(String manaCost) {
    var cost = ManaCost.parse(manaCost);

    var sum = cost.sum(ManaCost.ZERO);
    assertThat(sum).isEqualTo(cost);
    sum = sum.sum(ManaCost.ZERO);
    assertThat(sum).isEqualTo(cost);
  }

  @Test
  void sumShouldAddAllIndividualManaCosts() {
    assertThat(ManaCost.of(1).sum(ManaCost.of(2)).sum(ManaCost.of(3)))
        .isEqualTo(ManaCost.of(6));

    assertThat(ManaCost.of(0, W).sum(ManaCost.of(0, W).sum(ManaCost.of(0, W, W))))
        .isEqualTo(ManaCost.of(0, W, W, W, W));

    assertThat(ManaCost.of(1, W, G).sum(ManaCost.of(2, W, W).sum(ManaCost.of(0, G, G))))
        .isEqualTo(ManaCost.of(3, W, W, W, G, G, G));
  }

  @Test
  void sumShouldIncludeManaCostsNotAvailableOnTheOriginal() {
    assertThat(ManaCost.ZERO.sum(ManaCost.of(3)))
        .isEqualTo(ManaCost.of(3));

    assertThat(ManaCost.ZERO.sum(ManaCost.of(0, W, G)))
        .isEqualTo(ManaCost.of(0, W, G));

    assertThat(ManaCost.of(1, W, U).sum(ManaCost.of(0, B, R, G)))
        .isEqualTo(ManaCost.of(1, W, U, B, R, G));

    assertThat(ManaCost.of(1, W, U).sum(ManaCost.of(0, U, B)))
        .isEqualTo(ManaCost.of(1, W, U, U, B));

    assertThat(
        ManaCost.of(1)
            .sum(ManaCost.of(0, W))
            .sum(ManaCost.of(0, U))
            .sum(ManaCost.of(0, B))
            .sum(ManaCost.of(0, R))
            .sum(ManaCost.of(0, G)))
        .isEqualTo(ManaCost.of(1, W, U, B, R, G));
  }

  @Test
  void sumShouldNotAddDifferentXs() {
    assertThat(ManaCost.withX(1, 0).sum(ManaCost.withX(1, 0)))
        .isEqualTo(ManaCost.withX(1, 1, 0));

    assertThat(ManaCost.withX(1, 0).sum(ManaCost.withX(1, 0).sum(ManaCost.withX(1, 0))))
        .isEqualTo(ManaCost.withX(1, 1, 1, 0));

    assertThat(ManaCost.withX(2, 0, W).sum(ManaCost.withX(3, 0, G)))
        .isEqualTo(ManaCost.withX(2, 3, 0, W, G));
  }

  @ParameterizedTest
  @EnumSource(BicolorHybridMana.class)
  void sumShouldNotAddDifferentHybridMana(BicolorHybridMana hybrid) {
    var otherHybrids = Sets
        .difference(EnumSet.allOf(BicolorHybridMana.class), Collections.singleton(hybrid));
    for (var otherHybrid: otherHybrids) {
      var sum = ManaCost.ofHybrid(0, hybrid).sum(ManaCost.ofHybrid(0, otherHybrid));

      assertThat(sum.getCostPerHybridMana())
          .isEqualTo(ImmutableMap.of(hybrid, 1, otherHybrid, 1));
      assertThat(sum.getColorlessCost()).isZero();
      assertThat(sum.getCostPerColoredMana()).isEmpty();
    }
  }

  @ParameterizedTest
  @EnumSource(BicolorHybridMana.class)
  void sumShouldAddEqualHybridMana(BicolorHybridMana hybrid) {
    var sum = ManaCost.ofHybrid(1, hybrid).sum(ManaCost.ofHybrid(2, hybrid));
    assertThat(sum.getCostPerHybridMana()).containsOnly(entry(hybrid, 2));
    assertThat(sum.getColorlessCost()).isEqualTo(3);
  }

  @Test
  void sumShouldNotMixHybridToColorOrColorless() {
    var sum = ManaCost.ofHybrid(0, WU)
        .sum(ManaCost.of(0, W)
        .sum(ManaCost.of(0, U)));
    assertThat(sum.getCostPerHybridMana()).containsOnly(entry(WU, 1));
    assertThat(sum.getCostPerColoredMana()).containsOnly(entry(W, 1), entry(U, 1));

    sum = ManaCost.ofHybrid(0, _2R)
        .sum(ManaCost.of(0, R)
        .sum(ManaCost.of(2)));
    assertThat(sum.getCostPerHybridMana()).containsOnly(entry(_2R, 1));
    assertThat(sum.getCostPerColoredMana()).containsOnly(entry(R, 1));
    assertThat(sum.getColorlessCost()).isEqualTo(2);
  }

  @Test
  void shouldParseManaCost() {
    assertThat(ManaCost.parse("0")).isEqualTo(ManaCost.ZERO);
    assertThat(ManaCost.parse("W")).isEqualTo(ManaCost.of(0, W));
    assertThat(ManaCost.parse("1WW")).isEqualTo(ManaCost.of(1, W, W));
    assertThat(ManaCost.parse("3WUUB")).isEqualTo(ManaCost.of(3, W, U, U, B));
    assertThat(ManaCost.parse("WUBRG")).isEqualTo(ManaCost.of(0, W, U, B, R, G));
    assertThat(ManaCost.parse("1GRBUW"))
        .isEqualTo(ManaCost.of(1, W, U, B, R, G));
  }

  @Test
  void shouldParseX() {
    assertThat(ManaCost.parse("X")).isEqualTo(ManaCost.withX(1, 0));
    assertThat(ManaCost.parse("X1WW")).isEqualTo(ManaCost.withX(1, 1, W, W));
    assertThat(ManaCost.parse("XXX")).isEqualTo(ManaCost.withX(3, 0));
  }


  @ParameterizedTest
  @EnumSource(BicolorHybridMana.class)
  void shouldParseAllDualManaSymbols(BicolorHybridMana mana) {
    String toParse = String.join("/", mana.name().split(""));

    assertThat(
        ManaCost
            .parse(toParse)
            .getCostPerHybridMana()
            .keySet())
        .containsOnly(mana);
  }

  @ParameterizedTest
  @EnumSource(MonocoloredHybridMana.class)
  void shouldParseAllDualManaSymbols(MonocoloredHybridMana mana) {
    String toParse = String.join(
        "/",
        mana.name()
            .replace("_", "")
            .split(""));

    assertThat(
        ManaCost
            .parse(toParse)
            .getCostPerHybridMana()
            .keySet())
        .containsOnly(mana);
  }

  @Test
  void shouldParseCostWithDualManaAndOtherSymbols() {
    assertThat(ManaCost.parse("1W/U")).isEqualTo(ManaCost.ofHybrid(1, WU));
    assertThat(ManaCost.parse("X1W/UR/G")).isEqualTo(ManaCost.withXAndHybrid(1, 1, WU, RG));
    assertThat(ManaCost.parse("XX3U/BR/GWR")).isEqualTo(ManaCost.withAll(
        2,
        3,
        ImmutableList.of(W, R),
        ImmutableList.of(UB, RG)));
    assertThat(ManaCost.parse("XX32/WU/BR")).isEqualTo(ManaCost.withAll(
        2,
        3,
        ImmutableList.of(R),
        ImmutableList.of(_2W, UB)));
  }

  @ParameterizedTest(name = "shouldNotParseInvalidManaCost{0}")
  @ValueSource(strings = {"w", "W1", "wUb", "a", "-1", "", "\t", "\n", "W U", "W\tU", "xW", "WX", "YX"})
  void shouldNotParseInvalidManaCosts(String rawCost) {
    assertThatThrownBy(() -> ManaCost.parse(rawCost))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid mana cost: " + rawCost);
  }

  @Test
  void cmcShouldNotIncludeXs() {
    assertThat(
        ManaCost.withX(1, 0)
            .sum(ManaCost.withX(2, 1)
            .sum(ManaCost.withX(1, 1, 0, W))
            .sum(ManaCost.withXAndHybrid(1, 0, WU)))
            .cmc())
        .isEqualTo(3);
  }

  @Test
  void cmcShouldUseHighestCostForHybrids() {
    assertThat(ManaCost.parse("22/WU/BG").cmc()).isEqualTo(6);
  }

  private List<ManaCost> randomCosts(int amount) {
    return IntStream.range(0, amount)
        .mapToObj(unused -> ManaCosts.random())
        .collect(Collectors.toList());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "1", "W", "X", "XW", "W/U", "2/U", "XX22/GB/RU"})
  void toStringShouldUseTheSameFormatReadByParse(String manaCost) {
    var firstParse = ManaCost.parse(manaCost);
    var secondParse = ManaCost.parse(firstParse.toString());
    assertThat(secondParse.toString()).isEqualTo(manaCost);
    assertThat(secondParse).isEqualTo(firstParse);
  }
}
