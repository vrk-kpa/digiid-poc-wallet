package fi.dvv.digiid.poc.domain

interface EncryptedStorageManager {
    operator fun get(key: String): String?
    operator fun set(key: String, value: String?)
}