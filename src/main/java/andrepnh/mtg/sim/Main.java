package andrepnh.mtg.sim;

import andrepnh.mtg.sim.analytics.Analysis;
import andrepnh.mtg.sim.analytics.Report;
import andrepnh.mtg.sim.analytics.format.FormatterFactory;
import andrepnh.mtg.sim.analytics.mana.AvailableManaAnalysis;
import andrepnh.mtg.sim.analytics.mana.ManaCurveAnalysis;
import andrepnh.mtg.sim.analytics.synergy.SynergyAnalysis;
import andrepnh.mtg.sim.analytics.synergy.SynergyInputImporter;
import andrepnh.mtg.sim.card.api.ClientFacade;
import andrepnh.mtg.sim.in.DeckImporter;
import andrepnh.mtg.sim.in.FileSystemDeckImporter;
import andrepnh.mtg.sim.model.Deck;
import andrepnh.mtg.sim.model.Library;
import andrepnh.mtg.sim.sim.GameState;
import andrepnh.mtg.sim.sim.JavaShuffler;
import andrepnh.mtg.sim.sim.Shuffler;
import andrepnh.mtg.sim.sim.SimplePlayStrategy;
import andrepnh.mtg.sim.sim.Simulation;
import com.google.common.collect.Sets;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.tools.agent.ReactorDebugAgent;

@Slf4j
@RequiredArgsConstructor
public class Main {
  public static final Path INPUT_DIR = Path.of(".", "decks");
  private static final int TURNS = 16; // Opening hand is turn 0
  private static final int GAMES_TO_SIMULATE = 10000;
  public static final AtomicInteger GLOBAL_TURN_COUNTER = new AtomicInteger();
  public static final Set<Library> UNIQUE_LIBRARIES = Sets.newConcurrentHashSet();

  final int gamesToSimulate;
  final int turns;

  @SneakyThrows
  public static void main(String[] args) {
    ReactorDebugAgent.init();
    var formatterFactory = new FormatterFactory();
    Flux<? extends Tuple2<Deck, Report>> reportsPerDeck
        = new Main(GAMES_TO_SIMULATE, TURNS).simulate();
    reportsPerDeck.subscribe(
        deckReportPair -> {
            var formatter = formatterFactory.getInstance(deckReportPair._2);
            var reportFile = String.format("%s - %s.%s",
                deckReportPair._1.getName(), deckReportPair._2.getName(), formatter.getFileExtension());
            List<String> lines = formatter.format();
            write(reportFile, lines);
        }
    );
  }

  private static void write(String reportFile, List<String> lines) {
    try {
      Files.write(Path.of("reports").resolve(reportFile), lines);
    } catch (IOException e) {
      log.error("Could not create report file {}", reportFile, e);
    }
  }

  @SneakyThrows
  public Flux<? extends Tuple2<Deck, Report>> simulate() {
    var shuffler = new JavaShuffler();
    var deckImporter = new FileSystemDeckImporter(
        new DeckImporter(new ClientFacade()));

    Flux<Deck> decks = deckImporter.importDecks();
    Flux<Analysis<? extends Report>> analysis = getAnalysis(decks);

    Flux<Tuple2<Deck, ParallelFlux<GameState>>> statesPerDeck = simulateGames(shuffler, decks);
    Flux<? extends Tuple2<Deck, Report>> reportsPerDeck
        = runAnalyses(analysis, statesPerDeck);

    return reportsPerDeck;
  }

  private Flux<Analysis<? extends Report>> getAnalysis(Flux<Deck> decks) {
    var synergiesInputImported = new SynergyInputImporter();
    Flux<Analysis<? extends Report>> synergyAnalysis = decks
        .map(deck -> Tuple.of(deck, synergiesInputImported.forDeck(deck)))
        .filter(deckSynergiesPair -> !deckSynergiesPair._2.isEmpty())
        .map(pair -> pair.apply((d, s) -> new SynergyAnalysis(d, s, gamesToSimulate)));
    return Flux.concat(
        synergyAnalysis,
        Flux.just(new AvailableManaAnalysis(), new ManaCurveAnalysis()));
  }

  private Flux<? extends Tuple2<Deck, Report>> runAnalyses(
      Flux<Analysis<? extends Report>> analysis,
      Flux<Tuple2<Deck, ParallelFlux<GameState>>> statesPerDeck) {
    return statesPerDeck
          .flatMap(
              deckStates -> analysis
                    .filter(a -> a.appliesTo(deckStates._1))
                    .concatMap(
                        a -> {
                          log.debug("{} - Applying {}", deckStates._1.getName(), a.getClass().getSimpleName());
                          return a.analyze(deckStates._1, deckStates._2);
                        })
                    .map(report -> Tuple.of(deckStates._1, report)),
              Runtime.getRuntime().availableProcessors());
  }

  private Flux<Tuple2<Deck, ParallelFlux<GameState>>> simulateGames(
      Shuffler shuffler,
      Flux<Deck> decks) {
    return decks
          .map(deck -> new Simulation(
              shuffler, deck, SimplePlayStrategy.INSTANCE))
          .map(simulation -> Tuple.of(
              simulation.getDeck(),
              simulation.runFor(gamesToSimulate, turns)));
  }
}
