package co.earthme.hearse.utils;

public class NormalTickThread extends Thread{
    public static boolean isNormalTickThread(){
        return Thread.currentThread() instanceof NormalTickThread;
    }
}
