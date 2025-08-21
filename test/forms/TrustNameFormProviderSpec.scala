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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import org.scalatest.matchers.must.Matchers._


class TrustNameFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "trustName.error.required"
  private val lengthKey   = "trustName.error.length"
  private val maxLength   = 53
  private val regexp      = "^[A-Za-z0-9 ,.()/&'-]*$"
  private val invalidKey  = "trustName.error.invalidCharacters"

  private val form = new TrustNameFormProvider()()

  ".value" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    "not bind strings longer than 53 characters" in {
      val overMax = "A" * (maxLength + 1)
      val result  = form.bind(Map(fieldName -> overMax))
      result.errors mustBe List(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegexpWithGenerator(
      form,
      fieldName,
      regexp     = regexp,
      generator  = stringsWithMaxLength(maxLength),
      error      = FormError(fieldName, invalidKey, Seq(regexp))
    )

    behave like nonEmptyField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(fieldName))
    )
  }
}
