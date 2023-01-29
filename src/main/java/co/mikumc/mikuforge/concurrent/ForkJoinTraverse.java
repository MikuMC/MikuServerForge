package co.mikumc.mikuforge.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ForkJoinTraverse<E> extends RecursiveAction {
    private final Spliterator<E> spliterator;
    private final Consumer<E> action;
    private final long threshold;

    public ForkJoinTraverse(Iterable<E> iterable, int threads, Consumer<E> action){
        this.spliterator = iterable.spliterator();
        this.action = action;
        this.threshold = Math.max(5,(int)iterable.spliterator().getExactSizeIfKnown() / threads);
    }

    private ForkJoinTraverse(Spliterator<E> spliterator, Consumer<E> action, long t) {
        this.spliterator = spliterator;
        this.action = action;
        this.threshold = t;
    }

    @Override
    protected void compute() {
        if (this.spliterator.getExactSizeIfKnown() <= this.threshold) {
            this.spliterator.forEachRemaining(o->{
                try {
                    this.action.accept(o);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        } else {
            new ForkJoinTraverse<>(this.spliterator.trySplit(), this.action, this.threshold).fork();
            new ForkJoinTraverse<>(this.spliterator, this.action, this.threshold).fork();
        }
    }

    public static <T> ForkJoinTraverse<T> createNewStarted(Iterable<T> input, Consumer<T> action, ForkJoinPool pool){
        final ForkJoinTraverse<T> newTask = new ForkJoinTraverse<>(input,pool.getParallelism(),action);
        pool.execute(newTask);
        return newTask;
    }

    public static <T,K> CompletableFuture<List<K>> createNewApplyAction(Iterable<T> input, Function<T,K> action,ForkJoinPool pool){
        final CompletableFuture<List<K>> ret = new CompletableFuture<>();
        final List<K> valueKs = new ArrayList<>();
        final AtomicInteger totalCompleted = new AtomicInteger();
        final int allCount = (int) input.spliterator().getExactSizeIfKnown();
        final ForkJoinTraverse<T> newTask = new ForkJoinTraverse<>(input,pool.getParallelism(),valueT -> {
            final K result = action.apply(valueT);
            synchronized (valueKs){
                valueKs.add(result);
            }

            if (totalCompleted.incrementAndGet() == allCount){
                ret.complete(valueKs);
            }
        });
        pool.execute(newTask);
        return ret;
    }
}