package andrepnh.mtg.sim.model;

public interface Card {
  String getName();

  default boolean isLand() {
    return this instanceof Land;
  }
}
