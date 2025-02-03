package me.ivan;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {

    private static final int port = 1999;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        final AsyncServer server = new AsyncServer();
        // server.start().get();
        server.loop();

        Client c1 = Client.connect(21998);
        Client c2 = Client.connect(21998);

        c1.sendMessage("STOP");
//        System.out.println(c1.getMessage());


        c2.sendMessage("hoho");
        System.out.println(c2.getMessage());
//
        c1.sendMessage("bar");
        System.out.println(c1.getMessage());
        System.out.println(c1.getMessage());


//        System.in.read();



//        try (final Server server = Server.create(port)) {
//

//

//
//            Client c3 = Client.connect(port);
//            c3.sendMessage("test");
//            // System.out.println(c3.getMessage());
//
////            c1.close();
////            c2.close();
////            c3.close();
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }


    }
}