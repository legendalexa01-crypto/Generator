package com.example.cryptogenerator

/**
 * Перечисление вместо строк — компилятор ловит опечатки, а IDE подсказывает.
 * jcaName — имя, которое понимает MessageDigest.
 */
enum class HashAlgorithm(val displayName: String, val jcaName: String) {
    MD5("MD5", "MD5"),
    SHA1("SHA-1", "SHA-1"),
    SHA256("SHA-256", "SHA-256"),
    SHA512("SHA-512", "SHA-512")
}