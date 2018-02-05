package server_new;

public class RunServer {
    public static void main(String[] args) {
        try {
            ServerReactor reactor = new ServerReactor(9999);
            Thread reactorThread = new Thread(reactor);
            reactorThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
