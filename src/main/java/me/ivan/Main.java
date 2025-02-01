package me.ivan;

public class Main {
    public static void main(String[] args) {
        Server.start2(1999);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Client c1 = Client.connect(1999);
        c1.sendMessage("hello");
        System.out.println(c1.getMessage());

        c1.close();

        Client c2 = Client.connect(1999);
        c2.sendMessage("test");
        System.out.println(c2.getMessage());

    }
}