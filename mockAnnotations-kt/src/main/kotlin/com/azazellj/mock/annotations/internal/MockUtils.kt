package com.azazellj.mock.annotations.internal

import com.azazellj.mock.annotations.MockTypeHelper
import java.util.*

object MockUtils {
    @MockTypeHelper(types = arrayOf(Boolean::class))
    fun randomBoolean(): Boolean {
        return Math.random() < 0.5
    }

    @MockTypeHelper(types = [(Byte::class)])
    fun randomByte(): Byte {
        return (Math.random() * minOrMax(java.lang.Byte.MAX_VALUE.toLong(), java.lang.Byte.MIN_VALUE.toLong()).toByte()).toByte()
    }

    @MockTypeHelper(types = [(Char::class)])
    fun randomChar(): Char {
        return randomString()[(Math.random() * 16).toInt()]
    }

    @MockTypeHelper(types = [(Double::class)])
    fun randomDouble(): Double {
        return Math.random() * minOrMax(java.lang.Double.MAX_VALUE.toLong(), java.lang.Double.MIN_VALUE.toLong()).toDouble()
    }

    @MockTypeHelper(types = [(Float::class)])
    fun randomFloat(): Float {
        return (Math.random() * minOrMax(java.lang.Float.MAX_VALUE.toLong(), java.lang.Float.MIN_VALUE.toLong()).toFloat()).toFloat()
    }

    @MockTypeHelper(types = [(Int::class)])
    fun randomInt(): Int {
        return (Math.random() * minOrMax(Integer.MAX_VALUE.toLong(), Integer.MIN_VALUE.toLong()).toInt()).toInt()
    }

    @MockTypeHelper(types = [(Long::class)])
    fun randomLong(): Long {
        return (Math.random() * minOrMax(java.lang.Long.MAX_VALUE, java.lang.Long.MIN_VALUE).toLong()).toLong()
    }

    @MockTypeHelper(types = [(Short::class)])
    fun randomShort(): Short {
        return (Math.random() * minOrMax(java.lang.Short.MAX_VALUE.toLong(), java.lang.Short.MIN_VALUE.toLong()).toShort()).toShort()
    }

    private fun minOrMax(max: Long, min: Long): Number {
        return if (randomBoolean()) max else min
    }

    @MockTypeHelper(types = [(String::class)])
    fun randomString(): String {
        return UUID.randomUUID().toString()
    }

    @MockTypeHelper(types = [MutableCollection::class], initNeeded = true)
    fun <T, C : MutableCollection<T>> randomCollection(collection: C, randomValue: T): C {
        collection.add(randomValue)
        return collection
    }

    @MockTypeHelper(types = [MutableMap::class], initNeeded = true)
    fun <K, V, M : MutableMap<K, V>> randomMap(map: M, key: K, value: V): M {
        map.put(key, value)
        return map
    }
}