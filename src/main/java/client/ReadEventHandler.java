package client;

import model.MessageResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;

public class ReadEventHandler implements Runnable{
    private ClientReactor reactor;
    private SelectionKey selectionKey;

    public ReadEventHandler(ClientReactor reactor, SelectionKey selectionKey) {
        this.reactor = reactor;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try {
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
                MessageResponse response = new MessageResponse(id,content);
                System.out.println("Received: "+response.getContent());
                CompletableFuture<MessageResponse> echoResponseFuture = reactor.getMessageFutureMapper().get(id);
                echoResponseFuture.complete(response);
            }
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
