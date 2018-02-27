package org.kotlinq.jvm

import org.kotlinq.api.Kind
import org.kotlinq.api.PropertyInfo
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf


@PublishedApi internal
fun KProperty1<*, Data?>.toPropertyInfo(
    typeName: String,
    args: Map<String, Any> = emptyMap()
) = PropertyInfo.propertyNamed("")
    .typeKind(Kind.typeNamed(typeName))
    .arguments(args)
    .build()


internal
fun KType.scalarKind(): Kind? {
  val name = (this.rootType.classifier as? KClass<*>)?.simpleName ?: return null
  return Kind.scalars.firstOrNull { it.classifier == name }?.let {
    wrap(it, this)
  }
}

private fun wrap(kind: Kind, type: KType): Kind {
  val transformations = mutableListOf<(Kind) -> Kind>()

  var current = type
  while (current.isIterable) {
    if (current.isMarkedNullable)
      transformations.add(Kind::asNullable)
    transformations.add(Kind::asList)
    current.arguments.firstOrNull()?.type?.let {
      current = it
    }
  }
  return transformations.fold(kind) { acc, curr -> curr(acc) }
}

internal fun KType.dataKind(): Kind? =
    if (!rootType.isSubtypeOf(Types.dataType))
      null
    else rootType.clazz?.simpleName
        ?.let(Kind.Companion::typeNamed)
        ?.let { wrap(it, this) }

private object Types {
  val dataType = Data::class.createType(nullable = true)
}


val KType.clazz: KClass<*>? get() = this.classifier as? KClass<*>

val KType.rootType: KType
  get() {
    return if (isIterable) {
      var current: KType = this
      while (current.isIterable)
        current.arguments.firstOrNull()?.type?.let { current = it }
      current
    } else {
      this
    }
  }

val KType.isIterable get() = this.clazz?.isIterable == true

val KClass<*>.isIterable get() = isSubclassOf(Iterable::class)
