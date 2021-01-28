package andrepnh.mtg.sim.model;

public interface HybridMana<T1> {
  T1 option1();

  Mana option2();

  default boolean isMonocoloredHybrid() {
    return option1() instanceof Integer;
  }
}
