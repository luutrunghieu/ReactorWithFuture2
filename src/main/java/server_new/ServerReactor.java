package server_new;

import model.MessageRequest;
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
    BlockingQueue<Response> queue;

    public ServerReactor(int port) throws Exception {
        pool = Executors.newFixedThreadPool(5);
        selector = Selector.open();
        queue = new ArrayBlockingQueue<>(1024);
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

    public BlockingQueue<Response> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Response> queue) {
        this.queue = queue;
    }

    public static final long SELECTOR_TIMEOUT = 1000;

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
