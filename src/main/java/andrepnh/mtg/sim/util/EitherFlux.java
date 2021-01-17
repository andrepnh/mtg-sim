package andrepnh.mtg.sim.util;

import com.google.common.collect.Maps;
import io.vavr.control.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import lombok.Value;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

@Value(staticConstructor = "of")
public class EitherFlux<T> {
  private static final float MAP_LOAD_FACTOR = 0.75F;

  Either<ParallelFlux<T>, Flux<T>> either;

  public <K, V> Mono<Map<K, V>> collectMap(
      Function<? super T, ? extends K> keyExtractor,
      Function<? super T, ? extends V> valueExtractor) {
    return either.fold(
        p -> p
            .collect(
                (Supplier<Map<K, V>>) Maps::newHashMap,
                (m, e) -> m.put(keyExtractor.apply(e), valueExtractor.apply(e)))
            .reduce((m1, m2) -> {
              int expectedSize = m1.size() + m2.size();
              int capacity = (int) ((float) expectedSize / MAP_LOAD_FACTOR + 1.0F);
              var reduced = new ConcurrentHashMap<K, V>(capacity, MAP_LOAD_FACTOR, 2);
              reduced.putAll(m1);
              reduced.putAll(m2);
              return reduced;
            }),
        f -> f.collectMap(keyExtractor, valueExtractor));
  }

  public static <T> EitherFlux<T> of(ParallelFlux<T> parallelFlux) {
    return of(Either.left(parallelFlux));
  }

  public static <T> EitherFlux<T> of(Flux<T> parallelFlux) {
    return of(Either.right(parallelFlux));
  }

  public EitherFlux<T> doOnNext(Consumer<? super T> action) {
    return of(either.bimap(p -> p.doOnNext(action), f -> f.doOnNext(action)));
  }

  public EitherFlux<T> filter(Predicate<? super T> pred) {
    return of(either.bimap(p -> p.filter(pred), f -> f.filter(pred)));
  }

  public <R> EitherFlux<R> flatMapSequential(
      Function<? super T, ? extends Publisher<? extends R>> mapper) {
    Flux<R> mapped = sequentialFlux().flatMapSequential(mapper);
    Either<ParallelFlux<R>, Flux<R>> either = Either.right(mapped);
    return of(either);
  }

  public <R> EitherFlux<R> flatMap(
      Function<? super T, ? extends Publisher<? extends R>> mapper) {
    return of(either.bimap(p -> p.flatMap(mapper), f -> f.flatMap(mapper)));
  }

  public <R> EitherFlux<R> flatMap(
      Function<? super T, ? extends Publisher<? extends R>> mapper,
      int concurrency) {
    // TODO revert to Flux instead of ignoring concurrency?
    return of(either.bimap(
        p -> p.flatMap(mapper),
        f -> f.flatMap(mapper, concurrency)));
  }

  public <K> EitherFlux<GroupedFlux<K, T>> groupBy(
      Function<? super T, ? extends K> keyMapper) {
    Flux<GroupedFlux<K, T>> grouped = sequentialFlux().groupBy(keyMapper);
    Either<ParallelFlux<GroupedFlux<K, T>>, Flux<GroupedFlux<K, T>>> either = Either.right(grouped);
    return of(either);
  }

  public <K, V> EitherFlux<GroupedFlux<K, V>> groupBy(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends V> valueMapper) {
    Flux<GroupedFlux<K, V>> grouped = sequentialFlux().groupBy(keyMapper, valueMapper);
    Either<ParallelFlux<GroupedFlux<K, V>>, Flux<GroupedFlux<K, V>>> either = Either.right(grouped);
    return of(either);
  }

  public <V> EitherFlux<V> map(Function<? super T, ? extends V> mapper) {
    return of(either.bimap(p -> p.map(mapper), f -> f.map(mapper)));
  }

  public Mono<T> reduce(BiFunction<T, T, T> aggregator) {
    return either.fold(p -> p.reduce(aggregator), f -> f.reduce(aggregator));
  }

  public <A> Mono<A> reduce(A initial,
      BiFunction<A, ? super T, A> accumulator) {
    return either.fold(
        p -> p.reduce(() -> initial, accumulator)
            .reduce((BiFunction<A, A, A>) accumulator),
        f -> f.reduce(initial, accumulator));
  }

  public <A> Mono<A> reduceWith(Supplier<A> initial,
      BiFunction<A, ? super T, A> accumulator) {
    return either.fold(
        p -> p.reduce(initial, accumulator)
            .reduce((BiFunction<A, A, A>) accumulator),
        f -> f.reduce(initial.get(), accumulator));
  }

  public EitherFlux<T> sort(Comparator<? super T> sortFunction) {
    Flux<T> sorted = either
        .bimap(
            p -> p.sorted(sortFunction),
            f -> f.sort(sortFunction))
        .fold(Function.identity(), Function.identity());
    return of(Either.right(sorted));
  }

  public Disposable subscribe(Consumer<? super T> consumer) {
    return either.fold(
        p -> p.subscribe(consumer),
        f -> f.subscribe(consumer));
  }

  public Disposable subscribe(Consumer<? super T> consumer,
      Consumer<? super Throwable> errorConsumer) {
    return either.fold(
        p -> p.subscribe(consumer, errorConsumer),
        f -> f.subscribe(consumer, errorConsumer));
  }

  public Disposable subscribe(Consumer<? super T> consumer,
      Consumer<? super Throwable> errorConsumer,
      Runnable onComplete) {
    return either.fold(
        p -> p.subscribe(consumer, errorConsumer, onComplete),
        f -> f.subscribe(consumer, errorConsumer, onComplete));
  }

  public EitherFlux<T> sequential() {
    return of(Either.right(sequentialFlux()));
  }

  public Flux<T> sequentialFlux() {
    return either.fold(ParallelFlux::sequential, Function.identity());
  }

  public EitherFlux<T> parallel() {
    return of(Either.left(parallelFlux()));
  }

  public ParallelFlux<T> parallelFlux() {
    return either.fold(Function.identity(), Flux::parallel);
  }

  public <R, A> Mono<R> collect(Collector<? super T, A, ? extends R> collector) {
    return sequentialFlux().collect(collector);
  }

  public Mono<List<T>> collectList() {
    return either.fold(
        p -> p
            .collect(
                (Supplier<List<T>>) ArrayList::new,
                List::add)
            .reduce((l1, l2) -> {
              var reduced = Collections.synchronizedList(new ArrayList<T>(l1.size() + l2.size()));
              reduced.addAll(l1);
              reduced.addAll(l2);
              return reduced;
            }),
        Flux::collectList);
  }
}
