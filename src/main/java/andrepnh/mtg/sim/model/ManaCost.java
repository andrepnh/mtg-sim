package andrepnh.mtg.sim.model;

import static andrepnh.mtg.sim.util.Util.cat;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Value;
import reactor.core.publisher.Flux;

@Value
public class ManaCost {
  public static final ManaCost ZERO = new ManaCost(
      ImmutableList.of(), 0, Collections.emptyMap(), Collections.emptyMap());
  private static final Pattern MANA_COST_PATTERN = Pattern
      .compile("(?<x>X*)(?<colorless>\\d*)(?<hybrid>(?:[2WUBRG]/[WUBRG])*)(?<colored>[WUBRG]*)");
  private static final Comparator<HybridMana<?>> HYBRID_MANA_COMPARATOR = (c1, c2) -> {
    int mult1 = c1.isMonocoloredHybrid() ? 1 : 10,
        mult2 = c2.isMonocoloredHybrid() ? 1 : 10;
    return ((Enum) c1).ordinal() * mult1 - ((Enum) c2).ordinal() * mult2;
  };

  // Each integer represents the amount of duplicated Xs (e.g. XXW -> [2]W) and separate elements
  // means this cost has different Xs (e.g. XW.sum(XR) -> [1, 1]WR
  ImmutableList<Integer> xs;
  int colorlessCost;
  ImmutableMap<Mana, Integer> costPerColoredMana;
  ImmutableMap<HybridMana<?>, Integer> costPerHybridMana;

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

  public static ManaCost ofHybrid(int colorlessCost, HybridMana<?> first, HybridMana<?>... more) {
    return new ManaCost(
        ImmutableList.of(),
        colorlessCost,
        Collections.emptyMap(),
        groupBySymbol(first, more));
  }

  public static ManaCost withXAndHybrid(int xs, int colorlessCost, HybridMana<?> first, HybridMana<?>... more) {
    return new ManaCost(
        ImmutableList.of(xs),
        colorlessCost,
        Collections.emptyMap(),
        groupBySymbol(first, more));
  }

  public static ManaCost withAll(
      int xs, int colorlessCost, ImmutableList<Mana> coloredCost, ImmutableList<HybridMana<?>> hybridCost) {
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
      List<HybridMana<?>> hybridMana = parseHybrid(hybridSymbols);
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

  private static List<HybridMana<?>> parseHybrid(String hybridSymbols) {
    return Flux.fromStream(hybridSymbols.chars().boxed())
        .filter(charCode -> charCode != '/')
        .map(charCode -> String.valueOf((char) charCode.intValue()))
        .buffer(2)
        .map(hybridSymbolString -> String.join("", hybridSymbolString))
        .map(hybridSymbol -> hybridSymbol.contains("2")
            ? (HybridMana<?>) MonocoloredHybridMana.parse(hybridSymbol)
            : BicolorHybridMana.parse(hybridSymbol))
        .collectList()
        .block();
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

  private static Map<HybridMana<?>, Integer> groupBySymbol(HybridMana<?> first, HybridMana<?>... more) {
    return groupBySymbol(Lists.asList(first, more));
  }

  private static Map<HybridMana<?>, Integer> groupBySymbol(List<HybridMana<?>> hybridMana) {
    return hybridMana.stream()
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
  }

  private ManaCost(
      ImmutableList<Integer> xs,
      int colorlessCost,
      Map<Mana, Integer> coloredCosts,
      Map<HybridMana<?>, Integer> hybridManaCosts) {
    checkNotNull(xs, "xs")
        .forEach(x -> checkArgument(x > 0, "Invalid amount of X in %s", xs));
    checkArgument(colorlessCost >= 0, "Colorless cost must be >= 0; got %s", colorlessCost);
    this.xs = xs;
    this.colorlessCost = colorlessCost;
    this.costPerColoredMana = ImmutableMap.copyOf(coloredCosts);
    this.costPerHybridMana = ImmutableMap.copyOf(hybridManaCosts);
  }

  public int cmc() {
    int colored = costPerColoredMana.values().stream().mapToInt(i -> i).sum();
    // Rule 203.3c: https://yawgatog.com/resources/rules-changes/mor-shm/?id=11
    int hybrid = costPerHybridMana.entrySet()
        .stream()
        .mapToInt(kv -> kv.getKey().isMonocoloredHybrid() ? 2 * kv.getValue() : kv.getValue())
        .sum();
    return colorlessCost + colored + hybrid;
  }

  public ManaCost sum(ManaCost that) {
    requireNonNull(that);
    Map<Mana, Integer> coloredManaSum = sumColoredMana(that);
    Map<HybridMana<?>, Integer> hybridManaSum = sumHybridMana(that);

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

  private Map<HybridMana<?>, Integer> sumHybridMana(ManaCost that) {
    var hybridManaSum = new HashMap<>(costPerHybridMana);
    for (var hybridMana:
        cat(EnumSet.allOf(BicolorHybridMana.class), EnumSet.allOf(MonocoloredHybridMana.class))) {
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

  /**
   * @return an optional containing the mana used to cast card(s) represented by this cost, or an
   * empty optional if the mana available is not enough.
   */
  public Optional<List<Mana>> payWith(ImmutableMap<Mana, Integer> manaAvailable) {
    return doPayWith(manaAvailable, ImmutableList.of())
        .map(pair -> new ArrayList<>(pair._2));
  }

  private Optional<Tuple2<ImmutableMap<Mana, Integer>, ImmutableList<Mana>>> doPayWith(
      ImmutableMap<Mana, Integer> manaAvailable, ImmutableList<Mana> manaUsed) {
    return payColorCostsWith(manaAvailable, manaUsed)
        .flatMap(pair -> pair.apply((available, used) -> payHybridCostsWith(available, used)))
        .flatMap(pair -> pair.apply((available, used) -> payColorlessCostsWith(available, used)));
  }

  private Optional<Tuple2<ImmutableMap<Mana, Integer>, ImmutableList<Mana>>> payColorCostsWith(
      ImmutableMap<Mana, Integer> manaAvailable, ImmutableList<Mana> manaUsedSoFar) {
    var available = new HashMap<>(manaAvailable);
    var used = new ArrayList<>(manaUsedSoFar);
    for (var entry: costPerColoredMana.entrySet()) {
      int manaLeft = available.merge(entry.getKey(), -entry.getValue(), Integer::sum);
      if (manaLeft < 0) {
        return Optional.empty();
      }
      used.addAll(Collections.nCopies(entry.getValue(), entry.getKey()));
    }
    return Optional.of(Tuple.of(ImmutableMap.copyOf(available), ImmutableList.copyOf(used)));
  }

  private Optional<Tuple2<ImmutableMap<Mana, Integer>, ImmutableList<Mana>>> payHybridCostsWith(
      ImmutableMap<Mana, Integer> manaAvailable, ImmutableList<Mana> manaUsedSoFar) {
    if (costPerHybridMana.isEmpty()) {
      return Optional.of(Tuple.of(manaAvailable, manaUsedSoFar));
    }
    List<HashSet<Object>> hybridManaOptions = costPerHybridMana.entrySet()
        .stream()
        .flatMap(entry -> Collections.nCopies(entry.getValue(), entry.getKey()).stream())
        .map(hybrid -> Sets.newHashSet(hybrid.option1(), hybrid.option2()))
        .collect(Collectors.toList());

    // Brute forcing since cardinality should be small
    Set<List<Object>> hybridCombinations = Sets.cartesianProduct(hybridManaOptions);
    return hybridCombinations.stream()
        .map(combination -> {
          Tuple2<List<Integer>, List<Mana>> combinationCosts = splitByCostType(combination);
          int totalColorless = combinationCosts._1.stream().mapToInt(i -> i).sum();
          var combinationCost = ManaCost.of(
              totalColorless,
              combinationCosts._2.toArray(Mana[]::new));
          return combinationCost.doPayWith(manaAvailable, manaUsedSoFar);
        })
        .filter(Optional::isPresent)
        .findAny()
        .flatMap(Function.identity());
  }

  private Optional<Tuple2<ImmutableMap<Mana, Integer>, ImmutableList<Mana>>> payColorlessCostsWith(
      ImmutableMap<Mana, Integer> manaAvailable, ImmutableList<Mana> manaUsedSoFar) {
    var available = new HashMap<>(manaAvailable);
    var used = new ArrayList<>(manaUsedSoFar);
    int remainingColorless = colorlessCost;
    for (var entry: manaAvailable.entrySet()) {
      if (remainingColorless == 0) {
        break;
      }
      int toTake = Math.min(remainingColorless, entry.getValue());
      available.merge(entry.getKey(), 0, (curr, nw) -> curr - nw);
      remainingColorless -= toTake;
      used.addAll(Collections.nCopies(toTake, entry.getKey()));
    }

    if (remainingColorless > 0) {
      return Optional.empty();
    } else {
      return Optional.of(Tuple.of(ImmutableMap.copyOf(available), ImmutableList.copyOf(used)));
    }
  }

  private Tuple2<List<Integer>, List<Mana>> splitByCostType(List<Object> manaCosts) {
    var colorlessCosts = new ArrayList<Integer>();
    var coloredCosts = new ArrayList<Mana>();
    for (Object manaCost: manaCosts) {
      if (manaCost instanceof Integer) {
        colorlessCosts.add((Integer) manaCost);
      } else if (manaCost instanceof Mana) {
        coloredCosts.add((Mana) manaCost);
      } else {
        throw new IllegalArgumentException("Unknown mana cost: " + manaCost);
      }
    }
    return Tuple.of(colorlessCosts, coloredCosts);
  }

  private String joinWithoutDelimiter(List<?> list) {
    return list.stream()
        .map(Object::toString)
        .collect(Collectors.joining());
  }

  private <T> List<T> flatten(ImmutableMap<T, Integer> costPerMana) {
    var flattened = new ArrayList<T>();
    costPerMana.forEach((key, value) -> flattened.addAll(Collections.nCopies(value, key)));
    return flattened;
  }

  private List<HybridMana<?>> sortHybrid(List<HybridMana<?>> hybridCosts) {
    hybridCosts.sort(HYBRID_MANA_COMPARATOR);
    return hybridCosts;
  }

  @Override
  public String toString() {
    boolean zero = colorlessCost == 0 && xs.isEmpty()
        && costPerColoredMana.isEmpty() && costPerHybridMana.isEmpty();
    return zero
        ? "0"
        : String.format(
            "%s%s%s%s",
            xs.isEmpty() ? "" : Strings.repeat("X", xs.get(0)),
            colorlessCost == 0 ? "" : colorlessCost,
            joinWithoutDelimiter(sortHybrid(flatten(costPerHybridMana))),
            joinWithoutDelimiter(flatten(costPerColoredMana)));
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
