package me.ivan;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {

    private static final int port = 21998;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        try (final AsyncServer ignored = AsyncServer.create("127.0.0.1", port, 16)) {
            Client c1 = Client.connect(port);
            Client c2 = Client.connect(port);

            c1.sendMessage("STOP");
//        System.out.println(c1.getMessage());

            c2.sendMessage("hoho");
            System.out.println(c2.getMessage());
//
            c1.sendMessage("bar");
            System.out.println(c1.getMessage());
            System.out.println(c1.getMessage());
        }








    }
}