package andrepnh.mtg.sim.analytics.synergy.count;

import andrepnh.mtg.sim.model.Battlefield;
import io.vavr.Tuple3;
import java.util.List;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A test to check if cards in the battlefield contain the synergy represented by the instance.
 * Created directly from parsed {@link Token}s.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SynergyPredicate implements Predicate<Battlefield> {
  @Getter
  private final String name;
  private final Predicate<Battlefield> predicate;

  public static SynergyPredicate of(String name, List<Tuple3<AmountToken, MatchType, CardToken>> tokens) {
    Predicate<Battlefield> predicate = tokens
        .stream()
        .<Predicate<Battlefield>>map(expectedCardCount -> battlefield -> {
          if (expectedCardCount._2 == MatchType.AT_LEAST) {
            return battlefield.count(expectedCardCount._3.value()) >= expectedCardCount._1.value();
          } else if (expectedCardCount._2 == MatchType.EXACT) {
            return battlefield.count(expectedCardCount._3.value()) == expectedCardCount._1.value();
          } else {
            throw new IllegalStateException("Unknown match type in " + expectedCardCount);
          }
        }).reduce(battlefield -> true, Predicate::and);

    return new SynergyPredicate(name, predicate);
  }

  @Override
  public boolean test(Battlefield battlefield) {
    return predicate.test(battlefield);
  }
}
