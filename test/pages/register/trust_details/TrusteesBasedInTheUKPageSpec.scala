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

package pages.register.trust_details

import models.{TrusteesBasedInTheUK, UserAnswers}
import pages.behaviours.PageBehaviours

class TrusteesBasedInTheUKPageSpec extends PageBehaviours {

  "TrusteesBasedInTheUKPage" must {

    beRetrievable[TrusteesBasedInTheUK](TrusteesBasedInTheUKPage)

    beSettable[TrusteesBasedInTheUK](TrusteesBasedInTheUKPage)

    beRemovable[TrusteesBasedInTheUK](TrusteesBasedInTheUKPage)

    "implement cleanup logic" when {

      "UK based trustees selected" in {

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(SettlorsBasedInTheUKPage, true)
          .success
          .value
          .set(TrustHasBusinessRelationshipInUkPage, true)
          .success
          .value
          .set(RegisteringTrustFor5APage, false)
          .success
          .value
          .set(InheritanceTaxActPage, true)
          .success
          .value
          .set(AgentOtherThanBarristerPage, true)
          .success
          .value

        val result = userAnswers.set(TrusteesBasedInTheUKPage, TrusteesBasedInTheUK.UKBasedTrustees).success.value

        result.get(SettlorsBasedInTheUKPage) mustNot be(defined)
        result.get(TrustHasBusinessRelationshipInUkPage) mustNot be(defined)
        result.get(RegisteringTrustFor5APage) mustNot be(defined)
        result.get(InheritanceTaxActPage) mustNot be(defined)
        result.get(AgentOtherThanBarristerPage) mustNot be(defined)
      }

      "non-UK based trustees selected" in {

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(SettlorsBasedInTheUKPage, false)
          .success
          .value
          .set(EstablishedUnderScotsLawPage, true)
          .success
          .value
          .set(TrustResidentOffshorePage, true)
          .success
          .value
          .set(TrustPreviouslyResidentPage, "FR")
          .success
          .value

        val result = userAnswers.set(TrusteesBasedInTheUKPage, TrusteesBasedInTheUK.NonUkBasedTrustees).success.value

        result.get(SettlorsBasedInTheUKPage) mustNot be(defined)
        result.get(EstablishedUnderScotsLawPage) mustNot be(defined)
        result.get(TrustResidentOffshorePage) mustNot be(defined)
        result.get(TrustPreviouslyResidentPage) mustNot be(defined)
      }
    }
  }

}
