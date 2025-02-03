package me.ivan;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

    private final Map<String, String> data;

    private Storage(final Map<String, String> data) {
        this.data = data;
    }

    public static Storage create() {
        return new Storage(new ConcurrentHashMap<>());
    }

    public void setKey(final String key, final String val) {
        data.put(key, val);
    }

    public void delKey(final String key) {
        data.remove(key);
    }

    public void clear() {
        data.clear();
    }

    public int count() {
        return data.size();
    }

}
