package new_server;

import model.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ReadEventHandler implements Runnable {
    private SelectionKey selectionKey;
    private ServerReactor reactor;

    public ReadEventHandler(SelectionKey selectionKey, ServerReactor reactor) {
        this.selectionKey = selectionKey;
        this.reactor = reactor;
    }

    @Override
    public void run() {
        try {
            BlockingQueue<Message> queue = reactor.getQueue();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            channel.configureBlocking(false);
            channel.read(buffer);
            buffer.flip();
            while(buffer.hasRemaining()){
                int id = buffer.getInt();
                int size = buffer.getInt();
                byte[] contentAsBytes = new byte[size];
                buffer.get(contentAsBytes,0,size);
                String content = new String(contentAsBytes);
                Message message = new Message(id,content);
                queue.offer(message,1, TimeUnit.SECONDS);
                System.out.println("Received: "+message.getContent());
//                System.out.println("Thread name: "+Thread.currentThread().getName());
            }
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
