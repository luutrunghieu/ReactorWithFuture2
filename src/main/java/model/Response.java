package model;

/**
 * Created by imdb on 05/02/2018.
 */
public abstract class Response {
    private int id;
    private int type;
    private int size;

    public Response() {

    }

    public Response(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public abstract byte[] serialize();
}
