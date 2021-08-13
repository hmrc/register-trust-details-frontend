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

import base.SpecBase
import generators.ModelGenerators
import models.TaskStatus._
import models.{Status, TaskStatus}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.register.trust_details.WhenTrustSetupPage
import services.TrustsStoreService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class RegistrationProgressSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val mockService: TrustsStoreService = mock[TrustsStoreService]
  val registrationProgress: RegistrationProgress = new RegistrationProgress(mockService)
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "RegistrationProgress" when {

    "WhenTrustSetupPage not populated" must {

      val userAnswers = emptyUserAnswers

      "return None" in {

        whenReady(registrationProgress.trustDetailsStatus(userAnswers)) {
          _ mustBe None
        }
      }
    }

    "WhenTrustSetupPage populated" when {

      val userAnswers = emptyUserAnswers.set(WhenTrustSetupPage, LocalDate.parse("2010-10-10")).success.value

      "task is not completed" must {
        "return Some(InProgress)" in {

          forAll(arbitrary[TaskStatus].suchThat(_ != TaskStatus.Completed)) { taskStatus =>
            when(mockService.getTaskStatus(any())(any(), any())).thenReturn(Future.successful(taskStatus))

            whenReady(registrationProgress.trustDetailsStatus(userAnswers)) {
              _ mustBe Some(Status.InProgress)
            }
          }
        }
      }

      "task is completed" must {
        "return Some(Completed)" in {

          when(mockService.getTaskStatus(any())(any(), any())).thenReturn(Future.successful(TaskStatus.Completed))

          whenReady(registrationProgress.trustDetailsStatus(userAnswers)) {
            _ mustBe Some(Status.Completed)
          }
        }
      }
    }
  }

}
