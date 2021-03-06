package client_new;
import model.CalculationRequest;
import model.CalculationResponse;
import model.MessageRequest;
import model.MessageResponse;
import monitor.MonitorThread;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RunClient {
    public static final int NUM_REQUEST_ECHO = 20000;
    public static final int NUM_REQUEST_CALCULATE = 50000;
    public static void main(String[] args) throws Exception {

        ClientReactorNew reactor = new ClientReactorNew("127.0.0.1",9999);
        new Thread(reactor).start();

        ConcreteClient client = new ConcreteClient(reactor);

        int i = 0;
        while(true){
            int messageType = new SecureRandom().nextInt(1);
            if(messageType == 0){
                MessageRequest messageRequest = new MessageRequest(i,i+"aaaa");
                CompletableFuture<MessageResponse> echoResponseFuture = client.echo(messageRequest);
                echoResponseFuture.thenAcceptAsync((response) -> {
//                    System.out.println("Complete " + response.getId() + " - Type: " + response.getType());
                });
            } else if(messageType == 1){
                CalculationRequest calculationRequest = new CalculationRequest(i,i,i);
                CompletableFuture<CalculationResponse> calculateResponseFuture = client.calculate(calculationRequest);
                calculateResponseFuture.thenAcceptAsync((response) -> {
//                    System.out.println("Complete " + response.getId() + " - Type: " + response.getType());
                });
            }
            i++;
        }
//        for(int i = 0; i< NUM_REQUEST_ECHO;i++){
//            MessageRequest messageRequest = new MessageRequest(i,i+"aaaa");
//            CompletableFuture<MessageResponse> echoResponseFuture = client.echo(messageRequest);
//            echoResponseFuture.thenAcceptAsync((response)->{
////                System.out.println("Complete "+response.getId()+" - Type: "+response.getType());
//            });
//        }
//        for(int i = 0+NUM_REQUEST_ECHO; i< NUM_REQUEST_CALCULATE+NUM_REQUEST_ECHO;i++){
//            CalculationRequest calculationRequest = new CalculationRequest(i,i,i);
//            CompletableFuture<CalculationResponse> calculateResponseFuture = client.calculate(calculationRequest);
//            calculateResponseFuture.thenAcceptAsync((response)->{
////                System.out.println("Complete "+response.getId()+" - Type: "+response.getType());
//            });
//        }
    }
}
