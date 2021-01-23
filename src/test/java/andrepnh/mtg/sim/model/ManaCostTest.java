package andrepnh.mtg.sim.model;

import static andrepnh.mtg.sim.model.HybridMana.RG;
import static andrepnh.mtg.sim.model.HybridMana.UB;
import static andrepnh.mtg.sim.model.HybridMana.WU;
import static andrepnh.mtg.sim.model.HybridMana._2R;
import static andrepnh.mtg.sim.model.HybridMana._2W;
import static andrepnh.mtg.sim.model.Mana.B;
import static andrepnh.mtg.sim.model.Mana.G;
import static andrepnh.mtg.sim.model.Mana.R;
import static andrepnh.mtg.sim.model.Mana.U;
import static andrepnh.mtg.sim.model.Mana.W;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import andrepnh.mtg.sim.analytics.test.ManaCosts;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ManaCostTest {

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
    assertThatThrownBy(() -> ManaCost.of(1, null))
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
  @EnumSource(HybridMana.class)
  void sumShouldNotAddDifferentHybridMana(HybridMana hybrid) {
    var otherHybrids = Sets
        .difference(EnumSet.allOf(HybridMana.class), Collections.singleton(hybrid));
    for (var otherHybrid: otherHybrids) {
      var sum = ManaCost.ofHybrid(0, hybrid).sum(ManaCost.ofHybrid(0, otherHybrid));

      assertThat(sum.getCostPerHybridMana())
          .isEqualTo(ImmutableMap.of(hybrid, 1, otherHybrid, 1));
      assertThat(sum.getColorlessCost()).isZero();
      assertThat(sum.getCostPerColoredMana()).isEmpty();
    }
  }

  @ParameterizedTest
  @EnumSource(HybridMana.class)
  void sumShouldAddEqualHybridMana(HybridMana hybrid) {
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
  @EnumSource(HybridMana.class)
  void shouldParseAllDualManaSymbols(HybridMana mana) {
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

  private List<ManaCost> randomCosts(int amount) {
    return IntStream.range(0, amount)
        .mapToObj(unused -> ManaCosts.random())
        .collect(Collectors.toList());
  }
}
