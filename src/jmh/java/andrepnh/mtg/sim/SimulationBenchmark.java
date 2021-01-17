package andrepnh.mtg.sim;

import andrepnh.mtg.sim.jmh.ParallelStrategy;
import andrepnh.mtg.sim.jmh.Step;
import com.google.common.collect.ImmutableMap;
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

  @State(Scope.Benchmark)
  public static class Sequential {
    private ImmutableMap<Step, ParallelStrategy> strategy = ImmutableMap.of();
  }

  @State(Scope.Benchmark)
  public static class ParallelDeckProcessing {
    private ImmutableMap<Step, ParallelStrategy> strategy = ImmutableMap
        .of(Step.DECK, ParallelStrategy.PARALLEL);
  }

  @State(Scope.Benchmark)
  public static class ParallelSimulation {
    private ImmutableMap<Step, ParallelStrategy> strategy = ImmutableMap
        .of(Step.SIMULATE, ParallelStrategy.PARALLEL);
  }

  @Benchmark
  public void baseline(SimulationState state, Sequential stepsStrategy, Blackhole theVoid) {
    new Main(state.games, state.turns, stepsStrategy.strategy)
        .simulate()
        .subscribe(theVoid::consume);
  }

  @Benchmark
  public void parallelDeckProcessing(
      SimulationState state,
      ParallelDeckProcessing stepsStrategy,
      Blackhole theVoid) {
    new Main(state.games, state.turns, stepsStrategy.strategy)
        .simulate()
        .subscribe(theVoid::consume);
  }

  @Benchmark
  public void parallelSimulation(
      SimulationState state,
      ParallelSimulation stepsStrategy,
      Blackhole theVoid) {
    new Main(state.games, state.turns, stepsStrategy.strategy)
        .simulate()
        .subscribe(theVoid::consume);
  }
}
