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

import java.time.LocalDate
import generators.Generators
import org.scalacheck.Gen
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.validation.{Invalid, Valid}

class ConstraintsSpec
  extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with Generators
    with Constraints {

  "firstError" must {
    "return Valid when all constraints pass" in {
      val result = firstError(
        maxLength(10, "error.length"),
        regexp("""^\w+$""", "error.regexp")
      )("foo")
      result mustEqual Valid
    }

    "return Invalid when the first constraint fails" in {
      val result = firstError(
        maxLength(10, "error.length"),
        regexp("""^\w+$""", "error.regexp")
      )("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }

    "return Invalid when the second constraint fails" in {
      val result = firstError(
        maxLength(10, "error.length"),
        regexp("""^\w+$""", "error.regexp")
      )("")
      result mustEqual Invalid("error.regexp", """^\w+$""")
    }

    "return Invalid for the first error when both constraints fail" in {
      val result = firstError(
        maxLength(-1, "error.length"),
        regexp("""^\w+$""", "error.regexp")
      )("")
      result mustEqual Invalid("error.length", -1)
    }
  }

  "minimumValue" must {
    "return Valid for a number greater than the threshold" in {
      minimumValue(1, "error.min").apply(2) mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      minimumValue(1, "error.min").apply(1) mustEqual Valid
    }

    "return Invalid for a number below the threshold" in {
      minimumValue(1, "error.min").apply(0) mustEqual Invalid("error.min", 1)
    }
  }

  "maximumValue" must {
    "return Valid for a number less than the threshold" in {
      maximumValue(1, "error.max").apply(0) mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      maximumValue(1, "error.max").apply(1) mustEqual Valid
    }

    "return Invalid for a number above the threshold" in {
      maximumValue(1, "error.max").apply(2) mustEqual Invalid("error.max", 1)
    }
  }

  "inRange" must {
    "return Valid when inside the range (inclusive on both ends)" in {
      inRange(1, 3, "error.range").apply(2) mustEqual Valid
      inRange(1, 3, "error.range").apply(1) mustEqual Valid
      inRange(1, 3, "error.range").apply(3) mustEqual Valid
    }

    "return Invalid when below the minimum" in {
      inRange(1, 3, "error.range").apply(0) mustEqual Invalid("error.range", 1, 3)
    }

    "return Invalid when above the maximum" in {
      inRange(1, 3, "error.range").apply(4) mustEqual Invalid("error.range", 1, 3)
    }
  }

  "regexp" must {
    "return Valid for an input that matches the expression" in {
      regexp("""^\w+$""", "error.invalid")("foo") mustEqual Valid
    }

    "return Invalid for an input that does not match the expression" in {
      regexp("""^\d+$""", "error.invalid")("foo") mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" must {
    "return Valid for a string shorter than the allowed length" in {
      maxLength(10, "error.length")("a" * 9) mustEqual Valid
    }

    "return Valid for an empty string" in {
      maxLength(10, "error.length")("") mustEqual Valid
    }

    "return Valid for a string equal to the allowed length" in {
      maxLength(10, "error.length")("a" * 10) mustEqual Valid
    }

    "return Invalid for a string longer than the allowed length" in {
      maxLength(10, "error.length")("a" * 11) mustEqual Invalid("error.length", 10)
    }
  }

  "minLength" must {
    "return Invalid for an empty string" in {
      minLength(1, "error.minLen")("") mustEqual Invalid("error.minLen", 1)
    }

    "return Invalid for a string shorter than the minimum" in {
      minLength(5, "error.minLen")("abcd") mustEqual Invalid("error.minLen", 5)
    }

    "return Valid for a string equal to the minimum" in {
      minLength(5, "error.minLen")("abcde") mustEqual Valid
    }

    "return Valid for a string longer than the minimum" in {
      minLength(5, "error.minLen")("abcdef") mustEqual Valid
    }
  }

  "maxDate" must {
    "return Valid for a date before or equal to the maximum" in {
      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), max)
      } yield (max, date)

      forAll(gen) { case (max, date) =>
        maxDate(max, "error.future")(date) mustEqual Valid
      }
    }

    "return Invalid for a date after the maximum" in {
      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(max.plusDays(1), LocalDate.of(3000, 1, 2))
      } yield (max, date)

      forAll(gen) { case (max, date) =>
        maxDate(max, "error.future", "foo")(date) mustEqual Invalid("error.future", "foo")
      }
    }
  }

  "minDate" must {
    "return Valid for a date after or equal to the minimum" in {
      val gen: Gen[(LocalDate, LocalDate)] = for {
        min  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(min, LocalDate.of(3000, 1, 1))
      } yield (min, date)

      forAll(gen) { case (min, date) =>
        minDate(min, "error.past", "foo")(date) mustEqual Valid
      }
    }

    "return Invalid for a date before the minimum" in {
      val gen: Gen[(LocalDate, LocalDate)] = for {
        min  <- datesBetween(LocalDate.of(2000, 1, 2), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), min.minusDays(1))
      } yield (min, date)

      forAll(gen) { case (min, date) =>
        minDate(min, "error.past", "foo")(date) mustEqual Invalid("error.past", "foo")
      }
    }
  }

  "nonEmptySet" must {
    "return Valid for a non-empty set" in {
      nonEmptySet("error.emptySet")(Set(1)) mustEqual Valid
    }

    "return Invalid for an empty set" in {
      nonEmptySet("error.emptySet")(Set.empty[Any]) mustEqual Invalid("error.emptySet")
    }
  }

  "nonEmptyString" must {
    "return Valid for a non-blank string" in {
      nonEmptyString("valueName", "error.required")("hello") mustEqual Valid
    }

    "return Valid for a string with leading/trailing spaces but non-empty when trimmed" in {
      nonEmptyString("valueName", "error.required")("  hi ") mustEqual Valid
    }

    "return Invalid for an empty string" in {
      nonEmptyString("valueName", "error.required")("") mustEqual Invalid("error.required", "valueName")
    }

    "return Invalid for a whitespace-only string" in {
      nonEmptyString("valueName", "error.required")("   \t") mustEqual Invalid("error.required", "valueName")
    }
  }
}
