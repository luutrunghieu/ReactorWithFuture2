package server_new;

import constant.RPC;
import model.*;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

/**
 * Created by imdb on 06/02/2018.
 */
public class ProcessMessage implements Runnable {
    private Handler handler;
    private Request request;
    private CompletableFuture completableFuture;

    public ProcessMessage(Handler handler, Request request, CompletableFuture completableFuture) {
        this.handler = handler;
        this.request = request;
        this.completableFuture = completableFuture;
    }

    @Override
    public void run() {
        try {
//            System.out.println("Run process message");
            int type = request.getType();
            Response response = null;
            switch (type) {
                case RPC.ECHO: {
                    response = processEchoMessage();
                    break;
                }
                case RPC.CALCULATE: {
                    response = processCalculateMessage();
                    break;
                }
                default: {
                    break;
                }
            }
            handler.getWritePendingQueue().put(response);
            completableFuture.complete(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public Response processEchoMessage() throws Exception {
//        Thread.sleep(new SecureRandom().nextInt(1000));
        MessageRequest messageRequest = (MessageRequest) request;
        MessageResponse messageResponse = new MessageResponse(messageRequest.getId(), messageRequest.getContent());
//        System.out.println("MessageResponse: " + messageResponse.getId());
        return messageResponse;
    }

    public Response processCalculateMessage() throws Exception {
//        Thread.sleep(new SecureRandom().nextInt(1000));
        CalculationRequest calculationRequest = (CalculationRequest) request;
        CalculationResponse calculationResponse = new CalculationResponse(calculationRequest.getId(), calculationRequest.getNum1() + calculationRequest.getNum2());
//        System.out.println("CalculationResponse: " + calculationRequest.getId());
        return calculationResponse;
    }
}
