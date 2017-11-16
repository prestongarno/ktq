package com.prestongarno.ktq.enums

import com.prestongarno.ktq.QEnumType
import com.prestongarno.ktq.QModel
import com.prestongarno.ktq.QSchemaType.*
import com.prestongarno.ktq.QType
import com.prestongarno.ktq.primitives.eq
import org.intellij.lang.annotations.Language
import org.junit.Test

object Data : QType {
  val numberEnums by QEnumLists.stub<Kind>()
}

enum class Kind(val num: Int) : QEnumType {
  ZERO(0),
  ONE(1),
  TWO(2),
  THREE(3),
  FOUR(4),
  FIVE(5),
  SIX(6),
  SEVEN(7),
  EIGHT(8),
  NINE(9);

  companion object {
    fun fromNum(value: Int): Kind? = Kind.values().find { it.num == value }
  }
}

class BasicEnumLists {

  @Test fun `enum list basic delegate type is possible`() {

    val query = object : QModel<Data>(Data) {
      val numbers by model.numberEnums
    }
    query::numbers.returnType
        .arguments.firstOrNull()?.type?.classifier eq Kind::class
    query.toGraphql(false) eq "{numberEnums}"
  }

  @Test fun `enum list from response is valid`() {

    val query = object : QModel<Data>(Data) {
      val numbers by model.numberEnums
    }
    query.toGraphql(false) eq "{numberEnums}"

    @Language("JSON") val response = """
      {
        "numberEnums": [
          "ZERO",
          "ONE",
          "TWO",
          "THREE",
          "FOUR",
          "FIVE",
          "SIX",
          "SEVEN",
          "EIGHT"
        ]
      }
      """

    require(query.onResponse(response))
    query.numbers.size eq 9
    query.numbers.forEachIndexed { index, kind ->
      kind eq Kind.fromNum(index)
    }
  }

}