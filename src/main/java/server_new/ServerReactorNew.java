package server_new;

import model.Response;
import monitor.MonitorThread;
import server.Reactor;
import server_new.Handler;

import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by imdb on 06/02/2018.
 */
public class ServerReactorNew implements Runnable {
    private MonitorThread monitorThread;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    public static final int SERVER_SELECTOR_TIMEOUT = 1000;

    public ServerReactorNew(int port) throws Exception {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor(this));
        monitorThread = new MonitorThread();
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public static final long SELECTOR_TIMEOUT = 1000;

    @Override
    public void run() {
        new Thread(monitorThread).start();
        System.out.println("Server listening on port " + serverSocketChannel.socket().getLocalPort());
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
//                System.out.println("Size key set: "+selectionKeys.size());
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    dispatch(key);
                }
                selectionKeys.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void dispatch(SelectionKey key) {
//        System.out.println("-----------------");
//        System.out.println("Readable: " +key.isReadable());
//        System.out.println("Writable: " +key.isWritable());
//        System.out.println("Acceptable: " +key.isAcceptable());
//        System.out.println("Connectable: "+key.isConnectable());
        Runnable r = (Runnable) key.attachment();
        if (r != null) {
            r.run();
        }
    }

    class Acceptor implements Runnable {
        ServerReactorNew reactor;

        public Acceptor(ServerReactorNew reactor) {
            this.reactor = reactor;
        }

        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    new Handler(monitorThread, selector, socketChannel, reactor);
                }
                System.out.println("Connection accepted");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
