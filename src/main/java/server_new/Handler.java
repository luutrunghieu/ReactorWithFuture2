package server_new;

import constant.RPC;
import model.CalculationRequest;
import model.MessageRequest;
import model.Request;
import model.Response;
import monitor.MonitorThread;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;

/**
 * Created by imdb on 06/02/2018.
 */
public class Handler implements Runnable {
    static ExecutorService pool = Executors.newFixedThreadPool(10);
    ServerReactorNew reactor;
    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    static final int READING = 0, WRITING = 1;
    int state = READING;
    boolean reading = false;
    private BlockingQueue<Response> writePendingQueue;
    private MonitorThread monitorThread;
    Handler(MonitorThread monitorThread,Selector selector, SocketChannel socketChannel, ServerReactorNew reactor) throws Exception {
        this.monitorThread = monitorThread;
        this.reactor = reactor;
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
        selector.wakeup();
        writePendingQueue = new ArrayBlockingQueue<>(1024);
    }

    public BlockingQueue<Response> getWritePendingQueue() {
        return writePendingQueue;
    }

    public void setWritePendingQueue(BlockingQueue<Response> writePendingQueue) {
        this.writePendingQueue = writePendingQueue;
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
            reading = true;
            selectionKey.interestOps(0);
            while (true) {
                Request request = null;
                CompletableFuture completableFuture = new CompletableFuture();
                buffer.limit(12);
                int readCount = socketChannel.read(buffer);
                if (readCount == 0) {
//                    System.out.println("Read count = 0");
                    reading = false;
                    if (!writePendingQueue.isEmpty()) {
//                        System.out.println("read - Change to write");
                        state = WRITING;
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    } else {
//                        System.out.println("read - Change to read");
                        state = READING;
                        selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                    break;
                }

                buffer.flip();
                int type = buffer.getInt();
                int id = buffer.getInt();
                int size = buffer.getInt();
                buffer.limit(12 + size);
                socketChannel.read(buffer);
                buffer.position(12);
                switch (type) {
                    case RPC.ECHO: {
                        byte[] contentAsBytes = new byte[size];
                        buffer.get(contentAsBytes, 0, size);
                        String content = new String(contentAsBytes);
                        request = new MessageRequest(id, content);
                        monitorThread.getReceiveCount().incrementAndGet();
                        break;
                    }
                    case RPC.CALCULATE: {
                        long num1 = buffer.getLong();
                        long num2 = buffer.getLong();
                        request = new CalculationRequest(id, num1, num2);
                        monitorThread.getReceiveCount().incrementAndGet();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                buffer.clear();
//                System.out.println("Received message: " + request.getId());
                pool.execute(new ProcessMessage(this, request, completableFuture));
                completableFuture.thenAcceptAsync((mess) -> {
                    if (!reading) {
//                        System.out.println("completeFuture - Change to write");
                        state = WRITING;
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                        selectionKey.selector().wakeup();
                    }
                });
            }
        } catch (Exception ex) {
            System.out.println("State when throwing exception: " + state);
            System.out.println("Queue size: " + writePendingQueue.size());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    void write() {
        try {
//            System.out.println("WRITING");
            if (!writePendingQueue.isEmpty()) {
                while (!writePendingQueue.isEmpty()) {
                    Response response = writePendingQueue.take();
                    ByteBuffer buffer = ByteBuffer.wrap(response.serialize());
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    channel.configureBlocking(false);
                    channel.write(buffer);
                    monitorThread.getSendCount().incrementAndGet();
//                    System.out.println("Sent: " + response.getId() + " - Type: " + response.getType());
                }
            }
//            System.out.println("Change selector to OP_READ - threads = " + Thread.activeCount());
//            System.out.println("write - change to read");
            state = READING;
            selectionKey.interestOps(SelectionKey.OP_READ);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
