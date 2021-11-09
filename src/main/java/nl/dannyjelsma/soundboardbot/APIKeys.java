package nl.dannyjelsma.soundboardbot;

import java.util.ArrayList;
import java.util.List;

public class APIKeys {

    private List<String> keys;

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public void addKey(String key) {
        if (keys == null) {
            keys = new ArrayList<>();
        }

        keys.add(key);
    }

    public boolean containsKey(String key) {
        return keys.contains(key);
    }
}
