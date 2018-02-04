package test;

import model.Message;

import java.nio.ByteBuffer;

public class TestModel {
    public static void main(String[] args) {
        Message message = new Message(1,"Hello");
        byte[] seriazlied = message.serialize();
        Message deserialized = Message.deserialize(seriazlied);
        System.out.println("Id: "+deserialized.getId());
        System.out.println("Size: "+deserialized.getSize());
        System.out.println("Content: "+deserialized.getContent());
        System.out.println("--------------");
//        byte[] sizeAsByte = new byte[4];
//        System.arraycopy(seriazlied,0,sizeAsByte,0,4);
        ByteBuffer buffer = ByteBuffer.wrap(seriazlied);
        int id = buffer.getInt();
        int size = buffer.getInt();
        byte[] contentAsBytes = new byte[size];
        buffer.get(contentAsBytes,0,size);
        System.out.println("Id: "+id);
        System.out.println("Size: "+size);
        System.out.println("Content: "+new String(contentAsBytes));
        if(buffer.hasRemaining()){
            System.out.println("Remain");
        }
//        int size = sizeBuffer.getInt();
//        System.out.println("Size: "+size);
//        ByteBuffer contentBuffer = ByteBuffer.allocate(size);
//        contentBuffer.put(seriazlied,4,size);
//        contentBuffer.flip();
//        String content = new String(contentBuffer.array());
//        System.out.println("Content: "+content);
    }
}
