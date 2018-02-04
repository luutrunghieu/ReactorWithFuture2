package client;

import model.Message;
import model.MessageResponse;

import java.util.concurrent.CompletableFuture;

public class RunClient {
    public static final int NUM_REQUEST = 5;
    public static void main(String[] args) throws InterruptedException {
        ClientReactor reactor = new ClientReactor("127.0.0.1",9999);
        new Thread(reactor).start();
        ConcreteClient client = new ConcreteClient(reactor);
        for(int i = 0; i< NUM_REQUEST;i++){
            Message message = new Message(i,i+"");
            CompletableFuture<MessageResponse> echoResponseFuture = client.echo(message);
            echoResponseFuture.thenAcceptAsync((response)->{
                System.out.println("Complete "+response.getContent());
            });
        }
    }
}
