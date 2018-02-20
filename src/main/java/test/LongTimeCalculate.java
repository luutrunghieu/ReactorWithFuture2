package test;

public class LongTimeCalculate implements Runnable{
    @Override
    public void run() {
        try {
            System.out.println("Start longTimeCalculate");
            Thread.sleep(5000);
            System.out.println("Back to longTimeCalculate");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
