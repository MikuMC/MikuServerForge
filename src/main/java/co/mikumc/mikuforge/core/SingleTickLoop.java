package co.mikumc.mikuforge.core;

import catserver.server.CatServer;
import co.earthme.hearse.utils.NormalTickThread;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

public class SingleTickLoop extends NormalTickThread implements IThreadListener {
    private static final Logger logger = LogManager.getLogger();

    private final World world;
    private int tickCounter;
    private final Queue<Runnable> tasks = new ConcurrentLinkedDeque<>();
    private final Thread instance = this;
    private volatile boolean running = true;
    private long mspt = 0;
    private long lastTickTime = 0;
    private long lastLastedTime = 0;
    private int id;

    public SingleTickLoop(World world){
        this.world = world;
    }

    public void stopLoop(){
        this.running = false;
    }

    public World getWorld(){
        return this.world;
    }

    public long getLastTickTime() {
        return this.lastTickTime;
    }

    public long getMSPT(){
        return this.mspt;
    }

    @Override
    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
        try {
            ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.create(Executors.callable(runnableToSchedule));
            this.postTask(listenablefuturetask);
            return listenablefuturetask;
        } catch (Exception e) {
            return Futures.immediateFailedCheckedFuture(e);
        }
    }

    @Override
    public boolean isCallingFromMinecraftThread() {
        return NormalTickThread.isNormalTickThread();
    }

    @Override
    public void run(){
        while (this.running){
            this.lastTickTime = System.nanoTime();
            try {
                this.tickCounter++;
                this.doTickInternal();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                this.mspt = System.nanoTime() - this.lastTickTime;
                if ((50_000_000L - (this.mspt + this.lastLastedTime)) > 0){
                    LockSupport.parkNanos(50_000_000L - this.mspt - this.lastLastedTime);
                    this.lastTickTime = 0;
                }else{
                    this.lastLastedTime = this.mspt - 50_000_000L;
                }
                if (this.tickCounter % 20 == 0 && this.lastLastedTime > 0){
                    logger.warn("Can't keep up!Is the server overloaded?Running {} nanos lasted",this.mspt);
                }
            }
        }
    }

    private void runMainThreadTasks(){
        Runnable task;
        while ((task = this.tasks.poll())!=null){
            try {
                task.run();
            }catch (Exception e){
                logger.error("Failed to run task!");
                e.printStackTrace();
            }
        }
    }

    public void postTask(Runnable task){
        this.tasks.add(task);
    }

    private void doTickInternal(){
        this.runMainThreadTasks();
        org.bukkit.craftbukkit.chunkio.ChunkIOExecutor.tick();
        net.minecraftforge.common.chunkio.ChunkIOExecutor.tick();
        catserver.server.command.internal.CommandChunkStats.onServerTick();

        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreWorldTick(this.world);
        Integer[] ids = net.minecraftforge.common.DimensionManager.getIDs(this.tickCounter % 200 == 0);
        for (int id : ids){
            WorldServer worldserver = DimensionManager.getWorld(id);
            if (this.world.equals(worldserver)){
                this.id = id;
            }
        }

        long i = System.nanoTime();
        net.minecraft.tileentity.TileEntityHopper.skipHopperEvents = org.bukkit.event.inventory.InventoryMoveItemEvent.getHandlerList().getRegisteredListeners().length == 0 || CatServer.getConfig().disableHopperMoveEventWorlds.contains("*") || CatServer.getConfig().disableHopperMoveEventWorlds.contains(this.world.getWorld().getName()); // CatServer

        try {
            this.world.tick();
            this.world.waitTasks();
        } catch (Throwable throwable1) {
            throwable1.printStackTrace();
        }

        try {
            this.world.updateEntities();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostWorldTick(this.world);
        ((WorldServer)this.world).getEntityTracker().tick();
        this.world.explosionDensityCache.clear();

        if (MinecraftServer.getServerInst().worldTickTimes.containsKey(this.id))
            MinecraftServer.getServerInst().worldTickTimes.get(this.id)[this.tickCounter % 100] = System.nanoTime() - i;

        for (EntityPlayer player : this.world.playerEntities){
            final EntityPlayerMP playerMP = (EntityPlayerMP) player;
            MinecraftServer.getServerInst().getNetworkSystem().tickSingle(playerMP.connection.netManager);
        }
    }
}
