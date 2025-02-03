package me.ivan;

public record AddKeyMessage (String key, String val) implements ClientMessage {
}
