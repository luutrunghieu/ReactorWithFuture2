package server_new;

import model.Request;
import model.Response;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class ServerReactor implements Runnable {
    private ExecutorService pool;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private BlockingQueue<Response> writePendingQueue;

    public ServerReactor(int port) throws Exception {
        pool = Executors.newFixedThreadPool(5);
        selector = Selector.open();
        writePendingQueue = new ArrayBlockingQueue<>(1024);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public BlockingQueue<Response> getWritePendingQueue() {
        return writePendingQueue;
    }

    public void setWritePendingQueue(BlockingQueue<Response> writePendingQueue) {
        this.writePendingQueue = writePendingQueue;
    }

    public static final long SELECTOR_TIMEOUT = 1000;

    public ExecutorService getPool() {
        return pool;
    }

    public void setPool(ExecutorService pool) {
        this.pool = pool;
    }

    @Override
    public void run() {
        System.out.println("Server listening on port " + serverSocketChannel.socket().getLocalPort());
        try {
            while (true) {
//                System.out.println("outer while");
                selector.select(SELECTOR_TIMEOUT);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        System.out.println("Connection accepted");
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }

                    if (key.isReadable()) {
//                        System.out.println("Key is readable!!!!");
                        pool.submit(new ReadEventHandler(key,this));
                    }
                    if (key.isWritable()) {
//                        System.out.println("Key is writable!!!!");
                        pool.submit(new WriteEventHandler(key,this));
                    }
                }
                selectionKeys.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
