package client;

import model.Message;
import model.MessageResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class ClientReactor implements Runnable {
    private BlockingQueue<Message> queue;
    private Map<Integer, CompletableFuture<MessageResponse>> messageFutureMapper;
    private ExecutorService pool;
    private String host;
    private int port;
    private Selector selector;

    public ClientReactor(String hostIP, int port) {
        queue = new ArrayBlockingQueue(1024);
        messageFutureMapper = new HashMap<>();
        this.host = hostIP;
        this.port = port;
        pool = Executors.newFixedThreadPool(5);
    }

    public BlockingQueue<Message> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    public Map<Integer, CompletableFuture<MessageResponse>> getMessageFutureMapper() {
        return messageFutureMapper;
    }

    public void setMessageFutureMapper(Map<Integer, CompletableFuture<MessageResponse>> messageFutureMapper) {
        this.messageFutureMapper = messageFutureMapper;
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

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(9999));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_WRITE);

            while (true) {
                selector.select();
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
                selectionKeys.clear();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
