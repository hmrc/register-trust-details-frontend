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

package utils

import base.SpecBase
import models.TrusteesBasedInTheUK.UKBasedTrustees
import pages.register.trust_details._
import utils.print.TrustDetailsPrintHelper
import viewmodels.AnswerSection

import java.time.LocalDate

class SectionFormatterSpec extends SpecBase {

  "SectionFormatter" must {

    "display expected text" in {

      // by using the printHelper we use the application code that will generate the 'check your answers' labels.
      // this is within the class/method: AnswerRowConverter/Bound/question
      // this is  opposed to artificially constructing a sequence of answer sections with the CYA label in this test
      val printHelper = app.injector.instanceOf[TrustDetailsPrintHelper]

      val answers = emptyUserAnswers
        .set(TrustNamePage, "Trust of John").success.value
        .set(WhenTrustSetupPage, LocalDate.of(2012, 6, 9)).success.value
        .set(GovernedInsideTheUKPage, false).success.value
        .set(CountryGoverningTrustPage, "FR").success.value
        .set(AdministrationInsideUKPage, false).success.value
        .set(CountryAdministeringTrustPage, "US").success.value
        .set(TrustOwnsUkPropertyOrLandPage, true).success.value
        .set(TrustListedOnEeaRegisterPage, true).success.value
        .set(TrusteesBasedInTheUKPage, UKBasedTrustees).success.value
        .set(SettlorsBasedInTheUKPage, true).success.value
        .set(EstablishedUnderScotsLawPage, false).success.value
        .set(TrustResidentOffshorePage, false).success.value
        .set(TrustPreviouslyResidentPage, "CA").success.value
        .set(TrustHasBusinessRelationshipInUkPage, true).success.value
        .set(RegisteringTrustFor5APage, true).success.value
        .set(InheritanceTaxActPage, false).success.value
        .set(AgentOtherThanBarristerPage, true).success.value
        .set(Schedule3aExemptYesNoPage, true).success.value
        .set(TrustListedOnEeaRegisterPage, true).success.value

      val answerSection: Seq[AnswerSection] = Seq(printHelper.printSection(answers))

      val summaryListRows = SectionFormatter.formatSections(answerSection)

      val keyValueTextContent: Seq[(String, String)] = summaryListRows.map { row =>
        row.key.content.asHtml.toString() -> row.value.content.asHtml.toString()
      }

      val expectedKeyValueTextContent = Seq(
        "What is the trust’s name?" -> "Trust of John",
        "When was the trust created?" -> "9 June 2012",
        "Is the trust governed by UK law?" -> "No",
        "What country governs the trust?" -> "France",
        "Is the trust’s general administration done in the UK?" -> "No",
        "In what country is the trust administered?" -> "United States of America",
        "Has the trust acquired land or property in the UK since 6 October 2020?" -> "Yes",
        "Is the trust registered on the trust register of any countries within the EEA?" -> "Yes",
        "How many of the trustees are based in the UK?" -> "All of the trustees are based in the UK",
        "Are any of the settlors based in the UK?" -> "Yes",
        "Is the trust established under Scots Law?" -> "No",
        "Has the trust ever been resident offshore?" -> "No",
        "In what country was the trust previously resident?" -> "Canada",
        "Does the trust have a business relationship in the UK?" -> "Yes",
        "Are you registering the trust because the settlor benefits from the trust’s assets?" -> "Yes",
        "Are you registering the trust for Inheritance Tax reasons?" -> "No",
        "Has an agent who is not a barrister created this trust?" -> "Yes",
        "Does the trust have a Schedule 3a data sharing exemption?" -> "Yes"
      )

      val contentTestData = keyValueTextContent zip expectedKeyValueTextContent

      contentTestData.foreach {
        case (result, expected) =>
          val (resultKey, resultValue) = result
          val (expectedKey, expectedValue) = expected

          resultKey mustBe expectedKey
          resultValue mustBe expectedValue
      }
    }
  }
}
