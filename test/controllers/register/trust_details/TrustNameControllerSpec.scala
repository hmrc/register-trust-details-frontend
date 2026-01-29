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

package controllers.register.trust_details

import base.SpecBase
import forms.TrustNameFormProvider
import generators.Generators
import models.{ReadOnlyUserAnswers, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.register.trust_details.TrustNamePage
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.register.trust_details.TrustNameView

import scala.concurrent.Future

class TrustNameControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  val formProvider       = new TrustNameFormProvider()
  val form: Form[String] = formProvider()

  lazy val trustNameRoute: String = routes.TrustNameController.onPageLoad(fakeDraftId).url

  def readOnlyAnswers(trustHaveAUTR: Boolean): ReadOnlyUserAnswers = ReadOnlyUserAnswers(
    Json.obj(
      "trustHaveAUTR" -> trustHaveAUTR
    )
  )

  "TrustName Controller" when {

    "an existing trust" must {

      "return OK and the correct view for a GET" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          when(registrationsRepository.getMainAnswers(any())(any()))
            .thenReturn(Future.successful(Some(readOnlyAnswers(true))))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, trustNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[TrustNameView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, fakeDraftId, hintTextShown = true)(request, messages).toString
        }

      "populate the view correctly on a GET when the question has previously been answered" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          when(registrationsRepository.getMainAnswers(any())(any()))
            .thenReturn(Future.successful(Some(readOnlyAnswers(true))))

          val answers = userAnswers
            .set(TrustNamePage, "This Name")
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          val request = FakeRequest(GET, trustNameRoute)

          val view = application.injector.instanceOf[TrustNameView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill("This Name"), fakeDraftId, hintTextShown = true)(request, messages).toString

          application.stop()
        }

      "return a Bad Request and errors when invalid data is submitted" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          when(registrationsRepository.getMainAnswers(any())(any()))
            .thenReturn(Future.successful(Some(readOnlyAnswers(true))))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request =
            FakeRequest(POST, trustNameRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[TrustNameView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, fakeDraftId, hintTextShown = true)(request, messages).toString

          application.stop()
        }

    }

    "a new trust" must {

      "return OK and the correct view for a GET" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          when(registrationsRepository.getMainAnswers(any())(any()))
            .thenReturn(Future.successful(Some(readOnlyAnswers(false))))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, trustNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[TrustNameView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, fakeDraftId, hintTextShown = false)(request, messages).toString

          application.stop()
        }

      "populate the view correctly on a GET when the question has previously been answered" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          when(registrationsRepository.getMainAnswers(any())(any()))
            .thenReturn(Future.successful(Some(readOnlyAnswers(false))))

          val answers = userAnswers
            .set(TrustNamePage, "This Name")
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(answers)).build()
          val request     = FakeRequest(GET, trustNameRoute)

          val view = application.injector.instanceOf[TrustNameView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill("This Name"), fakeDraftId, hintTextShown = false)(request, messages).toString

          application.stop()
        }

      "return a Bad Request and errors when invalid data is submitted" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          when(registrationsRepository.getMainAnswers(any())(any()))
            .thenReturn(Future.successful(Some(readOnlyAnswers(false))))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request =
            FakeRequest(POST, trustNameRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[TrustNameView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, fakeDraftId, hintTextShown = false)(request, messages).toString

          application.stop()
        }

    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, trustNameRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, trustNameRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, trustNameRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

  }

}
