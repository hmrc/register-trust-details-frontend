/*
 * Copyright 2023 HM Revenue & Customs
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

import models.UserAnswers
import pages.behaviours.PageBehaviours

class TrustResidentOffshorePageSpec extends PageBehaviours {

  "TrustResidentOffshorePage" must {

    beRetrievable[Boolean](TrustResidentOffshorePage)

    beSettable[Boolean](TrustResidentOffshorePage)

    beRemovable[Boolean](TrustResidentOffshorePage)

    "implement cleanup logic" when {

      "no selected" in {

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(TrustPreviouslyResidentPage, "FR").success.value

        val result = userAnswers.set(TrustResidentOffshorePage, false).success.value

        result.get(TrustPreviouslyResidentPage) mustNot be(defined)
      }
    }
  }
}
