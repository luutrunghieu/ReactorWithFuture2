package client;

import model.MessageRequest;
import model.Request;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WriteEventHandler implements Runnable {
    private ClientReactor reactor;
    private SelectionKey selectionKey;

    public WriteEventHandler(ClientReactor reactor, SelectionKey selectionKey) {
        this.reactor = reactor;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try {
            BlockingQueue<Request> queue = reactor.getQueue();
            while (!queue.isEmpty()) {
                Request request = queue.poll(1, TimeUnit.SECONDS);
                ByteBuffer buffer = ByteBuffer.wrap(request.serialize());
                SocketChannel channel = (SocketChannel) selectionKey.channel();
                channel.configureBlocking(false);
                channel.write(buffer);
//                System.out.println("Sent: " + request.getId());
            }
            selectionKey.interestOps(SelectionKey.OP_READ);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
