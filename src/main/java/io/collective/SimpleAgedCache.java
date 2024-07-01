package io.collective;

import java.time.Clock;

public class SimpleAgedCache {

    private static class ExpirableEntry {
        Object key;
        Object value;
        long expirationTime;

        ExpirableEntry(Object key, Object value, long expirationTime) {
            this.key = key;
            this.value = value;
            this.expirationTime = expirationTime;
        }
    }

    private ExpirableEntry[] entries;
    private int size;
    private final Clock clock;

    public SimpleAgedCache(Clock clock) {
        this.entries = new ExpirableEntry[10];
        this.size = 0;
        this.clock = clock;
    }

    public SimpleAgedCache() {
        this(Clock.systemDefaultZone());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        long expirationTime = clock.millis() + retentionInMillis;
        for (int i = 0; i < size; i++) {
            if (entries[i].key.equals(key)) {
                entries[i].value = value;
                entries[i].expirationTime = expirationTime;
                return;
            }
        }

        if (size == entries.length) {
            expandArray();
        }

        entries[size++] = new ExpirableEntry(key, value, expirationTime);
    }

    public boolean isEmpty() {
        cleanUpExpiredEntries();
        return size == 0;
    }

    public int size() {
        cleanUpExpiredEntries();
        return size;
    }

    public Object get(Object key) {
        cleanUpExpiredEntries();
        for (int i = 0; i < size; i++) {
            if (entries[i].key.equals(key)) {
                return entries[i].value;
            }
        }
        return null;
    }

    private void cleanUpExpiredEntries() {
        long currentTime = clock.millis();
        int newSize = 0;

        for (int i = 0; i < size; i++) {
            if (entries[i].expirationTime > currentTime) {
                entries[newSize++] = entries[i];
            }
        }

        for (int i = newSize; i < size; i++) {
            entries[i] = null;
        }
        size = newSize;
    }

    private void expandArray() {
        ExpirableEntry[] newArray = new ExpirableEntry[entries.length * 2];
        System.arraycopy(entries, 0, newArray, 0, entries.length);
        entries = newArray;
    }
}