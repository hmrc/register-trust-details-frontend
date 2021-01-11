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

package utils

import java.time.LocalDate

import base.SpecBase
import models.Status.{Completed, InProgress}
import pages.TrustDetailsStatus
import pages.register.trust_details.WhenTrustSetupPage
import uk.gov.hmrc.http.HeaderCarrier

class RegistrationProgressSpec extends SpecBase {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  "Trust details section" must {

    "render no tag" when {

      "no status value in user answers" in {
        val registrationProgress = injector.instanceOf[RegistrationProgress]

        val userAnswers = emptyUserAnswers

        registrationProgress.trustDetailsStatus(userAnswers) mustBe None
      }
    }

    "render in-progress tag" when {

      "user has entered when the trust was created" in {

        val registrationProgress = injector.instanceOf[RegistrationProgress]

        val userAnswers = emptyUserAnswers
            .set(WhenTrustSetupPage, LocalDate.of(2010, 10, 10)).success.value

        registrationProgress.trustDetailsStatus(userAnswers).value mustBe InProgress
      }
    }

    "render complete tag" when {

      "user answer has reached check-trust-details" in {

        val registrationProgress = injector.instanceOf[RegistrationProgress]

        val userAnswers = emptyUserAnswers
          .set(WhenTrustSetupPage, LocalDate.of(2010, 10, 10)).success.value
          .set(TrustDetailsStatus, Completed).success.value

        registrationProgress.trustDetailsStatus(userAnswers).value mustBe Completed
      }
    }
  }

}
