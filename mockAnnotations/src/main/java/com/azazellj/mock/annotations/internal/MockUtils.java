package com.azazellj.mock.annotations.internal;

import com.azazellj.mock.annotations.MockTypeHelper;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class MockUtils {

    @MockTypeHelper(types = {boolean.class, Boolean.class})
    public static boolean randomBoolean() {
        return Math.random() < 0.5;
    }

    @MockTypeHelper(types = {byte.class, Byte.class})
    public static byte randomByte() {
        return (byte) (Math.random() * minOrMax(Byte.MAX_VALUE, Byte.MIN_VALUE).byteValue());
    }

    @MockTypeHelper(types = {char.class, Character.class})
    public static char randomChar() {
        return randomString().charAt((int) (Math.random() * 16));
    }

    @MockTypeHelper(types = {double.class, Double.class})
    public static double randomDouble() {
        return (Math.random() * minOrMax((long) Double.MAX_VALUE, (long) Double.MIN_VALUE).doubleValue());
    }

    @MockTypeHelper(types = {float.class, Float.class})
    public static float randomFloat() {
        return (float) (Math.random() * minOrMax((long) Float.MAX_VALUE, (long) Float.MIN_VALUE).floatValue());
    }

    @MockTypeHelper(types = {int.class, Integer.class})
    public static int randomInt() {
        return (int) (Math.random() * minOrMax(Integer.MAX_VALUE, Integer.MIN_VALUE).intValue());
    }

    @MockTypeHelper(types = {long.class, Long.class})
    public static long randomLong() {
        return (long) (Math.random() * minOrMax(Long.MAX_VALUE, Long.MIN_VALUE).longValue());
    }

    @MockTypeHelper(types = {short.class, Short.class})
    public static short randomShort() {
        return (short) (Math.random() * minOrMax(Short.MAX_VALUE, Short.MIN_VALUE).shortValue());
    }

    private static Number minOrMax(long max, long min) {
        return randomBoolean() ? max : min;
    }

    @MockTypeHelper(types = String.class)
    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    @MockTypeHelper(types = Collection.class, initNeeded = true)
    public static <T, C extends Collection<T>> C randomCollection(C collection, T randomValue) {
        collection.add(randomValue);
        return collection;
    }

    @MockTypeHelper(types = Map.class, initNeeded = true)
    public static <K, V, M extends Map<K, V>> M randomMap(M map, K key, V value) {
        map.put(key, value);
        return map;
    }
}
