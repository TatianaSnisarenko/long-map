package de.comparus.opensource.longmap2;

import java.util.List;

public interface LongMap<V> {
    V put(long key, V value);

    V get(long key);

    V remove(long key);

    boolean isEmpty();

    boolean containsKey(long key);

    boolean containsValue(V value);

    long[] keys();

    // using List instead of array, to avoid using Class
    List<V> values();

    long size();

    void clear();
}
