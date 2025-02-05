package me.ivan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SelectorServer implements AutoCloseable {

    private final ServerSocketChannel channel;
    private final SocketAddress address;
    private final Selector selector;
    private final Executor executor;

    private SelectorServer(final ServerSocketChannel channel,
                           final SocketAddress address,
                           final Selector selector,
                           final Executor executor) {
        this.channel = channel;
        this.address = address;
        this.selector = selector;
        this.executor = executor;
    }

    @Override
    public String toString() {
        return String.format("<Server %s>", address);
    }

    private void handleAcceptable(final SelectableChannel selectableChannel) throws IOException {
        System.out.println("handleAcceptable");
        final SocketChannel clientChannel = ((ServerSocketChannel) selectableChannel).accept();
//        if (clientChannel == null)  {
//            return;
//        }
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private record Handler (SocketChannel channel) implements Runnable {

        public void runUnsafe() throws IOException {
            final ByteBuffer bb = ByteBuffer.allocate(4);
            while (bb.hasRemaining()) {
                channel.read(bb);
            }
            bb.rewind();
            while (bb.hasRemaining()) {
                channel.write(bb);
            }
        }

        @Override
        public void run() {
            try {
                runUnsafe();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleReadable(final SelectableChannel selectableChannel) {
        final SocketChannel socketChannel = (SocketChannel) selectableChannel;
        executor.execute(new Handler(socketChannel));
    }

    private void loopStep() throws IOException {
//        final int len = selector.select();
//        if (len == 0) {
//            return;
//        }

        selector.select((SelectionKey key) -> {
            if (key.isAcceptable()) {
                try {
                    handleAcceptable(key.channel());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (key.isReadable()) {
                handleReadable(key.channel());
            }
        });
//        final Set<SelectionKey> keys = selector.keys();
//        for (SelectionKey key: keys) {
//            if (key.isAcceptable()) {
//                handleAcceptable(key.channel());
//            }
//            if (key.isReadable()) {
//                handleReadable(key.channel());
//            }
//        }
    }

    private void loop() throws IOException {
        while (true) {
            loopStep();
        }
    }

    public static SelectorServer create(final String host, final int port) throws IOException {
        final SocketAddress address = new InetSocketAddress(host, port);
        final ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(address);
        channel.configureBlocking(false);
        final Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_ACCEPT);
        final Executor executor = Executors.newFixedThreadPool(8);
        return new SelectorServer(channel, address, selector, executor);
    }

    @Override
    public void close() throws Exception {
        channel.close();
    }

    public static void main(final String... args) throws IOException {
        final SelectorServer server = create("127.0.0.1", 1998);

        Thread t = new Thread() {
            public void run() {
                try {
                    server.loop();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        t.start();

        Client c1 = Client.connect(1998);
        Client c2 = Client.connect(1998);

        c1.sendMessage("STOP");
        System.out.println(c1.getMessage());

        c1.sendMessage("hehe");
        System.out.println(c1.getMessage());
//
        for (int i = 0; i < 99; i++) {
            c1.sendMessage("STOP");
            System.out.println(c1.getMessage());
        }

//
//        c1.sendMessage("STOP");
//        System.out.println(c1.getMessage());

        // server.close();

//        c2.sendMessage("hoho");
//        System.out.println(c2.getMessage());
//
//        c2.sendMessage("haha");
//        System.out.println(c2.getMessage());
    }
}
