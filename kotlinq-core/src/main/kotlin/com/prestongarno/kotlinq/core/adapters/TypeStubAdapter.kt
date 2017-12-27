/*
 * Copyright (C) 2017 Preston Garno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.prestongarno.kotlinq.core.adapters

import com.beust.klaxon.JsonObject
import com.prestongarno.kotlinq.core.ArgumentSpec
import com.prestongarno.kotlinq.core.QModel
import com.prestongarno.kotlinq.core.schema.QType
import com.prestongarno.kotlinq.core.api.ModelProvider
import com.prestongarno.kotlinq.core.internal.ValueDelegate
import com.prestongarno.kotlinq.core.internal.stringify
import com.prestongarno.kotlinq.core.properties.GraphQlProperty
import com.prestongarno.kotlinq.core.schema.stubs.TypeStub
import kotlin.reflect.KProperty

internal
class TypeStubAdapter<out T : QModel<U>, out U : QType, out A : ArgumentSpec>(
    private val qproperty: GraphQlProperty,
    private val init: () -> T,
    private val argBuilder: A?
) : TypeStub<T, U, A> {

  fun bindingTo(inst: QModel<*>): QField<T> =
      TypeStubImpl(qproperty, init, argBuilder.toMap()).bind(inst)

  override fun config(argumentScope: A.() -> Unit) {
    argBuilder?.apply(argumentScope)
  }
}

@ValueDelegate(QModel::class)
private data class TypeStubImpl<out I : QType, out P : QModel<I>>(
    override val qproperty: GraphQlProperty,
    val init: () -> P,
    override val args: Map<String, Any> = emptyMap()
) : QField<P>,
    Adapter,
    ModelProvider {

  override val value by lazy(init)

  override fun getValue(inst: QModel<*>, property: KProperty<*>): P = value

  override fun accept(result: Any?): Boolean {
    value.resolved = true
    return result is JsonObject
        && value.fields.filterNot { f ->
      f.value.accept(result[f.value.qproperty.graphqlName])
    }.isEmpty() && value.resolved
  }

  override fun toRawPayload(): String =
      qproperty.graphqlName + args.stringify() + value.toGraphql()

  override fun hashCode(): Int =
      (value.hashCode() * 31) +
          (args.hashCode() * 31) +
          (qproperty.hashCode() * 31)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as TypeStubImpl<*, *>

    if (qproperty != other.qproperty) return false
    if (args != other.args) return false

    return true
  }
}