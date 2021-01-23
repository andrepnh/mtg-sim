package andrepnh.mtg.sim.analytics.test;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RandomUtils {
  public <T> T choose(Collection<T> collection) {
    int chosen = ThreadLocalRandom.current().nextInt(0, collection.size());
    int i = 0;
    for (T value: collection) {
      if (i++ == chosen) {
        return value;
      }
    }

    throw new IllegalStateException(String.format(
        "Should have chosen element %s of %s",
        chosen, collection));
  }
}
