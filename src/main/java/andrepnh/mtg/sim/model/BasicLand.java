package andrepnh.mtg.sim.model;

import java.util.Arrays;
import java.util.stream.Stream;

public enum BasicLand implements Land {
  PLAINS("Plains"), ISLAND("Island"), SWAMP("Swamp"), MOUNTAIN("Mountain"), FOREST("Forest");

  private final String name;

  BasicLand(String name) {
    this.name = name;
  }

  public static BasicLand ofName(String name) {
    return Stream.of(values())
        .filter(val -> val.getName().equals(name))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("No basic land named " + name));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
