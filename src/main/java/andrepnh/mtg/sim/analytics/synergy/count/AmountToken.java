package andrepnh.mtg.sim.analytics.synergy.count;

import lombok.Value;

@Value
public class AmountToken implements ValuedToken<Integer> {
  int amount;

  @Override
  public Integer value() {
    return amount;
  }
}
