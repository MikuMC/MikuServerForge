package org.spigotmc;

import co.earthme.hearse.utils.TickThread;
import net.minecraft.server.MinecraftServer;

public class AsyncCatcher
{

    public static boolean enabled = false;

    public static void catchOp(String reason)
    {
        if ( enabled && Thread.currentThread() != MinecraftServer.getServerInst().primaryThread && !(Thread.currentThread() instanceof TickThread) && !co.earthme.hearse.utils.NormalTickThread.isNormalTickThread())
        {
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
