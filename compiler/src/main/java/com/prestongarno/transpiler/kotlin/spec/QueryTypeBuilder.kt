package com.prestongarno.transpiler.kotlin.spec

import com.prestongarno.ktq.runtime.GraphType
import com.prestongarno.transpiler.qlang.spec.QSymbol
import com.prestongarno.transpiler.qlang.spec.QTypeDef
import com.squareup.kotlinpoet.*

object QueryTypeBuilder {
	fun buildRootQueryClass(qType: QTypeDef, packageName: String = "com.prestongarno.ktq"): KotlinFile {
		val file = KotlinFile.builder(packageName, "Query")
		val typeVariable = TypeVariableName.Companion.invoke("E").withBounds(GraphType::class)

		val onSuccess = LambdaTypeName.get(null, listOf(typeVariable), UNIT)
		val onError = LambdaTypeName.get(null, listOf(INT.topLevelClassName(), ClassName.invoke("java.lang", "String")), UNIT)

		val companionObject = TypeSpec.companionObjectBuilder()

		return file.addType(TypeSpec.classBuilder(qType.name)
				.addTypeVariable(typeVariable)
				.addFun(FunSpec.constructorBuilder()
						.addParameter(ParameterSpec.builder("onSuccess", onSuccess).build())
						.addParameter(ParameterSpec.builder("onError", onError).defaultValue(CodeBlock.builder()
								.add(" { code, message -> }")
								.build()).build())
						.build()).companionObject(companionObject.build())
				.build()).build()
	}
}

/**
 *

class Query<E>(val onSuccess: (E) -> Unit, val onError: (Int, String) -> Unit = { i, m -> }) {

companion object ArgBuilder {

val queue = LinkedList<Any>()

fun exec() = (queue[0] as Query<SearchResultItemConnection<*,*,*>>).onError(400, "Not Found!")

fun <T : SearchResultItemConnection<*,*,*>> search(ArgBuilder: Search.ArgBuilder<T>): Query.ArgBuilder {
println("Created object '$ArgBuilder'")
queue.add(ArgBuilder.search)
return this
}
}

}

 */