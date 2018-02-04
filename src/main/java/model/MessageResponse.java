package model;

import java.nio.ByteBuffer;

public class MessageResponse {
    private int id;
    private int size;
    private String content;

    public MessageResponse(){

    }
    public MessageResponse(int id, String content) {
        this.id = id;
        this.content = content;
        size = content.getBytes().length;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String message) {
        this.content = message;
    }

    public byte[] serialize(){
        ByteBuffer buffer = ByteBuffer.allocate(8+size);
        buffer.putInt(id);
        buffer.putInt(size);
        buffer.put(content.getBytes());
        return buffer.array();
    }

    public static Message deserialize(byte[] array){
        Message message = new Message();
        ByteBuffer buffer = ByteBuffer.wrap(array);
        int id = buffer.getInt();
        int size = buffer.getInt();
        byte[] contentAsBytes = new byte[size];
        buffer.get(contentAsBytes);
        String content = new String(contentAsBytes);
        message.setId(id);
        message.setSize(size);
        message.setContent(content);
        return message;
    }
}
