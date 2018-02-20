package test;

public class ShortTimeCalculate implements Runnable{
    @Override
    public void run() {
        System.out.println("Start shortTimeCalculate");
        System.out.println("Thread running: "+Thread.activeCount());
    }
}
