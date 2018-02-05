package client;

import model.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ConcreteClient {
    ClientReactor reactor;

    public ConcreteClient(ClientReactor reactor) {
        this.reactor = reactor;
    }

    CompletableFuture<MessageResponse> echo(MessageRequest messageRequest){
        CompletableFuture<MessageResponse> echoResponseFuture = new CompletableFuture<>();
        reactor.getResponseFutureMapper().put(messageRequest.getId(),echoResponseFuture);
        try {
            reactor.getQueue().offer(messageRequest,1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return echoResponseFuture;
    }

    CompletableFuture<CalculationResponse> calculate(CalculationRequest calculationRequest){
        CompletableFuture<CalculationResponse> calculationResponseFuture = new CompletableFuture<>();
        reactor.getResponseFutureMapper().put(calculationRequest.getId(),calculationResponseFuture);
        try {
            reactor.getQueue().offer(calculationRequest,1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return calculationResponseFuture;
    }
}
