/*
 * Copyright 2021 HM Revenue & Customs
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

import models.Status._
import models.UserAnswers
import pages.TrustDetailsStatus
import pages.behaviours.PageBehaviours

class RegisteringTrustFor5APageSpec extends PageBehaviours {

  "RegisteringTrustFor5APage" must {

    beRetrievable[Boolean](RegisteringTrustFor5APage)

    beSettable[Boolean](RegisteringTrustFor5APage)

    beRemovable[Boolean](RegisteringTrustFor5APage)

    "implement cleanup logic" when {

      "yes selected" in {

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(InheritanceTaxActPage, true).success.value
          .set(AgentOtherThanBarristerPage, true).success.value

        val result = userAnswers.set(RegisteringTrustFor5APage, true).success.value

        result.get(InheritanceTaxActPage) mustNot be(defined)
        result.get(AgentOtherThanBarristerPage) mustNot be(defined)
      }
    }

    "no selected" when {

      "InheritanceTaxActPage is not answered" must {
        "set TrustDetailsStatus to InProgress" in {

          val userAnswers: UserAnswers = emptyUserAnswers
            .set(TrustDetailsStatus, Completed).success.value

          val result = userAnswers.set(RegisteringTrustFor5APage, false).success.value

          result.get(TrustDetailsStatus).get mustBe InProgress
        }
      }

      "InheritanceTaxActPage is answered" must {
        "not set TrustDetailsStatus to InProgress" in {

          val userAnswers: UserAnswers = emptyUserAnswers
            .set(InheritanceTaxActPage, true).success.value
            .set(TrustDetailsStatus, Completed).success.value

          val result = userAnswers.set(RegisteringTrustFor5APage, false).success.value

          result.get(TrustDetailsStatus).get mustBe Completed
        }
      }
    }
  }
}
