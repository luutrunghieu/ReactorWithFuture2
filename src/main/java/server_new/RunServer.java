package server_new;

public class RunServer {
    public static void main(String[] args) {
        try {
            ServerReactorNew reactor = new ServerReactorNew(9999);
            Thread reactorThread = new Thread(reactor);
            reactorThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
