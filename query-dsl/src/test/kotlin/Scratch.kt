@file:Suppress("unused")

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.kotlinq.dsl.fragment
import org.kotlinq.dsl.query
import org.kotlinq.dsl.toGraphQl


fun greet(worldName: String = "Earth", message: Any = "Hello") =
    query {
      !"greet"("name" to worldName, "message" to message) on def("Response") {
        "population"(integer)
        "countries"("first" to 100) on def("Country") {
          "name"(string)
          !"coordinates" on coordinates()
          !"subEntities"..{
            on("State") {
              "mayor"() on def("Person") {
                "name"(string)
              }
            }
            on("City") {
              "name"(string)
            }
          }
        }
      }
    }

fun coordinates() = fragment("Coordinate") {
  "xValue"(float)
  "yValue"(float)
}

enum class Measurement {
  MILES,
  KILOMETERS
}

class Scratch {

  @Test fun `simple primitive field dsl coordinate type prints correctly`() {
    assertThat(coordinates().toGraphQl(pretty = true, inlineFragments = false))
        .isEqualTo("""
          {
            xValue
            yValue
          }
        """.trimIndent())
  }

  @Test fun queryGraph() {
    println(greet().toGraphQl(pretty = true, inlineFragments = false))
    println(coordinates().toGraphQl(pretty = true, inlineFragments = false))
  }

  @Test fun simpleStarWars() {

    val expect = """
      |{
      |  search(text: "r2d2") {
      |    __typename
      |    ... on Human {
      |      name
      |      id
      |      height(unit: "METER")
      |      friendsConnection(first: 10) {
      |        totalCount
      |        friends {
      |          __typename
      |          ... on Human {
      |            name
      |            id
      |          }
      |        }
      |      }
      |    }
      |  }
      |}
      """.trimMargin("|")

    val starWarsQuery = query {
      "search"("text" to "r2d2")..{
        on("Human") {
          "name"(string)
          "id"(string)
          "height"(!float, "unit" to "METER")
          "friendsConnection"("first" to 10) on def("FriendConnection") {
            "totalCount"(integer)
            "friends"..{
              on("Human") {
                "name"(!string)
                "id"(string)
              }
            }
          }
        }
      }
    }.toGraphQl(pretty = true,
        inlineFragments = false)

    println(starWarsQuery)
    assertThat(starWarsQuery)
        .isEqualTo(expect)
  }

  @Test fun listStarWarsScratch() {

    val humanDef = fragment("Human") {
      "name"(string)
      "nicknames" listOf string
    }

    val robotDef = fragment("Robot") {
      "modelNumber"(string)
      "maker" on humanDef
    }

    val query = query {
      "characters"("first" to 100)..listOf {
        on..humanDef
        on..robotDef
      }
    }

    val expect = """
      |{
      |  characters(first: 100) {
      |    __typename
      |    ... on Human {
      |      name
      |      nicknames
      |    }
      |    ... on Robot {
      |      modelNumber
      |      maker {
      |        name
      |        nicknames
      |      }
      |    }
      |  }
      |}
      """.trimMargin("|")

    assertThat(query.toGraphQl())
        .isEqualTo(expect)
    println(query.toGraphQl())
  }
}

