package me.ivan;

public class Main {

    private static final int port = 1999;

    public static void main(String[] args) {
        try (final Server server = Server.create(port)) {

            Client c1 = Client.connect(port);
            c1.sendMessage("hello");
            System.out.println(c1.getMessage());

            Client c2 = Client.connect(port);
            c2.sendMessage("test");
            System.out.println(c2.getMessage());

            Client c3 = Client.connect(port);
            c3.sendMessage("test");
            // System.out.println(c3.getMessage());

            c1.close();
            c2.close();
            c3.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}