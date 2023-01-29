package co.earthme.hearse.utils;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

public class PublicConstants{
    private static final AtomicInteger entityWorkerThreadIdCounter = new AtomicInteger();
    public static final ForkJoinPool worldEntityWorker = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            pool -> {
                TickThread worker = new TickThread(pool){};
                worker.setDaemon(true);
                worker.setContextClassLoader(net.minecraft.server.MinecraftServer.class.getClassLoader());
                worker.setPriority(Thread.NORM_PRIORITY - 2);
                worker.setName("Hearse-Entity-ForkJoinPool-Worker-"+entityWorkerThreadIdCounter.getAndIncrement());
                return worker;
            },
            null,
            true
    );

    public static boolean asyncEntity = true;

    private static final AtomicInteger randomTickThreadIdCounter = new AtomicInteger();
    public static final ForkJoinPool worldRandomTickWorker = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            pool -> {
                TickThread worker = new TickThread(pool){};
                worker.setDaemon(true);
                worker.setContextClassLoader(net.minecraft.server.MinecraftServer.class.getClassLoader());
                worker.setPriority(Thread.NORM_PRIORITY - 2);
                worker.setName("Hearse-Entity-ForkJoinPool-Worker-"+randomTickThreadIdCounter.getAndIncrement());
                return worker;
            },
            null,
            true
    );

    private static final AtomicInteger worldThreadIdCounter = new AtomicInteger();
    public static ForkJoinPool worldWorker;

    public static void initWorldWorker(int count){
        if (worldWorker == null){
            worldWorker = new ForkJoinPool(
                    count,
                    pool -> {
                        TickThread worker = new TickThread(pool){};
                        worker.setDaemon(true);
                        worker.setContextClassLoader(net.minecraft.server.MinecraftServer.class.getClassLoader());
                        worker.setPriority(Thread.NORM_PRIORITY + 2);
                        worker.setName("Hearse-World-ForkJoin-Worker-"+worldThreadIdCounter.getAndIncrement());
                        return worker;
                    },
                    null,
                    true
            );
        }
    }
}
