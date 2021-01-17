package andrepnh.mtg.sim.analytics.synergy.count;

import lombok.Value;

@Value
public class CardToken implements ValuedToken<String> {
  String name;

  @Override
  public String value() {
    return name;
  }
}
