package com.example.cryptogenerator

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

/**
 * Чистая логика генерации. Никакого Android — поэтому класс легко
 * покрывается обычными JUnit-тестами на JVM.
 */
object Generator {

    /** Случайный UUID версии 4. */
    fun uuid(): String = UUID.randomUUID().toString()

    /** Хеш строки выбранным алгоритмом, hex-строка в нижнем регистре. */
    fun hash(input: String, algorithm: HashAlgorithm): String {
        val digest = MessageDigest.getInstance(algorithm.jcaName)
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * Криптостойкий пароль. Возвращает пустую строку, если длина вне [4, 64].
     * Валидацию для UI делаем отдельно, а здесь — честный контракт для тестов.
     */
    fun password(length: Int): String {
        if (length !in 4..64) return ""
        val pool = "abcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "0123456789" +
                "!@#$%^&*()-_=+"
        val rnd = SecureRandom()
        return (1..length).map { pool[rnd.nextInt(pool.length)] }.joinToString("")
    }
}