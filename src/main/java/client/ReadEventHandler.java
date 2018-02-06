package client;

import model.CalculationResponse;
import model.MessageResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;

public class ReadEventHandler implements Runnable {
    private ClientReactor reactor;
    private SelectionKey selectionKey;

    public ReadEventHandler(ClientReactor reactor, SelectionKey selectionKey) {
        this.reactor = reactor;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try {
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
                    case 1: {
                        byte[] contentAsBytes = new byte[size];
                        buffer.get(contentAsBytes, 0, size);
                        String content = new String(contentAsBytes);
                        MessageResponse response = new MessageResponse(id, content);
//                        System.out.println("Received : " + response.getId()+ " - Type: "+response.getType());
                        CompletableFuture<MessageResponse> echoResponseFuture = (CompletableFuture<MessageResponse>) reactor.getResponseFutureMapper().get(id);
                        echoResponseFuture.complete(response);
                        break;
                    }
                    case 2: {
                        long result = buffer.getLong();
                        CalculationResponse response = new CalculationResponse(id, result);
//                        System.out.println("Received : " + response.getId()+" - Type: "+response.getType());
                        CompletableFuture<CalculationResponse> calcalateResponseFuture = (CompletableFuture<CalculationResponse>) reactor.getResponseFutureMapper().get(id);
                        calcalateResponseFuture.complete(response);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                reactor.getResponseFutureMapper().remove(id);
            }
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
