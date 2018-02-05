package server_new;

import constant.RPC;
import model.*;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public class ReadEventHandler implements Runnable {
    private ConcreteServer server;
    private SelectionKey selectionKey;
    private ServerReactor reactor;

    public ReadEventHandler(SelectionKey selectionKey, ServerReactor reactor) {
        this.selectionKey = selectionKey;
        this.reactor = reactor;
        this.server = new ConcreteServer();
    }

    @Override
    public void run() {
        try {
            BlockingQueue<Response> queue = reactor.getQueue();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            channel.configureBlocking(false);
            channel.read(buffer);
            buffer.flip();
            while (buffer.hasRemaining()) {
                int type = buffer.getInt();
                int id = buffer.getInt();
                int size = buffer.getInt();
                switch (type) {
                    case RPC.ECHO: {
                        byte[] contentAsBytes = new byte[size];
                        buffer.get(contentAsBytes, 0, size);
                        String content = new String(contentAsBytes);
                        MessageRequest messageRequest = new MessageRequest(id, content);
                        System.out.println("Received message: " + messageRequest.getId());

                        CompletableFuture<MessageResponse> messageResponseFuture;//  = new CompletableFuture<>();
                        messageResponseFuture = server.echo(messageRequest);
                        messageResponseFuture.thenAcceptAsync((mess) -> {
                            try {
                                queue.put(mess);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                        });
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    Thread.sleep(new SecureRandom().nextInt(1000));
//                                    MessageResponse messageResponse = new MessageResponse(messageRequest.getId(),messageRequest.getContent());
//                                    System.out.println("MessageResponse: "+messageResponse.getId());
//                                    queue.put(messageResponse);
//                                    messageResponseFuture.complete(messageResponse);
//                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                }
//                            }
//                        }).start();
                        break;
                    }
                    case RPC.CALCULATE:
                        {
                        long num1 = buffer.getLong();
                        long num2 = buffer.getLong();
                        CalculationRequest calculationRequest = new CalculationRequest(id, num1, num2);
                        System.out.println("Received message: " + calculationRequest.getId());

                        CompletableFuture<CalculationResponse> calculationResponseFuture = new CompletableFuture<>();
                        calculationResponseFuture.thenAcceptAsync((mess) -> {
//                            System.out.println("Change selector to OP_WRITE");
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                        });
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(new SecureRandom().nextInt(1000));
                                    CalculationResponse calculationResponse = new CalculationResponse(calculationRequest.getId(),calculationRequest.getNum1()+calculationRequest.getNum2());
                                    System.out.println("CalculationResponse: "+calculationRequest.getId());
                                    queue.put(calculationResponse);
                                    calculationResponseFuture.complete(calculationResponse);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }).start();
                        break;
                    }
                    default: {
                        break;
                    }
                }


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
