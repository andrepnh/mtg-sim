package andrepnh.mtg.sim;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class SimulationBenchmark {

  @State(Scope.Benchmark)
  public static class SimulationState {
    // To avoid constant folding; do NOT make these final
    // https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_10_ConstantFold.java
    private int games = 3;
    private int turns = 16;
  }

  @Benchmark
  public void baseline(SimulationState state, Blackhole theVoid) {
    new Main(state.games, state.turns)
        .simulate()
        .subscribe(theVoid::consume);
  }
}
