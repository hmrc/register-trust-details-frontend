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

package utils.print

import base.SpecBase
import controllers.register.trust_details.routes
import models.TrusteesBasedInTheUK.UKBasedTrustees
import pages.register.trust_details._
import play.twirl.api.HtmlFormat
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class TrustDetailsPrintHelperSpec extends SpecBase {

  private val printHelper = app.injector.instanceOf[TrustDetailsPrintHelper]

  "Trust details print helper" must {

    "return no answer rows" when {
      "there are no answers" in {
        printHelper.printSection(emptyUserAnswers).rows        mustBe Seq.empty
        printHelper.checkDetailsSection(emptyUserAnswers).rows mustBe Seq.empty
      }
    }

    "return answer rows" when {
      "there are answers" in {
        val answers = emptyUserAnswers
          .set(TrustNamePage, "Trust of John")
          .success
          .value
          .set(WhenTrustSetupPage, LocalDate.of(2012, 6, 9))
          .success
          .value
          .set(GovernedInsideTheUKPage, false)
          .success
          .value
          .set(CountryGoverningTrustPage, "FR")
          .success
          .value
          .set(AdministrationInsideUKPage, false)
          .success
          .value
          .set(CountryAdministeringTrustPage, "US")
          .success
          .value
          .set(TrustOwnsUkPropertyOrLandPage, true)
          .success
          .value
          .set(TrustListedOnEeaRegisterPage, true)
          .success
          .value
          .set(TrusteesBasedInTheUKPage, UKBasedTrustees)
          .success
          .value
          .set(SettlorsBasedInTheUKPage, true)
          .success
          .value
          .set(EstablishedUnderScotsLawPage, false)
          .success
          .value
          .set(TrustResidentOffshorePage, false)
          .success
          .value
          .set(TrustPreviouslyResidentPage, "CA")
          .success
          .value
          .set(TrustHasBusinessRelationshipInUkPage, true)
          .success
          .value
          .set(RegisteringTrustFor5APage, true)
          .success
          .value
          .set(InheritanceTaxActPage, false)
          .success
          .value
          .set(AgentOtherThanBarristerPage, true)
          .success
          .value
          .set(Schedule3aExemptYesNoPage, true)
          .success
          .value
          .set(TrustListedOnEeaRegisterPage, true)
          .success
          .value

        val rows = Seq(
          AnswerRow(
            "trustName.checkYourAnswersLabel",
            HtmlFormat.escape("Trust of John"),
            Some(routes.TrustNameController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "whenTrustSetupDate.checkYourAnswersLabel",
            HtmlFormat.escape("9 June 2012"),
            Some(routes.WhenTrustSetupController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "governedInsideTheUKYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("No"),
            Some(routes.GovernedInsideTheUKController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "countryGoverningTrust.checkYourAnswersLabel",
            HtmlFormat.escape("France"),
            Some(routes.CountryGoverningTrustController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "administrationInsideUKYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("No"),
            Some(routes.AdministrationInsideUKController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "countryAdministeringTrust.checkYourAnswersLabel",
            HtmlFormat.escape("United States of America"),
            Some(routes.CountryAdministeringTrustController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "trustOwnsUkPropertyOrLandYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("Yes"),
            Some(routes.TrustOwnsUkPropertyOrLandController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "trustListedOnEeaRegisterYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("Yes"),
            Some(routes.TrustListedOnEeaRegisterController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "trusteesBasedInTheUK.checkYourAnswersLabel",
            HtmlFormat.escape("All of the trustees are based in the UK"),
            Some(routes.TrusteesBasedInTheUKController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "settlorsBasedInTheUKYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("Yes"),
            Some(routes.SettlorsBasedInTheUKController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "establishedUnderScotsLawYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("No"),
            Some(routes.EstablishedUnderScotsLawController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "trustResidentOffshoreYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("No"),
            Some(routes.TrustResidentOffshoreController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "trustPreviouslyResident.checkYourAnswersLabel",
            HtmlFormat.escape("Canada"),
            Some(routes.TrustPreviouslyResidentController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "trustHasBusinessRelationshipInUkYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("Yes"),
            Some(routes.TrustHasBusinessRelationshipInUkController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "registeringTrustFor5AYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("Yes"),
            Some(routes.RegisteringTrustFor5AController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "inheritanceTaxActYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("No"),
            Some(routes.InheritanceTaxActController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "agentOtherThanBarristerYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("Yes"),
            Some(routes.AgentOtherThanBarristerController.onPageLoad(draftId).url)
          ),
          AnswerRow(
            "schedule3aExemptYesNo.checkYourAnswersLabel",
            HtmlFormat.escape("Yes"),
            Some(routes.Schedule3aExemptYesNoController.onPageLoad(draftId).url)
          )
        )

        printHelper.printSection(answers) mustBe AnswerSection(
          headingKey = None,
          rows = rows,
          sectionKey = Some("answerPage.section.trustDetails.heading")
        )

        printHelper.checkDetailsSection(answers) mustBe AnswerSection(
          headingKey = None,
          rows = rows,
          sectionKey = None
        )
      }
    }
  }

}
