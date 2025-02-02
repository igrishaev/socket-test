package me.ivan;

public class Main {
    public static void main(String[] args) {
        try (final Server server = Server.start(1999)) {

            Client c1 = Client.connect(1999);
            c1.sendMessage("hello");
            System.out.println(c1.getMessage());

            Client c2 = Client.connect(1999);
            c2.sendMessage("test");
            System.out.println(c2.getMessage());

            c1.close();
            c2.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}