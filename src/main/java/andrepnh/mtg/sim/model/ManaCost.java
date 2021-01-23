package andrepnh.mtg.sim.model;

import static andrepnh.mtg.sim.util.Util.cat;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Value;

@Value
public class ManaCost {
  public static final ManaCost ZERO = new ManaCost(
      ImmutableList.of(), 0, Collections.emptyMap(), Collections.emptyMap());
  private static final Pattern MANA_COST_PATTERN = Pattern
      .compile("(?<x>X*)(?<colorless>\\d*)(?<hybrid>(?:[2WUBRG]/[WUBRG])*)(?<colored>[WUBRG]*)");

  // Each integer represents the amount of duplicated Xs (e.g. XXW -> [2]W) and separate elements
  // means this cost has different Xs (e.g. XW.sum(XR) -> [1, 1]WR
  ImmutableList<Integer> xs;
  int colorlessCost;
  ImmutableMap<Mana, Integer> costPerColoredMana;
  ImmutableMap<HybridMana, Integer> costPerHybridMana;

  public static ManaCost of(int colorlessCost, Mana... coloredCost) {
    return new ManaCost(
        ImmutableList.of(),
        colorlessCost,
        groupBySymbol(coloredCost),
        Collections.emptyMap());
  }

  public static ManaCost withX(int xs, int colorlessCost, Mana... coloredCost) {
    return new ManaCost(
        ImmutableList.of(xs),
        colorlessCost,
        groupBySymbol(coloredCost),
        Collections.emptyMap());
  }

  public static ManaCost withX(int xs1, int xs2, int colorlessCost, Mana... coloredCost) {
    return new ManaCost(
        ImmutableList.of(xs1, xs2),
        colorlessCost,
        groupBySymbol(coloredCost),
        Collections.emptyMap());
  }

  public static ManaCost withX(int xs1, int xs2, int xs3, int colorlessCost, Mana... coloredCost) {
    return new ManaCost(
        ImmutableList.of(xs1, xs2, xs3),
        colorlessCost,
        groupBySymbol(coloredCost),
        Collections.emptyMap());
  }

  public static ManaCost ofHybrid(int colorlessCost, HybridMana first, HybridMana... more) {
    return new ManaCost(
        ImmutableList.of(),
        colorlessCost,
        Collections.emptyMap(),
        groupBySymbol(first, more));
  }

  public static ManaCost withXAndHybrid(int xs, int colorlessCost, HybridMana first, HybridMana... more) {
    return new ManaCost(
        ImmutableList.of(xs),
        colorlessCost,
        Collections.emptyMap(),
        groupBySymbol(first, more));
  }

  public static ManaCost withXAndHybrid(
      int xs1, int xs2, int colorlessCost, HybridMana first, HybridMana... more) {
    return new ManaCost(
        ImmutableList.of(xs1, xs2),
        colorlessCost,
        Collections.emptyMap(),
        groupBySymbol(first, more));
  }

  public static ManaCost withXAndHybrid(
      int xs1, int xs2, int xs3, int colorlessCost, HybridMana first, HybridMana... more) {
    return new ManaCost(
        ImmutableList.of(xs1, xs2, xs3),
        colorlessCost,
        Collections.emptyMap(),
        groupBySymbol(first, more));
  }

  public static ManaCost withAll(
      int xs, int colorlessCost, ImmutableList<Mana> coloredCost, ImmutableList<HybridMana> hybridCost) {
    return new ManaCost(
        xs == 0 ? ImmutableList.of() : ImmutableList.of(xs),
        colorlessCost,
        groupBySymbol(coloredCost.toArray(Mana[]::new)),
        groupBySymbol(hybridCost));
  }

  public static ManaCost parse(String raw) {
    checkArgument(!Strings.isNullOrEmpty(raw), "Invalid mana cost: %s", raw);
    Matcher matcher = MANA_COST_PATTERN.matcher(raw);
    checkArgument(matcher.matches(), "Invalid mana cost: %s", raw);
    try {
      String x = matcher.group("x");
      int xs = Strings.isNullOrEmpty(x) ? 0 : x.length();
      String colorlessString = matcher.group("colorless");
      int colorless = colorlessString.isEmpty() ? 0 : Integer.parseInt(colorlessString);
      String hybridSymbols = matcher.group("hybrid");
      List<HybridMana> hybridMana = parseHybrid(hybridSymbols);
      String manaSymbols = matcher.group("colored");
      var colored = Strings.isNullOrEmpty(manaSymbols)
          ? new Mana[0]
          : Stream.of(manaSymbols.split(""))
              .map(Mana::valueOf)
              .toArray(Mana[]::new);
      return new ManaCost(
          xs == 0 ? ImmutableList.of() : ImmutableList.of(xs),
          colorless,
          groupBySymbol(colored),
          groupBySymbol(hybridMana));
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid mana cost: " + raw, ex);
    }
  }

  private static List<HybridMana> parseHybrid(String hybridSymbols) {
    List<String> chars = hybridSymbols.chars()
        .mapToObj(c -> String.valueOf((char) c))
        .collect(Collectors.toList());
    // Split every third char
    return Lists.partition(chars, 3)
        .stream()
        .map(hybridSymbolString -> String.join("", hybridSymbolString)
            .replace("/", ""))
        .map(HybridMana::parse)
        .collect(Collectors.toList());
  }

  private static Map<Mana, Integer> groupBySymbol(Mana... coloredCosts) {
    return Stream.of(coloredCosts)
        .peek(cost -> checkNotNull(cost,
            "Got null %s on array: %s",
            ManaCost.class, Arrays.toString(coloredCosts)))
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
  }

  private static Map<HybridMana, Integer> groupBySymbol(HybridMana first, HybridMana... more) {
    return groupBySymbol(Lists.asList(first, more));
  }

  private static Map<HybridMana, Integer> groupBySymbol(List<HybridMana> hybridMana) {
    return hybridMana.stream()
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
  }

  private ManaCost(
      ImmutableList<Integer> xs,
      int colorlessCost,
      Map<Mana, Integer> coloredCosts,
      Map<HybridMana, Integer> hybridManaCosts) {
    checkNotNull(xs, "xs")
        .forEach(x -> checkArgument(x > 0, "Invalid amount of X in %s", xs));
    checkArgument(colorlessCost >= 0, "Colorless cost must be >= 0; got %s", colorlessCost);
    this.xs = xs;
    this.colorlessCost = colorlessCost;
    this.costPerColoredMana = ImmutableMap.copyOf(coloredCosts);
    this.costPerHybridMana = ImmutableMap.copyOf(hybridManaCosts);
  }

  public ManaCost sum(ManaCost that) {
    requireNonNull(that);
    Map<Mana, Integer> coloredManaSum = sumColoredMana(that);
    Map<HybridMana, Integer> hybridManaSum = sumHybridMana(that);

    return new ManaCost(
        cat(this.xs, that.xs),
        this.colorlessCost + that.colorlessCost,
        coloredManaSum.entrySet()
            .stream()
            .filter(entry -> entry.getValue() != 0)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        hybridManaSum);
  }

  private Map<Mana, Integer> sumColoredMana(ManaCost that) {
    var coloredManaSum = new HashMap<>(costPerColoredMana);
    for (var coloredMana: Mana.values()) {
      coloredManaSum.merge(
          coloredMana,
          that.costPerColoredMana.getOrDefault(coloredMana, 0),
          Integer::sum);
    }
    return coloredManaSum
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() != 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Map<HybridMana, Integer> sumHybridMana(ManaCost that) {
    var hybridManaSum = new HashMap<>(costPerHybridMana);
    for (var hybridMana: HybridMana.values()) {
      hybridManaSum.merge(
          hybridMana,
          that.costPerHybridMana.getOrDefault(hybridMana, 0),
          Integer::sum);
    }
    return hybridManaSum
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() != 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public boolean fits(Map<Mana, Integer> manaAvailable) {
    int remainingColorless = colorlessCost;
    manaAvailable = new HashMap<>(manaAvailable);
    for (var entry: costPerColoredMana.entrySet()) {
      int remaining = manaAvailable.merge(entry.getKey(), 0, (aval, cost) -> aval - cost);
      // Immediately try to use what's left to pay colorless
      remainingColorless = Math.max(0, remainingColorless - remaining);
    }
    return remainingColorless == 0 && manaAvailable.values()
        .stream()
        .allMatch(available -> available >= 0);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ManaCost that = (ManaCost) o;
    if (this.colorlessCost != that.colorlessCost
        || !Objects.equals(this.costPerColoredMana, that.costPerColoredMana)
        || !Objects.equals(this.costPerHybridMana, that.costPerHybridMana)) {
      return false;
    }
    List<Integer> thisXs, thatXs;
    if (this.xs.size() > 1 || that.xs.size() > 1) {
      thisXs = new ArrayList<>(this.xs);
      thisXs.sort(Comparator.naturalOrder());
      thatXs = new ArrayList<>(that.xs);
      thatXs.sort(Comparator.naturalOrder());
    } else {
      thisXs = this.xs;
      thatXs = that.xs;
    }
    return Objects.equals(thisXs, thatXs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(xs, colorlessCost, costPerColoredMana, costPerHybridMana);
  }
}
