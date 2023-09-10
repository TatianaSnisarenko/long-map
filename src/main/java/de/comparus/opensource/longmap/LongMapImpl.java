package de.comparus.opensource.longmap;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.StringJoiner;

@SuppressWarnings("unchecked")
public class LongMapImpl<V> implements LongMap<V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float DEFAULT_MAX_LOAD_FACTOR = 0.75f;
    private static final float DEFAULT_MIN_LOAD_FACTOR = 0.25f;
    Class<V> clazz;
    private Entry<V>[] table;
    private int size;
    private int maxThreshold;
    private int minThreshold;
    private int capacity;

    public LongMapImpl(Class<V> clazz) {
        this.size = 0;
        this.clazz = clazz;
    }

    public V put(long key, V value) {
        resize();
        return put(key, value, capacity, table, true);
    }

    public V get(long key) {
        Entry<V> entry;
        return (entry = getEntry(key)) == null ? null : entry.value;
    }

    public V remove(long key) {
        Entry<V> entry = removeEntry(key);
        return entry != null ? entry.value : null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {
        return get(key) != null;
    }

    public boolean containsValue(V value) {
        if (table != null && size > 0) {
            for (Entry<V> entry : table) {
                while (entry != null) {
                    if (Objects.equals(entry.value, value)) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        }
        return false;
    }

    public long[] keys() {
        return keysToArray();
    }

    public V[] values() {
        return valuesToArray();
    }

    public long size() {
        return size;
    }

    public void clear() {
        capacity = 0;
        size = 0;
        maxThreshold = 0;
        minThreshold = 0;
        table = null;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        if (table == null || size == 0) {
            return joiner.toString();
        }
        for (Entry<V> current : table) {
            while (current != null) {
                joiner.add(current.toString());
                current = current.next;
            }
        }
        return joiner.toString();
    }

    public static class Entry<V> {
        private final Long key;
        private V value;
        private Entry<V> next;

        private Entry(long key, V value, Entry<V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public String toString() {
            return new StringJoiner("=").add(key.toString()).add(value.toString()).toString();
        }
    }

    private V put(long key, V value, int capacity, Entry<V>[] table, boolean changeSize) {
        int index = bucketIndex(key, capacity);
        Entry<V> exitingEntry;
        V oldValue = null;
        if ((exitingEntry = table[index]) == null) {
            table[index] = new Entry<>(key, value, null);
            if (changeSize) {
                size++;
            }
        } else {
            boolean updated = false;
            do {
                if (hash(exitingEntry.key) == hash(key) && exitingEntry.key == key) {
                    oldValue = exitingEntry.value;
                    exitingEntry.value = value;
                    updated = true;
                    break;
                }
                exitingEntry = exitingEntry.next;
            } while (exitingEntry != null);
            if (!updated) {
                Entry<V> oldHead = table[index];
                Entry<V> newEntry = new Entry<>(key, value, oldHead);
                table[index] = newEntry;
                if (changeSize) {
                    size++;
                }
            }
        }
        return oldValue;
    }

    private static int hash(long key) {
        int h;
        return (h = Long.hashCode(key)) ^ (h >>> 16);
    }

    private static int bucketIndex(long key, int capacity) {
        return hash(key) & (capacity - 1);
    }

    private Entry<V> removeEntry(long key) {
        if (table == null || size == 0) {
            return null;
        }
        Entry<V> current = table[bucketIndex(key, capacity)];
        Entry<V> prev = null;
        while (current != null) {
            if (current.key == key) {
                if (prev == null) {
                    table[bucketIndex(key, capacity)] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return current;
            }
            prev = current;
            current = current.next;
        }
        resize();
        return null;
    }

    private Entry<V> getEntry(long key) {
        if (table == null || size == 0) {
            return null;
        }
        Entry<V> current = table[bucketIndex(key, capacity)];
        while (current != null) {
            if (current.key == key) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    private long[] keysToArray() {
        long[] keys = new long[size];
        Entry<V>[] tableCopy;
        int keyIndex = 0;
        if (size > 0 && (tableCopy = table) != null) {
            for (Entry<V> entry : tableCopy) {
                for (; entry != null; entry = entry.next) {
                    keys[keyIndex++] = entry.key;
                }
            }
        }
        return keys;
    }

    private V[] valuesToArray() {
        V[] values = (V[]) Array.newInstance(clazz, size);
        int valueIndex = 0;
        if (size > 0 && table != null) {
            for (Entry<V> entry : table) {
                while (entry != null) {
                    values[valueIndex++] = entry.value;
                    entry = entry.next;
                }
            }
        }
        return values;
    }

    private void resize() {
        if (table == null) {
            initialize();
        }
        if (size <= minThreshold && capacity > DEFAULT_CAPACITY) {
            shrinkTable();
        }
        if (size == maxThreshold) {
            expandTable();
        }
    }

    private void initialize() {
        capacity = DEFAULT_CAPACITY;
        maxThreshold = (int) (capacity * DEFAULT_MAX_LOAD_FACTOR);
        minThreshold = (int) (capacity * DEFAULT_MIN_LOAD_FACTOR);
        table = (Entry<V>[]) new Entry[capacity];
    }

    private void expandTable() {
        int newCapacity = capacity;
        if (capacity >= MAXIMUM_CAPACITY) {
            maxThreshold = Integer.MAX_VALUE;
        } else if ((newCapacity = newCapacity << 1) <= MAXIMUM_CAPACITY && capacity >= DEFAULT_CAPACITY) {
            maxThreshold = maxThreshold << 1;
            minThreshold = minThreshold << 1;
            resizeTable(newCapacity);
        }
    }

    private void resizeTable(int newCapacity) {
        capacity = newCapacity;
        Entry<V>[] newTable = (Entry<V>[]) new Entry[newCapacity];
        for (Entry<V> entry : table) {
            Entry<V> current = entry;
            while (current != null) {
                put(current.key, current.value, newCapacity, newTable, false);
                current = current.next;
            }
        }
        table = newTable;
    }

    private void shrinkTable() {
        int newCapacity = capacity >> 1;
        if (newCapacity >= DEFAULT_CAPACITY) {
            maxThreshold = maxThreshold >> 1;
            minThreshold = minThreshold >> 1;
            resizeTable(newCapacity);
        }
    }
}
