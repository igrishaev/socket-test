package me.ivan.message.client;

import me.ivan.message.client.ClientMessage;

public class UnknownMessage implements ClientMessage {
    public static UnknownMessage INSTANCE = new UnknownMessage();
}
