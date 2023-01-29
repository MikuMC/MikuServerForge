package co.earthme.hearse.utils;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class TickThread extends ForkJoinWorkerThread {
    /**
     * Creates a ForkJoinWorkerThread operating in the given pool.
     *
     * @param pool the pool this thread works in
     * @throws NullPointerException if pool is null
     */
    protected TickThread(ForkJoinPool pool) {
        super(pool);
    }
}
