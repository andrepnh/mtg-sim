package andrepnh.mtg.sim.analytics.test;

import andrepnh.mtg.sim.model.BicolorHybridMana;
import andrepnh.mtg.sim.model.HybridMana;
import andrepnh.mtg.sim.model.Mana;
import andrepnh.mtg.sim.model.ManaCost;
import andrepnh.mtg.sim.model.MonocoloredHybridMana;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ManaCosts {
  public ManaCost random() {
    int xs = ThreadLocalRandom.current().nextInt(0, 3);
    int colorless = ThreadLocalRandom.current().nextInt(0, 9);

    int cardColors = ThreadLocalRandom.current().nextInt(0, 6);
    var colored = IntStream.range(0, cardColors)
        .mapToObj(unused -> RandomUtils.choose(Arrays.asList(Mana.values())))
        .flatMap(color -> Collections
            .nCopies(
                ThreadLocalRandom.current().nextInt(1, 4),
                color)
            .stream())
        .collect(ImmutableList.toImmutableList());

    int hybridColors = ThreadLocalRandom.current().nextInt(0, 3);
    ImmutableList<HybridMana<?>> hybrid = IntStream.range(0, hybridColors)
        .mapToObj(unused -> {
          boolean bicolor = ThreadLocalRandom.current().nextBoolean();
          if (bicolor) {
            return RandomUtils.choose(Arrays.asList(BicolorHybridMana.values()));
          } else {
            return RandomUtils.choose(Arrays.asList(MonocoloredHybridMana.values()));
          }
        })
        .flatMap(hybridColor -> Collections
            .nCopies(
                ThreadLocalRandom.current().nextInt(1, 4),
                hybridColor)
            .stream())
        .collect(ImmutableList.toImmutableList());

    return ManaCost.withAll(xs, colorless, colored, hybrid);
  }
}
