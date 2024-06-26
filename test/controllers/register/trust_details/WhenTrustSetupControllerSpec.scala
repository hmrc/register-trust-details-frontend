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

import java.time.{LocalDate, ZoneOffset}
import base.SpecBase
import forms.WhenTrustSetupFormProvider
import models.ReadOnlyUserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.register.trust_details.WhenTrustSetupPage
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.register.trust_details.WhenTrustSetupView

import scala.concurrent.Future

class WhenTrustSetupControllerSpec extends SpecBase {

  val formProvider = new WhenTrustSetupFormProvider(frontendAppConfig)
  val form: Form[LocalDate] = formProvider.withConfig()

  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)

  lazy val whenTrustSetupRoute: String = routes.WhenTrustSetupController.onPageLoad(fakeDraftId).url

  "WhenTrustSetup Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, whenTrustSetupRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[WhenTrustSetupView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, fakeDraftId)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(WhenTrustSetupPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, whenTrustSetupRoute)

      val view = application.injector.instanceOf[WhenTrustSetupView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer),  fakeDraftId)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, whenTrustSetupRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, whenTrustSetupRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[WhenTrustSetupView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm,  fakeDraftId)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when submitted date is before date of death" in {

      val dateOfDeath: LocalDate = LocalDate.parse("2019-02-03")
      val submittedDate: LocalDate = LocalDate.parse("2018-02-03")

      val readOnlyAnswers = ReadOnlyUserAnswers(Json.obj(
        "settlors" -> Json.obj(
          "deceased" -> Json.obj(
            "dateOfDeath" -> dateOfDeath
          )
        )
      ))

      when(registrationsRepository.getMainAnswers(any())(any()))
        .thenReturn(Future.successful(Some(readOnlyAnswers)))

      val form = formProvider.withConfig((dateOfDeath, "beforeDateOfDeath"))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, whenTrustSetupRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> submittedDate.getDayOfMonth.toString,
            "value.month" -> submittedDate.getMonthValue.toString,
            "value.year"  -> submittedDate.getYear.toString
          )

      val boundForm = form.bind(Map(
        "value.day"   -> submittedDate.getDayOfMonth.toString,
        "value.month" -> submittedDate.getMonthValue.toString,
        "value.year"  -> submittedDate.getYear.toString
      ))

      val view = application.injector.instanceOf[WhenTrustSetupView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm,  fakeDraftId)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, whenTrustSetupRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, whenTrustSetupRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
