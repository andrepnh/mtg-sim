package andrepnh.mtg.sim.test;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.assertj.core.api.AbstractAssert;

public class ListAssertion<T> extends AbstractAssert<ListAssertion<T>, List<T>> {
  public ListAssertion(List<T> objects) {
    super(objects, ListAssertion.class);
  }

  public static <T> ListAssertion<T> assertThatList(List<T> actual) {
    return new ListAssertion<>(actual);
  }

  public ListAssertion<T> containsExactlyAnyOf(Iterable<List<T>> options) {
    isNotNull();
    for (List<T> option: options) {
      if (Objects.equals(actual, option)) {
        return this;
      }
    }
    String optionsString = StreamSupport.stream(options.spliterator(), false)
        .map(list -> "  " + list)
        .collect(Collectors.joining(System.lineSeparator()));
    failWithMessage("Expected list:%n  %s%n to contain any of:%s",
        actual, optionsString);
    return this;
  }
}
