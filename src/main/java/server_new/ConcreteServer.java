package server_new;

import model.CalculationRequest;
import model.CalculationResponse;
import model.MessageRequest;
import model.MessageResponse;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

/**
 * Created by imdb on 05/02/2018.
 */
public class ConcreteServer {
    CompletableFuture<MessageResponse> echo(MessageRequest messageRequest){

        CompletableFuture<MessageResponse> messageResponseFuture = new CompletableFuture<>();
        try {
            Thread.sleep(new SecureRandom().nextInt(10000));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        messageResponseFuture.complete(new MessageResponse(messageRequest.getId(), "Echo response: " + messageRequest.getContent()));

        return messageResponseFuture;
    }
    CompletableFuture<CalculationResponse> calculate(CalculationRequest calculationRequest){
        CompletableFuture<CalculationResponse> calculationResponseFuture = new CompletableFuture<>();
        // process long calculation here

        return calculationResponseFuture;
    }
}
