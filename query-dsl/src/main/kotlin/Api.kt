import org.kotlinq.api.Context
import org.kotlinq.dsl.GraphBuilder
import org.kotlinq.dsl.TypeBuilder

typealias TypeDefinition = () -> Context

/**
 * Top-level query.
 *
 * @param name the name of the operation. This does nothing
 * @param definition the query definition
 *
 * @author prestongarno
 * @since 0.4.0
 */
fun query(name: String = "Query", definition: TypeBuilder.() -> Unit): Context =
    GraphBuilder(name, definition).build()


/**
 * Creates a named type definition
 *
 * TODO naming types shouldn't really be allowed because they aren't checked ATM,
 * probably should optionally allow for this later
 *
 * @author prestongarno
 * @since 0.4.0
 */
fun typeDefinition(name: String, block: TypeBuilder.() -> Unit): TypeDefinition = {
  query(name = name, definition = block)
}