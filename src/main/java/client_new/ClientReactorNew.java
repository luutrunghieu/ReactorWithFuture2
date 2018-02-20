package client_new;

import model.Request;
import monitor.MonitorThread;

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

public class ClientReactorNew<T> implements Runnable {
    public static final int SELECTOR_TIMEOUT = 500;
    private BlockingQueue<Request> queue;
    private Map<Integer, CompletableFuture<T>> responseFutureMapper;
    private String host;
    private int port;
    private MonitorThread monitorThread;

    public ClientReactorNew(String hostIP, int port) throws Exception {
        queue = new ArrayBlockingQueue(1024);
        responseFutureMapper = new HashMap<>();
        this.host = hostIP;
        this.port = port;
        monitorThread = new MonitorThread();
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

    @Override
    public void run() {
        try {
            new Thread(monitorThread).start();
            Selector selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(9999));
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_WRITE);
            ClientHandler handler = new ClientHandler(monitorThread, socketChannel, this, selectionKey);
            selectionKey.attach(handler);
            while (true) {
                selector.select(SELECTOR_TIMEOUT);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    Runnable r = (Runnable) key.attachment();
                    if (r != null) {
                        r.run();
                    }
                }
                interestNextOps(handler);
                selectionKeys.clear();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void interestNextOps(ClientHandler handler){
        if(!queue.isEmpty()){
            handler.setState(1);
            handler.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else if(!responseFutureMapper.isEmpty()){
            handler.setState(0);
            handler.getSelectionKey().interestOps(SelectionKey.OP_READ);
        } else{
            handler.setState(2);
            handler.getSelectionKey().interestOps(0);
        }
    }
}
