package com.papel.imdb_clone.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple application-wide event bus for decoupled communication between components.
 * Usage:
 * AppEventBus.getInstance().publish("USER_LOGGED_IN", user);
 */
public class AppEventBus {
    private static final AppEventBus INSTANCE = new AppEventBus();

    private final Map<String, CopyOnWriteArrayList<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    private AppEventBus() {
    }

    public static AppEventBus getInstance() {
        return INSTANCE;
    }

    public void subscribe(String eventKey, Consumer<Object> listener) {
        listeners.computeIfAbsent(eventKey, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

}
