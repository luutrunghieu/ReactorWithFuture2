package client;

import model.Message;
import model.MessageResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ConcreteClient {
    ClientReactor reactor;

    public ConcreteClient(ClientReactor reactor) {
        this.reactor = reactor;
    }

    CompletableFuture<MessageResponse> echo(Message message){
        CompletableFuture<MessageResponse> echoResponseFuture = new CompletableFuture<>();
        reactor.getMessageFutureMapper().put(message.getId(),echoResponseFuture);
        try {
            reactor.getQueue().offer(message,1, TimeUnit.SECONDS);
//            System.out.println("Queue size: "+reactor.getQueue().size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return echoResponseFuture;
    }
}
