package client;

import model.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WriteEventHandler implements Runnable {
    private ClientReactor reactor;
    private SelectionKey selectionKey;
    public WriteEventHandler(ClientReactor reactor,SelectionKey selectionKey) {
        this.reactor = reactor;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try{
            BlockingQueue<Message> queue = reactor.getQueue();
            while(!queue.isEmpty()){
//                System.out.println("Queue size: "+queue.size());
                Message message = queue.poll(1, TimeUnit.SECONDS);
                ByteBuffer buffer = ByteBuffer.wrap(message.serialize());
                SocketChannel channel = (SocketChannel) selectionKey.channel();
                channel.configureBlocking(false);
                channel.write(buffer);

                System.out.println("Sent: "+message.getContent());
//                System.out.println("Size: "+message.getSize());
//                Thread.sleep(1000);
            }
            selectionKey.interestOps(SelectionKey.OP_READ);
            selectionKey.selector().wakeup();
        } catch(Exception ex){
            ex.printStackTrace();
        }

    }
}
