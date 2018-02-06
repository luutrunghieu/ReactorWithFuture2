package server_new;

import constant.RPC;
import model.*;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public class ReadEventHandler implements Runnable {
    private SelectionKey selectionKey;
    private ServerReactor reactor;

    public ReadEventHandler(SelectionKey selectionKey, ServerReactor reactor) {
        this.selectionKey = selectionKey;
        this.reactor = reactor;
    }

    @Override
    public void run() {
        try {
            BlockingQueue<Response> queue = reactor.getWritePendingQueue();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            channel.configureBlocking(false);
            channel.read(buffer);
            buffer.flip();
            while (buffer.hasRemaining()) {
                Request request = null;
                CompletableFuture completableFuture = new CompletableFuture();
                int type = buffer.getInt();
                int id = buffer.getInt();
                int size = buffer.getInt();
                switch (type) {
                    case RPC.ECHO: {
                        byte[] contentAsBytes = new byte[size];
                        buffer.get(contentAsBytes, 0, size);
                        String content = new String(contentAsBytes);
                        request = new MessageRequest(id, content);
                        break;
                    }
                    case RPC.CALCULATE: {
                        long num1 = buffer.getLong();
                        long num2 = buffer.getLong();
                        request = new CalculationRequest(id, num1, num2);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                System.out.println("Received message: " + request.getId());
                reactor.getPool().submit(new ProcessMessage(reactor, request, completableFuture));
                completableFuture.thenAcceptAsync((mess) -> {
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
