/*
 * Copyright 2026 HM Revenue & Customs
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

package views.register.trust_details

import forms.YesNoFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.trust_details.TrustListedOnEeaRegisterView

class TrustListedOnEeaRegisterViewSpec extends YesNoViewBehaviours {

  private val messageKeyPrefix = "trustListedOnEeaRegisterYesNo"

  override val form: Form[Boolean] = new YesNoFormProvider().withPrefix(messageKeyPrefix)

  "TrustListedOnEeaRegisterView" must {

    val view = viewFor[TrustListedOnEeaRegisterView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, fakeDraftId)(fakeRequest, messages)

    val expectedGuidanceKeys =
      Seq("paragraph", "text", "text.bullet1", "text.bullet2", "text.bullet3", "text.bullet4", "question")

    behave like normalPage(applyView(form), messageKeyPrefix, expectedGuidanceKeys: _*)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(
      form = form,
      createView = applyView,
      messageKeyPrefix = messageKeyPrefix,
      legendAsHeading = false
    )

    behave like pageWithASubmitButton(applyView(form))
  }

}
