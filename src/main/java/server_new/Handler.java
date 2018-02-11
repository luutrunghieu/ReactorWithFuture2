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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by imdb on 06/02/2018.
 */
public class Handler implements Runnable {
    static ExecutorService pool = Executors.newFixedThreadPool(2);
    ServerReactorNew reactor;
    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    static final int READING = 0, WRITING = 1, PROCESSING = 2, WAITING = 3;
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

    void read() {
        try {
            state = PROCESSING;
            BlockingQueue<Response> queue = reactor.getWritePendingQueue();
            while (true) {
                Request request = null;
                CompletableFuture completableFuture = new CompletableFuture();
                buffer.limit(12);
                int readCount = socketChannel.read(buffer);
                if (readCount == 0) {
                    if (!queue.isEmpty()) {
                        state = WAITING;
                    } else {
                        state = READING;
                    }
                    break;
                }

                buffer.flip();
                int type = buffer.getInt();
                int id = buffer.getInt();
                int size = buffer.getInt();
                System.out.println("Type: " + type);
                System.out.println("ID: " + id);
                System.out.println("Size: " + size);
                buffer.limit(12 + size);
                socketChannel.read(buffer);
                buffer.position(12);
                switch (type) {
                    case RPC.ECHO: {
                        byte[] contentAsBytes = new byte[size];
                        buffer.get(contentAsBytes, 0, size);
                        String content = new String(contentAsBytes);
                        System.out.println("Content: " + content);
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
                buffer.clear();
                System.out.println("Received message: " + request.getId());
                pool.execute(new ProcessMessage(reactor, request, completableFuture));
                completableFuture.thenAcceptAsync((mess) -> {
                    if (state == WAITING || state == READING) {
                        state = WRITING;
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                        System.out.println("Chuyen sang write");
                    }
                });
            }
        } catch (Exception ex) {
            System.out.println("State when throwing exception: " + state);
            System.out.println("Queue size: " + reactor.getWritePendingQueue().size());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    void write() {
        try {
            BlockingQueue<Response> queue = reactor.getWritePendingQueue();
            if (!queue.isEmpty()) {
                while (!queue.isEmpty()) {
                    Response response = queue.take();
                    ByteBuffer buffer = ByteBuffer.wrap(response.serialize());
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    channel.configureBlocking(false);
                    channel.write(buffer);
                    System.out.println("Sent: " + response.getId() + " - Type: " + response.getType());
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
