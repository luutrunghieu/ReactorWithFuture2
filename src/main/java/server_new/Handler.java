package server_new;

import constant.RPC;
import model.CalculationRequest;
import model.MessageRequest;
import model.Request;
import model.Response;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

/**
 * Created by imdb on 06/02/2018.
 */
public class Handler implements Runnable {
    ServerReactorNew reactor;
    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    static final int READING = 0, WRITING = 1;
    int state = READING;

    Handler(Selector selector, SocketChannel socketChannel, ServerReactorNew reactor) throws Exception {
        this.reactor = reactor;
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            if (state == READING) {
                read();
            } else if (state == WRITING) {
                write();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void read() throws Exception {
        try {
            BlockingQueue<Response> queue = reactor.getWritePendingQueue();
            int readCount = socketChannel.read(buffer);
            if (readCount > 0) {
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
                        state = WRITING;
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    });
                }
                buffer.clear();
                state = WRITING;
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            } else {
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        int readCount = socketChannel.read(input);
//        if (readCount > 0) {
//            readProcess(readCount);
//        }
//        state = WRITING;
//        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    void write() throws Exception {
//        if (clientName.equalsIgnoreCase("Bye")) {
//            socketChannel.close();
//        } else {
////            System.out.println("Saying hello to " + clientName);
//            ByteBuffer output = ByteBuffer.wrap((clientName + "\n").getBytes());
//            socketChannel.write(output);
//            selectionKey.interestOps(SelectionKey.OP_READ);
//            state = READING;
//            System.out.println("Sent: "+clientName);
//        }
        try {
            BlockingQueue<Response> queue = reactor.getWritePendingQueue();
            if (!queue.isEmpty()) {
                while (!queue.isEmpty()) {
                    Response response = queue.take();
                    ByteBuffer buffer = ByteBuffer.wrap(response.serialize());
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    channel.configureBlocking(false);
                    channel.write(buffer);
//                    System.out.println("Sent: " + response.getId() + " - Type: " + response.getType());
                }
            }
//            System.out.println("Change selector to OP_READ - threads = " + Thread.activeCount());
            state = READING;
            selectionKey.interestOps(SelectionKey.OP_READ);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
