package nl.dannyjelsma.soundboardbot

class APIKeys {
    lateinit var keys: MutableList<String>

    fun addKey(key: String) {
        keys.add(key)
    }

    fun containsKey(key: String): Boolean {
        return keys.contains(key)
    }
}