package de.comparus.opensource.longmap;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;


public class LongMapImplTest {

    @Test
    void putAndGet_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        assertThat(map.size()).isEqualTo(0);
        map.put(3L, "Adam");
        map.put(6L, "Hanna");
        map.put(21L, null);
        map.put(22L, null);
        assertThat(map.size()).isEqualTo(4);
        assertThat(map.get(3L)).isEqualTo("Adam");
        assertThat(map.get(6L)).isEqualTo("Hanna");
        assertThat(map.get(1L)).isNull();
        assertThat(map.get(21L)).isNull();
        assertThat(map.get(22L)).isNull();
    }

    @Test
    void putAndGet_withResize_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        assertThat(map.size()).isEqualTo(0);
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        assertThat(map.size()).isEqualTo(20);
        LongStream.rangeClosed(1, 20).forEach(i -> assertThat(map.get(i)).isEqualTo("a" + i));
    }

    @Test
    void putAndGet_withUpdate_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        assertThat(map.size()).isEqualTo(0);
        map.put(3L, "Adam");
        map.put(6L, "Hanna");
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get(3L)).isEqualTo("Adam");
        assertThat(map.get(6L)).isEqualTo("Hanna");
        assertThat(map.get(1L)).isNull();
        map.put(6L, "Helen");
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get(3L)).isEqualTo("Adam");
        assertThat(map.get(6L)).isEqualTo("Helen");
        assertThat(map.get(1L)).isNull();
        map.put(3L, null);
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get(3L)).isEqualTo(null);
        assertThat(map.get(6L)).isEqualTo("Helen");
        assertThat(map.get(1L)).isNull();
    }

    @Test
    void remove_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        assertThat(map.size()).isEqualTo(0);
        map.put(3L, "Adam");
        map.put(6L, "Hanna");
        map.put(7L, null);
        assertThat(map.size()).isEqualTo(3);
        assertThat(map.remove(3L)).isEqualTo("Adam");
        assertThat(map.remove(6L)).isEqualTo("Hanna");
        assertThat(map.get(1L)).isNull();
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.remove(7L)).isNull();
        assertThat(map.size()).isEqualTo(0);
        assertThat(map.get(3L)).isNull();
        assertThat(map.get(6L)).isNull();
        assertThat(map.remove(3L)).isNull();
        assertThat(map.remove(6L)).isNull();
        map.put(10L, "Barcelona");
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    void remove_withResize_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        assertThat(map.size()).isEqualTo(0);
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        assertThat(map.size()).isEqualTo(20);
        LongStream.rangeClosed(1, 13).forEach(i -> assertThat(map.remove(i)).isEqualTo("a" + i));
        assertThat(map.size()).isEqualTo(7);
        LongStream.rangeClosed(14, 20).forEach(i -> assertThat(map.get(i)).isEqualTo("a" + i));
        LongStream.rangeClosed(1, 13).forEach(i -> assertThat(map.remove(i)).isNull());
        assertThat(map.remove(100L)).isNull();
        assertThat(map.size()).isEqualTo(7);
    }

    @Test
    void isEmpty_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        assertThat(map.isEmpty()).isTrue();
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        assertThat(map.isEmpty()).isFalse();
        LongStream.rangeClosed(1, 13).forEach(i -> assertThat(map.remove(i)).isEqualTo("a" + i));
        assertThat(map.isEmpty()).isFalse();
        LongStream.rangeClosed(14, 20).forEach(i -> assertThat(map.remove(i)).isEqualTo("a" + i));
        assertThat(map.isEmpty()).isTrue();
        map.put(7L, null);
        assertThat(map.isEmpty()).isFalse();
    }

    @Test
    void containsKey_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        LongStream.rangeClosed(1, 20).forEach(i -> assertThat(map.containsKey(i)).isFalse());
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        LongStream.rangeClosed(1, 20).forEach(i -> assertThat(map.containsKey(i)).isTrue());
        LongStream.rangeClosed(1, 13).forEach(map::remove);
        LongStream.rangeClosed(1, 13).forEach(i -> assertThat(map.containsKey(i)).isFalse());
        LongStream.rangeClosed(14, 20).forEach(i -> assertThat(map.containsKey(i)).isTrue());
        LongStream.rangeClosed(14, 20).forEach(map::remove);
        LongStream.rangeClosed(14, 20).forEach(i -> assertThat(map.containsKey(i)).isFalse());
    }

    @Test
    void containsValue_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        LongStream.rangeClosed(1, 20).forEach(i -> assertThat(map.containsValue("a" + i)).isFalse());
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        map.put(21L, null);
        map.put(22L, null);
        LongStream.rangeClosed(1, 20).forEach(i -> assertThat(map.containsValue("a" + i)).isTrue());
        assertThat(map.containsValue(null)).isTrue();
        LongStream.rangeClosed(1, 13).forEach(map::remove);
        LongStream.rangeClosed(1, 13).forEach(i -> assertThat(map.containsValue("a" + i)).isFalse());
        LongStream.rangeClosed(14, 20).forEach(i -> assertThat(map.containsValue("a" + i)).isTrue());
        LongStream.rangeClosed(14, 20).forEach(map::remove);
        LongStream.rangeClosed(14, 20).forEach(i -> assertThat(map.containsValue("a" + i)).isFalse());
        map.remove(21L);
        map.remove(22L);
        assertThat(map.containsValue(null)).isFalse();
    }

    @Test
    void keys_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        map.put(21L, null);
        map.put(22L, null);
        assertThat(map.keys()).containsOnly(LongStream.rangeClosed(1, 22).toArray());
        LongStream.rangeClosed(1, 13).forEach(map::remove);
        assertThat(map.keys()).containsOnly(LongStream.rangeClosed(14, 22).toArray());
        LongStream.rangeClosed(14, 22).forEach(map::remove);
        assertThat(map.keys()).isEmpty();
    }

    @Test
    void values_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        map.put(21L, null);
        map.put(22L, null);
        String[] expectedOne = new String[22];
        IntStream.rangeClosed(1, 20).forEach(i -> expectedOne[i - 1] = "a" + i);
        expectedOne[20] = null;
        expectedOne[21] = null;
        String[] valuesOne = map.values();
        assertThat(valuesOne.length).isEqualTo(22);
        assertThat(valuesOne).containsOnly(expectedOne);
        LongStream.rangeClosed(1, 13).forEach(map::remove);
        String[] expectedTwo = Arrays.copyOfRange(expectedOne, 13, expectedOne.length);
        String[] valuesTwo = map.values();
        assertThat(valuesTwo.length).isEqualTo(9);
        assertThat(valuesTwo).containsOnly(expectedTwo);
        LongStream.rangeClosed(14, 22).forEach(map::remove);
        assertThat(map.values()).isEmpty();
    }

    @Test
    void size_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        assertThat(map.size()).isEqualTo(0);
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        map.put(21L, null);
        map.put(22L, null);
        assertThat(map.size()).isEqualTo(22);
        LongStream.rangeClosed(1, 13).forEach(map::remove);
        assertThat(map.size()).isEqualTo(9);
        LongStream.rangeClosed(14, 22).forEach(map::remove);
        assertThat(map.size()).isEqualTo(0);
    }

    @Test
    void clear_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        map.put(21L, null);
        map.put(22L, null);
        assertThat(map.size()).isEqualTo(22);
        assertThat(map.keys().length).isEqualTo(22);
        assertThat(map.values().length).isEqualTo(22);
        map.clear();
        assertThat(map.size()).isEqualTo(0);
        assertThat(map.keys()).isEmpty();
        assertThat(map.values()).isEmpty();
    }

    @Test
    void toString_happyPath() {
        LongMap<String> map = new LongMapImpl<>(String.class);
        LongStream.rangeClosed(1, 20).forEach(i -> map.put(i, "a" + i));
        String expected = "[1=a1,2=a2,3=a3,4=a4,5=a5,6=a6,7=a7,8=a8,9=a9,10=a10,11=a11,12=a12,13=a13,14=a14,15=a15,16=a16,17=a17,18=a18,19=a19,20=a20]";
        assertThat(map.toString()).isEqualTo(expected);
    }
}
