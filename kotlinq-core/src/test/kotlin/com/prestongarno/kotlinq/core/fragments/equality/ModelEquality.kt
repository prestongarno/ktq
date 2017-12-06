package com.prestongarno.kotlinq.core.fragments.equality

import com.google.common.truth.Truth.assertThat
import com.prestongarno.kotlinq.core.ArgBuilder
import com.prestongarno.kotlinq.core.QEnumType
import com.prestongarno.kotlinq.core.QInterface
import com.prestongarno.kotlinq.core.QModel
import com.prestongarno.kotlinq.core.QSchemaType.QEnum
import com.prestongarno.kotlinq.core.QSchemaType.QInterfaces
import com.prestongarno.kotlinq.core.QSchemaType.QScalar
import com.prestongarno.kotlinq.core.QSchemaType.QTypes
import com.prestongarno.kotlinq.core.QSchemaType.QUnion
import com.prestongarno.kotlinq.core.QType
import com.prestongarno.kotlinq.core.QUnionType
import com.prestongarno.kotlinq.core.adapters.Adapter
import com.prestongarno.kotlinq.core.primitives.eq
import com.prestongarno.kotlinq.core.stubs.FloatDelegate
import com.prestongarno.kotlinq.core.stubs.InterfaceListStub
import com.prestongarno.kotlinq.core.stubs.StringDelegate
import com.prestongarno.kotlinq.core.stubs.StringStub
import com.prestongarno.kotlinq.core.stubs.UnionStub
import com.prestongarno.kotlinq.core.type.BasicTypeList.PersonModel
import com.prestongarno.kotlinq.core.type.Person
import org.junit.Test
import kotlin.reflect.jvm.isAccessible

/*********************************************************
 *  Manually-created schema definition
 *********************************************************/

object Entity : QUnionType by QUnionType.new() {

  fun onPerson(init: () -> QModel<Person>) = on(init)

  fun onOrganization(init: () -> QModel<Organization>) = on(init)

}

interface Vehicle : QInterface, QType {

  val model: StringDelegate.Query

  val owner: UnionStub.Query<Entity>

  val maxSpeed: FloatDelegate.Query

}

object Airplane : Vehicle {

  override val model by QScalar.String.stub()

  override val maxSpeed by QScalar.Float.stub()

  val engineHours by QScalar.Float.stub()

  val maxPassengers by QScalar.Int.stub()

  override val owner by QUnion.stub(Entity)
}

object Car : Vehicle {

  override val model by QScalar.String.stub()

  override val maxSpeed by QScalar.Float.stub()

  val mileage by QScalar.Float.stub()

  val maxPassengers by QScalar.Int.stub()

  override val owner by QUnion.stub(Entity)

}

object Organization : QType {

  val name by QScalar.String.stub()

  val type by QEnum.stub<OrganizationType>()

  val members by QTypes.stub<Person>()
}

enum class OrganizationType : QEnumType {
  CORPORATION,
  NON_PROFIT
}

object EqualityQuery : QType {
  val search by QInterfaces.List.configStub<Vehicle, EqualityQuery.SearchArgs>()

  class SearchArgs(keyword: String) : ArgBuilder() {

    val keyword by arguments.notNull("keyword", keyword)

  }
}

/*********************************************************
 *  Models from schema definition
 *********************************************************/

class EqualityImpl(
    keyword: String,
    fragment: InterfaceListStub<Vehicle, EqualityQuery.SearchArgs>.() -> Unit
) : QModel<EqualityQuery>(EqualityQuery) {

  val searchResult by model.search(EqualityQuery.SearchArgs(keyword), fragment)
}


open class AirplaneFrag0 : QModel<Airplane>(Airplane) {
  val modelName by model.model

  val maxSpeed by model.maxSpeed
}

open class AirplaneFrag1 : QModel<Airplane>(Airplane) {
  val modelName by model.model
}

open class AirplaneFrag2(spread: Entity.() -> Unit) : AirplaneFrag1() {
  val owner by model.owner { fragment(spread) }
}

/*********************************************************
 *  Class with test cases
 *********************************************************/

class ModelEquality {

  @Test fun simpleModelInEqualityCheck() {

    assertThat(AirplaneFrag0())
        .isNotEqualTo(AirplaneFrag1())
  }

  @Test fun instanceDelegateCheck() {


    val frag0 = AirplaneFrag0()

    val f = AirplaneFrag0().getDelegate<StringStub>("model")

    val g = AirplaneFrag0().getDelegate<StringStub>("model")

    f eq g

  }

  @Test fun simpleModelEqualityCheck() {

    AirplaneFrag0() eq AirplaneFrag0()

    AirplaneFrag1() eq AirplaneFrag1()

    val ctor = { AirplaneFrag2({ onPerson(::PersonModel) }) }

    ctor() eq ctor()
  }
}

private fun <Z : Adapter> QModel<*>.getDelegate(named: String): Z = fields[named] as? Z
    ?: throw IllegalArgumentException("Field $named is not of that type!")
private fun Any?.println() = println(this)
