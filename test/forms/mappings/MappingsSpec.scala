/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.mappings

import models.Enumerable
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.Form

import java.time.LocalDate

object MappingsSpec {

  sealed trait Foo
  case object Bar extends Foo
  case object Baz extends Foo

  object Foo {
    val values: Set[Foo] = Set(Bar, Baz)
    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(values.toSeq.map(v => v.toString -> v): _*)
  }
}

class MappingsSpec extends AnyWordSpec with Matchers with OptionValues with Mappings {

  import MappingsSpec._

  private def errorMessages(form: Form[_]): Set[String] =
    form.errors.map(_.message).toSet

  "text" must {

    val testForm: Form[String] = Form("value" -> text())

    "bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "filter out smart apostrophes on binding" in {
      val boundValue = testForm bind Map("value" -> "We’re ‘aving fish ‘n’ chips for tea")
      boundValue.get mustEqual "We're 'aving fish 'n' chips for tea"
    }

    "not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      errorMessages(result) must contain only "error.required"
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      errorMessages(result) must contain only "error.required"
    }

    "return a custom error message" in {
      val form = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      errorMessages(result) must contain only "custom.error"
    }

    "unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "boolean" must {

    val testForm: Form[Boolean] = Form("value" -> boolean())

    "bind true" in {
      testForm.bind(Map("value" -> "true")).get mustEqual true
    }

    "bind false" in {
      testForm.bind(Map("value" -> "false")).get mustEqual false
    }

    "not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      errorMessages(result) must contain only "error.boolean"
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      errorMessages(result) must contain only "error.required"
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      errorMessages(result) must contain only "error.required"
    }

    "unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" must {

    val testForm: Form[Int] = Form("value" -> int())

    "bind a valid integer" in {
      testForm.bind(Map("value" -> "1")).get mustEqual 1
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      errorMessages(result) must contain only "error.required"
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      errorMessages(result) must contain only "error.required"
    }

    "not bind a non-numeric value" in {
      val result = testForm.bind(Map("value" -> "abc"))
      errorMessages(result) must contain only "error.nonNumeric"
    }

    "reject a decimal value" in {
      val result = testForm.bind(Map("value" -> "1.2"))
      errorMessages(result) must (contain only "error.wholeNumber" or contain only "error.nonNumeric")
    }

    "unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "enumerable" must {

    val testForm = Form("value" -> enumerable[Foo]())

    "bind a valid option" in {
      testForm.bind(Map("value" -> "Bar")).get mustEqual Bar
    }

    "not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      errorMessages(result) must contain only "error.invalid"
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      errorMessages(result) must contain only "error.required"
    }
  }

  "postcode" must {

    val testForm = Form("value" -> postcode())

    "bind and normalise a valid UK postcode without space/lowercase" in {
      val result = testForm.bind(Map("value" -> "ec1a1bb"))
      result.errors mustBe empty
      result.get.replaceAll("\\s", "") mustEqual "EC1A1BB"
    }

    "bind a valid UK postcode with space and mixed case" in {
      val result = testForm.bind(Map("value" -> "Sw1A 1AA"))
      result.errors mustBe empty
      result.get.replaceAll("\\s", "") mustEqual "SW1A1AA"
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      errorMessages(result) must contain only "error.required"
    }

    "not bind an invalid postcode" in {
      val result = testForm.bind(Map("value" -> "###"))
      errorMessages(result) must contain only "error.invalid"
    }
  }

  "localDate" must {

    val form = Form(
      "value" -> localDate(
        invalidKey     = "error.invalid",
        allRequiredKey = "error.allRequired",
        twoRequiredKey = "error.twoRequired",
        requiredKey    = "error.required"
      )
    )

    "bind a valid date" in {
      val today = LocalDate.now()
      val result = form.bind(
        Map(
          "value.day"   -> today.getDayOfMonth.toString,
          "value.month" -> today.getMonthValue.toString,
          "value.year"  -> today.getYear.toString
        )
      )
      result.errors mustBe empty
      result.get mustEqual today
    }

    "error when all fields are missing" in {
      val result = form.bind(Map.empty[String, String])
      errorMessages(result) must contain only "error.allRequired"
    }

    "error when two fields are missing" in {
      val result = form.bind(Map("value.day" -> "12"))
      errorMessages(result) must contain only "error.twoRequired"
    }

    "error when one field is missing" in {
      val r1 = form.bind(Map("value.day" -> "12", "value.month" -> "08"))
      val r2 = form.bind(Map("value.day" -> "12", "value.year" -> "2024"))
      val r3 = form.bind(Map("value.month" -> "08", "value.year" -> "2024"))
      errorMessages(r1) must contain only "error.required"
      errorMessages(r2) must contain only "error.required"
      errorMessages(r3) must contain only "error.required"
    }

    "error when fields are non-numeric" in {
      val result = form.bind(Map("value.day" -> "aa", "value.month" -> "bb", "value.year" -> "cccc"))
      errorMessages(result) must contain only "error.invalid"
    }

    "error when fields form an invalid date" in {
      val result = form.bind(Map("value.day" -> "31", "value.month" -> "02", "value.year" -> "2024"))
      errorMessages(result) must contain only "error.invalid"
    }
  }
}
