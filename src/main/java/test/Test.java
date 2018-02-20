package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(2);
        for(int i = 0 ;i <3 ;i++){
            es.execute(new LongTimeCalculate());
        }
        for(int i = 0 ; i < 5;i++){
            es.execute(new ShortTimeCalculate());
        }
    }
}
