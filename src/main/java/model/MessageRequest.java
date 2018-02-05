package model;

import java.nio.ByteBuffer;

public class MessageRequest extends Request{
    private String content;

    public MessageRequest(){

    }
    public MessageRequest(int id, String content) {
        super(id);
        this.content = content;
        this.setSize(content.getBytes().length);
        this.setType(1);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String message) {
        this.content = message;
    }

    @Override
    public byte[] serialize(){
        ByteBuffer buffer = ByteBuffer.allocate(12+this.getSize());
        buffer.putInt(this.getType());
        buffer.putInt(this.getId());
        buffer.putInt(this.getSize());
        buffer.put(content.getBytes());
        return buffer.array();
    }

    public static MessageRequest deserialize(byte[] array){
        MessageRequest messageRequest = new MessageRequest();
        ByteBuffer buffer = ByteBuffer.wrap(array);
        int type = buffer.getInt();
        int id = buffer.getInt();
        int size = buffer.getInt();
        byte[] contentAsBytes = new byte[size];
        buffer.get(contentAsBytes);
        String content = new String(contentAsBytes);
        messageRequest.setId(id);
        messageRequest.setSize(size);
        messageRequest.setContent(content);
        return messageRequest;
    }
}
