@file:Suppress("UNUSED_PARAMETER")

package org.kotlinq.dsl

import org.kotlinq.api.Fragment
import org.kotlinq.api.Kind


@GraphQlDslObject
class FragmentContextBuilder internal constructor(
    var fieldTypeName: String = "") {



  private var isCollection = false

  private val fragments = mutableSetOf<Fragment>()

  fun on(typeName: String, block: SelectionSet) {
    fragments += GraphBuilder(block).build(typeName)
  }

  val on = FragmentPrefix()


  internal
  fun flagField(isCollection: Boolean = true) {
    this.isCollection = isCollection
  }

  private
  fun toFragmentInfo(): FragmentInfo = FragmentInfo(
      fieldTypeName,
      isCollection,
      fragments.toSet())

  /**
   * This exists because of syntax limitations
   */
  internal class FragmentInfo(
      typeName: String,
      isCollection: Boolean,
      val fragments: Set<Fragment>) {

    val typeKind = Kind.named(typeName).let {
      if (isCollection) it.asList() else it
    }
  }

  inner class FragmentPrefix internal constructor(){
    operator fun rangeTo(fragment: Fragment) {
      this@FragmentContextBuilder.fragments += fragment
    }
  }


  companion object {


    internal fun fromBlock(block: FragmentSelection) =
        FragmentContextBuilder().apply(block)
            .let { if (it.fragments.isEmpty()) null else it.toFragmentInfo() }
  }
}