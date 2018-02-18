@file:Suppress("UNUSED_PARAMETER")

package org.kotlinq.dsl

import org.kotlinq.api.Context


@GraphQlDslObject
class FragmentScopeBuilder internal constructor() {

  internal val fragments = mutableMapOf<String, () -> Context>()

  fun on(typeName: String, block: TypeBuilder.() -> Unit) = Unit

  infix fun String.def(block: TypeBuilder.() -> Unit) {
    fragments[this] = { GraphBuilder(this, definition = block).build() }
  }

}