package co.mikumc.mikuforge.core;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.util.List;

public class TickLoopManager {
    private final List<SingleTickLoop> tickLoopList = ObjectLists.synchronize(new ObjectArrayList<>());

    public void register(SingleTickLoop loop){
        this.tickLoopList.add(loop);
        loop.setPriority(8);
        loop.start();
    }

    public boolean has(SingleTickLoop loop){
        return this.tickLoopList.contains(loop);
    }

    public void stopAll(){
        for (SingleTickLoop loop : this.tickLoopList){
            loop.stopLoop();
        }
    }
}
