package server_new;

import model.Response;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

//public class WriteEventHandler implements Runnable{
//    private SelectionKey selectionKey;
//    private ServerReactor reactor;
//
//    public WriteEventHandler(SelectionKey selectionKey, ServerReactor reactor) {
//        this.selectionKey = selectionKey;
//        this.reactor = reactor;
//    }
//
//    @Override
//    public void run() {
//        try {
//            BlockingQueue<Response> queue = reactor.getWritePendingQueue();
//            if (!queue.isEmpty()) {
//                while (!queue.isEmpty()) {
//                    Response response = queue.take();
//                    ByteBuffer buffer = ByteBuffer.wrap(response.serialize());
//                    SocketChannel channel = (SocketChannel) selectionKey.channel();
//                    channel.configureBlocking(false);
//                    channel.write(buffer);
////                    System.out.println("Sent: " + response.getId()+" - Type: "+response.getType());
//                }
//            }
////            System.out.println("Change selector to OP_READ - threads = " + Thread.activeCount());
//            selectionKey.interestOps(SelectionKey.OP_READ);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//}
