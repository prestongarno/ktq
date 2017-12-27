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
package com.prestongarno.kotlinq.core

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.prestongarno.kotlinq.core.adapters.Adapter
import com.prestongarno.kotlinq.core.adapters.QField
import com.prestongarno.kotlinq.core.internal.extractedPayload
import com.prestongarno.kotlinq.core.internal.pretty
import com.prestongarno.kotlinq.core.schema.QType
import java.io.InputStream

/**
 * The base class for creating a GraphQL type model
 *
 * @param model the generated schema object
 * @param T the upper bounds for the type (should be the same as the ^ usually)
 * @author prestongarno
 */
open class QModel<out T : QType>(val model: T) {

  internal
  val fields = mutableMapOf<String, Adapter>()

  internal
  var resolved = false

  internal
  val graphqlType by lazy { "${model::class.simpleName}" }

  val isResolved: Boolean get() = resolved

  /**
   * Constructs a GraphQL query from this [QModel]. Fragments are named according to this rule:
   *
   *
   * ***`frag${[QModel.model].class.simpleName}${index}`***
   *
   * and are printed in alphabetical order.
   * The ${index} mentioned in the last sentence is primarily assigned by the depth of the fragment in the
   * graph & secondarily the order in which the properties/fragments are declared in a [QModel]
   *
   * ***Remember:*** Kotlinq does not distinguish between a root query/mutation
   * and therefore treats this instance as a root query.
   *
   * @param pretty If true, returns a formatted string query indented by two spaces, otherwise a compact query
   * @return The GraphQL query that this class represents as a [String]
   * @throws OutOfMemoryError if the definition is recursive
   */
  @JvmOverloads fun toGraphql(pretty: Boolean = false): String =
      if (pretty) pretty() else extractedPayload(this)

  private
  fun onResponse(input: InputStream): Boolean =
      (Parser().parse(input) as? JsonObject)?.let(this::accept) == true

  internal
  fun onResponse(input: String) = onResponse(input.byteInputStream())

  internal
  fun accept(input: JsonObject): Boolean {
    resolved = fields.filterNot {
      it.value.accept(input[it.value.qproperty.graphqlName])
    }.isEmpty()
    return resolved
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is QModel<*>) return false
    if (graphqlType != other.graphqlType) return false
    if (fields.size != other.fields.size) return false

    return fields.entries.find {
      other.fields[it.key] != it.value
    } == null
  }

  override fun hashCode(): Int = fields.entries.fold(initial = graphqlType.hashCode()) { acc, entry ->
    acc * 31 + entry.value.hashCode()
  }

  override fun toString() = "${this::class.simpleName}<${model::class.simpleName}>" +
      fields.entries.joinToString(",", "[", "]") { it.value.qproperty.toString() }

  internal
  fun getFields(): Sequence<Adapter> =
      fields.entries.asSequence().map(MutableMap.MutableEntry<String, Adapter>::value)

  /**
   * Add the field to the instance of this model
   * @param field the Adapter to bind
   * @return the field
   */
  internal
  fun <T> register(field: T): T where T : Adapter, T : QField<T> {
    fields[field.qproperty.graphqlName] = field
    return field
  }

}
