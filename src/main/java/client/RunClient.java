package client;

import model.CalculationRequest;
import model.CalculationResponse;
import model.MessageRequest;
import model.MessageResponse;

import java.util.concurrent.CompletableFuture;

public class RunClient {
    public static final int NUM_REQUEST_ECHO = 50;
    public static final int NUM_REQUEST_CALCULATE = 50;
    public static void main(String[] args) throws InterruptedException {
        ClientReactor reactor = new ClientReactor("127.0.0.1",9999);
        new Thread(reactor).start();
        ConcreteClient client = new ConcreteClient(reactor);
        for(int i = 0; i< NUM_REQUEST_ECHO;i++){
            MessageRequest messageRequest = new MessageRequest(i,i+"aaaa");
            CompletableFuture<MessageResponse> echoResponseFuture = client.echo(messageRequest);
            echoResponseFuture.thenAcceptAsync((response)->{
                System.out.println("Complete "+response.getId()+" - Type: "+response.getType());
            });
        }
        for(int i = 0+NUM_REQUEST_ECHO; i< NUM_REQUEST_CALCULATE+NUM_REQUEST_ECHO;i++){
            CalculationRequest calculationRequest = new CalculationRequest(i,i,i);
            CompletableFuture<CalculationResponse> calculateResponseFuture = client.calculate(calculationRequest);
            calculateResponseFuture.thenAcceptAsync((response)->{
                System.out.println("Complete "+response.getId()+" - Type: "+response.getType());
            });
        }
    }
}
