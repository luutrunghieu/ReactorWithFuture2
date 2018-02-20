package client_new;

import constant.RPC;
import model.*;
import monitor.MonitorThread;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {
    private ClientReactorNew reactor;
    private SocketChannel channel;
    private SelectionKey selectionKey;
    static final int READING = 0, WRITING = 1;
    private int state = WRITING;
    private MonitorThread monitorThread;

    public ClientHandler(MonitorThread monitorThread, SocketChannel channel, ClientReactorNew reactor, SelectionKey selectionKey) {
        this.reactor = reactor;
        this.channel = channel;
        this.selectionKey = selectionKey;
        this.monitorThread = monitorThread;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        if (state == READING) {
            read();
        } else if (state == WRITING) {
            write();
        }
    }

    private void read() {
        try {
            selectionKey.interestOps(0);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            channel.configureBlocking(false);
            while (true) {
                buffer.limit(12);
                int readCount = socketChannel.read(buffer);
                if (readCount == 0) {
//                    System.out.println("Read count = 0");
                    if (!reactor.getQueue().isEmpty()) {
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
                        MessageResponse response = new MessageResponse(id, content);
                        CompletableFuture<MessageResponse> echoResponseFuture = (CompletableFuture<MessageResponse>) reactor.getResponseFutureMapper().get(id);
                        if (echoResponseFuture == null) {
                            break;
                        }
                        echoResponseFuture.complete(response);
                        monitorThread.getReceiveCount().incrementAndGet();
                        break;
                    }
                    case RPC.CALCULATE: {
                        long result = buffer.getLong();
                        CalculationResponse response = new CalculationResponse(id, result);
                        CompletableFuture<CalculationResponse> calcalateResponseFuture = (CompletableFuture<CalculationResponse>) reactor.getResponseFutureMapper().get(id);
                        calcalateResponseFuture.complete(response);
                        monitorThread.getReceiveCount().incrementAndGet();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                reactor.getResponseFutureMapper().remove(id);
                buffer.clear();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
//        try {
//            System.out.println("READING");
//            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            SocketChannel channel = (SocketChannel) selectionKey.channel();
//            channel.configureBlocking(false);
//            channel.read(buffer);
//            buffer.flip();
//            while (buffer.hasRemaining()) {
//                int type = buffer.getInt();
//                int id = buffer.getInt();
//                int size = buffer.getInt();
//                switch (type) {
//                    case 1: {
//                        byte[] contentAsBytes = new byte[size];
//                        buffer.get(contentAsBytes, 0, size);
//                        String content = new String(contentAsBytes);
//                        MessageResponse response = new MessageResponse(id, content);
//                        System.out.println("Received : " + response.getId()+ " - Type: "+response.getType());
//                        CompletableFuture<MessageResponse> echoResponseFuture = (CompletableFuture<MessageResponse>) reactor.getResponseFutureMapper().get(id);
//                        echoResponseFuture.complete(response);
//                        break;
//                    }
//                    case 2: {
//                        long result = buffer.getLong();
//                        CalculationResponse response = new CalculationResponse(id, result);
//                        System.out.println("Received : " + response.getId()+" - Type: "+response.getType());
//                        CompletableFuture<CalculationResponse> calcalateResponseFuture = (CompletableFuture<CalculationResponse>) reactor.getResponseFutureMapper().get(id);
//                        calcalateResponseFuture.complete(response);
//                        break;
//                    }
//                    default: {
//                        break;
//                    }
//                }
//                reactor.getResponseFutureMapper().remove(id);
//            }
//            System.out.println("Change to write");
//            state = WRITING;
//            selectionKey.interestOps(SelectionKey.OP_WRITE);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void write() {
        try {
//            System.out.println("WRITING");
            BlockingQueue<Request> queue = reactor.getQueue();
            while (!queue.isEmpty()) {
                Request request = queue.poll(1, TimeUnit.SECONDS);
                ByteBuffer buffer = ByteBuffer.wrap(request.serialize());
                channel.configureBlocking(false);
                channel.write(buffer);
//                System.out.println("Sent: " + request.getId());
                monitorThread.getSendCount().incrementAndGet();
            }
//            System.out.println("Change to read");
            state = READING;
            selectionKey.interestOps(SelectionKey.OP_READ);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
