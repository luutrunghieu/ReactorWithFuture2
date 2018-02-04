package new_server;

import model.Message;
import model.MessageResponse;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WriteEventHandler implements Runnable{
    private SelectionKey selectionKey;
    private ServerReactor reactor;

    public WriteEventHandler(SelectionKey selectionKey, ServerReactor reactor) {
        this.selectionKey = selectionKey;
        this.reactor = reactor;
    }

    @Override
    public void run() {
        try{
            BlockingQueue<Message> queue = reactor.getQueue();
            while(!queue.isEmpty()){
                Message message = queue.poll(1, TimeUnit.SECONDS);
                MessageResponse response = new MessageResponse(message.getId(),message.getContent());
                ByteBuffer buffer = ByteBuffer.wrap(response.serialize());
                SocketChannel channel = (SocketChannel) selectionKey.channel();
                channel.configureBlocking(false);
                channel.write(buffer);
                System.out.println("Sent: "+response.getContent());
            }
            selectionKey.interestOps(SelectionKey.OP_READ);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
