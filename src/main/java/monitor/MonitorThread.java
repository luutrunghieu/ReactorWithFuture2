package monitor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by imdb on 01/02/2018.
 */
public class MonitorThread implements Runnable {
    private AtomicInteger sendCount, receiveCount;

    public MonitorThread() {
        sendCount = new AtomicInteger();
        receiveCount = new AtomicInteger();
    }

    public AtomicInteger getSendCount() {
        return sendCount;
    }

    public void setSendCount(AtomicInteger sendCount) {
        this.sendCount = sendCount;
    }

    public AtomicInteger getReceiveCount() {
        return receiveCount;
    }

    public void setReceiveCount(AtomicInteger receiveCount) {
        this.receiveCount = receiveCount;
    }


    @Override
    public void run() {
        while (true) {
            System.out.println("Send Count: " + sendCount);
            System.out.println("Receive Count: " + receiveCount);
            System.out.println("-------------------");
            sendCount.set(0);
            receiveCount.set(0);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
