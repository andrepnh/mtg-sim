package andrepnh.mtg.sim.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import java.util.Optional;

public final class Util {
  public static <T> ImmutableList<T> cat(
      Iterable<? extends T> first,
      Iterable<? extends T> second,
      Iterable<? extends T>... more) {
    Builder<T> builder = ImmutableList.<T>builder()
        .addAll(first)
        .addAll(second);
    Arrays.asList(more).forEach(builder::addAll);
    return builder.build();
  }

  public static <T> ImmutableList<T> asList(Optional<? extends T> optional) {
    return optional.isEmpty() ? ImmutableList.of() : ImmutableList.of(optional.get());
  }

  private Util() { }
}
