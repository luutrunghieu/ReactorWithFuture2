package model;

import java.nio.ByteBuffer;

/**
 * Created by imdb on 05/02/2018.
 */
public class CalculationRequest extends Request{
    private long num1;
    private long num2;

    public CalculationRequest(){

    }

    public CalculationRequest(int id, long num1, long num2) {
        super(id);
        this.num1 = num1;
        this.num2 = num2;
        this.setType(2);
        this.setSize(16);
    }

    public long getNum1() {
        return num1;
    }

    public void setNum1(long num1) {
        this.num1 = num1;
    }

    public long getNum2() {
        return num2;
    }

    public void setNum2(long num2) {
        this.num2 = num2;
    }

    @Override
    public byte[] serialize(){
        ByteBuffer buffer = ByteBuffer.allocate(12+this.getSize());
        buffer.putInt(this.getType());
        buffer.putInt(this.getId());
        buffer.putInt(this.getSize());
        buffer.putLong(num1);
        buffer.putLong(num2);
        return buffer.array();
    }

    public static CalculationRequest deserialize(byte[] array){
        ByteBuffer buffer = ByteBuffer.wrap(array);
        int type = buffer.getInt();
        int id = buffer.getInt();
        int size = buffer.getInt();
        long num1 = buffer.getLong();
        long num2 = buffer.getLong();
        CalculationRequest calculationRequest = new CalculationRequest(id,num1,num2);
        return calculationRequest;
    }
}
