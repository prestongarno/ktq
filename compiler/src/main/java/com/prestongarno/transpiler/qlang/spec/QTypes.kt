package com.prestongarno.transpiler.qlang.spec

import com.squareup.kotlinpoet.TypeSpec

/** The base class for all components of the compilation
 */
abstract class QSchemaType<E>(var name: String) {

  protected var kotlinSpec: E? = null // sue me

  // TODO associate each QSchemaType with transforming themselves into a kotlinpoet type
  abstract fun toKotlin() : E

  var description: String = ""

  override fun toString(): String {
    return "'$name' (${this::class.simpleName})"
  }
}

/**
 * Base class for all "types" defined by the schema
 */
abstract class QDefinedType(name: String) : QSchemaType<TypeSpec>(name)

abstract class QStatefulType(name: String, val fields: List<QField>) : QDefinedType(name)

class QUnknownType(name: String) : QDefinedType(name) {
  override fun toKotlin(): TypeSpec {
    throw IllegalStateException("This shouldnt happen")
  }
}

class QUnknownInterface(name: String) : QInterfaceDef(name, emptyList())

class QDirectiveType(name: String, val arg: String) : QDefinedType(name) {
  override fun toKotlin(): TypeSpec = throw IllegalStateException("This shouldnt happen")
}

class QDirectiveSymbol(val type: QDefinedType, val value: String) : QSchemaType<Any>(type.name) {
  override fun toKotlin(): TypeSpec = throw IllegalStateException("This shouldnt happen")
  companion object {
    val default = QDirectiveSymbol(QUnknownType(""), "")
  }
}
