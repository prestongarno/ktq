import org.junit.Test
import org.kotlinq.api.schema.Schema
import org.kotlinq.api.schema.TypeMapping
import org.kotlinq.api.schema.notNull
import org.kotlinq.dsl.query
import org.kotlinq.jvm.Data
import org.kotlinq.jvm.TypedFragment.Companion.typedFragment
import org.kotlinq.jvm.invoke

class Tests {

  @Test fun foo() {

    val query = query {
      "hello"(string)
    }

    class Query(map: Map<String, Any?>) {
      val hello by map.notNull<String>()
    }

    Schema.Builder()
        .define(TypeMapping("Query", ::Query))
        .build()
        .apply {
          require(canResolve(query))
          require(!canResolve(query(name = "Fubar") { "world"(string) }))
        }.resolve<Query>(query, mapOf("hello" to "world"))!!.hello!!
  }


  @Test fun bar() {
    typedFragment<Foo>().apply {
      println(this.typeName)
    }
  }
}

class Foo(map: Map<String, Any>) : Data by map() {
  val floatProp = 0.1f
  val fooProp by result
}
val Foo.bar get() = 10

