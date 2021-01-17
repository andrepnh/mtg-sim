package andrepnh.mtg.sim.analytics.synergy.count;

public enum MatchType implements ValuedToken<MatchType> {
  EXACT, AT_LEAST;

  @Override
  public MatchType value() {
    return this;
  }
}
