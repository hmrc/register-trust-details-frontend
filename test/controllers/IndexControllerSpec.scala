/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import java.time.LocalDate

import base.SpecBase
import models.Status.Completed
import models.registration.Matched.Success
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.TrustDetailsStatus
import pages.register.ExistingTrustMatched
import pages.register.trust_details.{TrustNamePage, WhenTrustSetupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  "Index Controller" must {
    "go to TrustName page" when {
      "no answers exist yet for the draft id" in {

        when(registrationsRepository.get(any())(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder().build()

        val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe
          Some(controllers.register.trust_details.routes.TrustNameController.onPageLoad(fakeDraftId).url)

      }
    }

    "trust details has been answered" must {

      "go to Check Trust Answers Page" in {
        val answers = emptyUserAnswers
          .set(TrustNamePage, "Trust of John").success.value
          .set(WhenTrustSetupPage, LocalDate.of(2010, 10, 10)).success.value
          .set(TrustDetailsStatus, Completed).success.value

        when(registrationsRepository.get(any())(any()))
          .thenReturn(Future.successful(Some(answers)))

        val application = applicationBuilder().build()

        val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result)  mustBe Some(controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(fakeDraftId).url)

        application.stop()
      }

    }

    "trust details has not been answered" when {

      "trust has been matched" must {
        "go to WhenTrustSetup Page" in {
          val answers = emptyUserAnswers
            .set(ExistingTrustMatched, Success).success.value

          when(registrationsRepository.get(any())(any()))
            .thenReturn(Future.successful(Some(emptyUserAnswers)))

          when(registrationsRepository.getMainAnswers(any())(any()))
            .thenReturn(Future.successful(Some(answers)))

          val application = applicationBuilder().build()

          val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe
            Some(controllers.register.trust_details.routes.WhenTrustSetupController.onPageLoad(fakeDraftId).url)
        }
      }

      "trust has not been matched" must {
        "go to TrustName page" in {
          when(registrationsRepository.get(any())(any()))
            .thenReturn(Future.successful(Some(emptyUserAnswers)))

          when(registrationsRepository.getMainAnswers(any())(any()))
            .thenReturn(Future.successful(Some(emptyUserAnswers)))

          val application = applicationBuilder().build()

          val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe
            Some(controllers.register.trust_details.routes.TrustNameController.onPageLoad(fakeDraftId).url)
        }
      }

    }
  }
}
