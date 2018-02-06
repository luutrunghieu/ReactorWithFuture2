package client;

import model.Request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class ClientReactor<T> implements Runnable {
    public static final int SELECTOR_TIMEOUT = 1000;
    private BlockingQueue<Request> queue;
    private Map<Integer, CompletableFuture<T>> responseFutureMapper;
    private ExecutorService pool;
    private String host;
    private int port;

    public ClientReactor(String hostIP, int port) {
        queue = new ArrayBlockingQueue(1024);
        responseFutureMapper = new HashMap<>();
        this.host = hostIP;
        this.port = port;
        pool = Executors.newFixedThreadPool(5);
    }

    public BlockingQueue<Request> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Request> queue) {
        this.queue = queue;
    }

    public Map<Integer, CompletableFuture<T>> getResponseFutureMapper() {
        return responseFutureMapper;
    }

    public void setResponseFutureMapper(Map<Integer, CompletableFuture<T>> responseFutureMapper) {
        this.responseFutureMapper = responseFutureMapper;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void interestNextOps(Selector selector,SocketChannel channel) throws ClosedChannelException {
        if(!queue.isEmpty()){
            channel.register(selector,SelectionKey.OP_WRITE);
        } else if(!responseFutureMapper.isEmpty()){
            channel.register(selector,SelectionKey.OP_READ);
        } else{
            channel.register(selector,0);
        }
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(9999));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_WRITE);

            while (true) {
                selector.select(SELECTOR_TIMEOUT);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        pool.submit(new ReadEventHandler(this, selectionKey));
                    }
                    if (selectionKey.isWritable()) {
                        pool.submit(new WriteEventHandler(this, selectionKey));
                    }
                }
//                interestNextOps(selector,socketChannel);
                selectionKeys.clear();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
