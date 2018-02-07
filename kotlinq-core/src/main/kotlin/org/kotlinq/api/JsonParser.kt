package org.kotlinq.api


interface JsonParser {

  fun parseToObject(string: String): Sequence<Pair<String, String>>

  fun parseToArray(string: String): Sequence<String>

  companion object : JsonParser by Configuration.jsonParser
}
