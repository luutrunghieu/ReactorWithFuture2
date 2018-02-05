package model;

import java.nio.ByteBuffer;

/**
 * Created by imdb on 05/02/2018.
 */
public class CalculationResponse extends Response{
    private long result;

    public CalculationResponse(){

    }

    public CalculationResponse(int id, long result) {
        super(id);
        this.result = result;
        this.setType(2);
        this.setSize(8);
    }

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }

    @Override
    public byte[] serialize(){
        ByteBuffer buffer = ByteBuffer.allocate(12+this.getSize());
        buffer.putInt(this.getType());
        buffer.putInt(this.getId());
        buffer.putInt(this.getSize());
        buffer.putLong(result);
        return buffer.array();
    }

    public static CalculationResponse deserialize(byte[] array){
        ByteBuffer buffer = ByteBuffer.wrap(array);
        int type = buffer.getInt();
        int id = buffer.getInt();
        int size = buffer.getInt();
        long result = buffer.getLong();
        CalculationResponse calculationResponse = new CalculationResponse(id,result);
        return calculationResponse;
    }
}
