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

import com.prestongarno.kotlinq.core.QModel
import com.prestongarno.kotlinq.core.api.ModelProvider
import com.prestongarno.kotlinq.core.properties.GraphQlProperty
import com.prestongarno.kotlinq.core.properties.NullableElementListDelegate
import kotlin.reflect.KProperty

/**
 * Reflective object which provides the information needed
 * to generate the correct representation of the GraphQL field query
 *
 * Base type of all delegates - this is the internal side of a property.
 * This class (internal API) and [com.prestongarno.kotlinq.core.adapters.GraphQlField] (public API delegate)
 * generally are both implemented on a property delegate */
interface Adapter {

  val qproperty: GraphQlProperty

  val args: Map<String, Any>

  fun accept(result: Any?): Boolean

  fun toRawPayload(): String
}

/**
 * Public API delegate representing an object which holds
 * the backing field information & value(s) for a GraphQL property
 * @param T : The type of object or value which this provides */
interface GraphQlField<out T> {
  operator fun getValue(inst: QModel<*>, property: KProperty<*>): T
}


/**
 * Internal interface to represent a union between [Adapter] and [GraphQlField]
 * @param T the return type of the field
 */
internal
interface GraphqlPropertyDelegate<out T : Any?> : GraphQlField<T>, Adapter {

  fun asNullable(): GraphqlPropertyDelegate<T?>

  fun asList(): GraphqlPropertyDelegate<List<T>>

  /**
   * Standard implementations of this function will take input and return [T]
   * if it is able to parse from JSON to the correct type, or null if the operation
   * failed.
   *
   * TODO -> It will never return the same reference twice. ([QType] needs to invoke ctor for this)
   *
   * This was added in 0.4.0 to support generically wrapping delegated properties as [List] types
   * @author prestongarno
   * @param obj Should be a type from [com.beust.klaxon] namespace representing JSON
   * @since 0.4.0
   */
  fun transform(obj: Any?): T?

  /**
   * Called on construction of a graphql object model.
   * Default behaviour: binds this property delegate to the instance of [graphqlModel]
   */
  fun bindToContext(graphqlModel: QModel<*>) = apply { graphqlModel.register(this) }

  companion object {

    internal
    fun <T : Any> wrapAsNullable(
        instance: GraphqlPropertyDelegate<T>,
        ref: () -> T?
    ): GraphqlPropertyDelegate<T?> = NullableDelegate(instance, ref)
  }
}

private
class NullableDelegate<out T: Any?>(
    private val instance: GraphqlPropertyDelegate<T>,
    private val ref: () -> T?
) : Adapter by instance, GraphqlPropertyDelegate<T?> {

  override fun asList(): GraphqlPropertyDelegate<List<T?>> =
      NullableElementListDelegate(this)

  override fun accept(result: Any?): Boolean {
    instance.accept(result)
    return when (instance) {
      is ModelProvider -> ref().let { it == null || instance.value.isResolved }
      else -> true
    }
  }

  override fun transform(obj: Any?): T? = instance.transform(obj)
  override fun asNullable(): GraphqlPropertyDelegate<T?> = this
  override fun getValue(inst: QModel<*>, property: KProperty<*>) = ref()
  override fun equals(other: Any?): Boolean = instance == other
  override fun hashCode(): Int = instance.hashCode() * 31
}
