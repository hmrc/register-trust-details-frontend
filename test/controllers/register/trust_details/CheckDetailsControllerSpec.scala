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

package controllers.register.trust_details

import base.SpecBase
import models.TaskStatus.Completed
import models.UserAnswers
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpResponse
import utils.print.TrustDetailsPrintHelper
import views.html.register.trust_details.CheckDetailsView

import scala.concurrent.Future

class CheckDetailsControllerSpec extends SpecBase with BeforeAndAfterEach {

  private lazy val checkDetailsRoute = routes.CheckDetailsController.onPageLoad(fakeDraftId).url

  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers

  val mockTrustsStoreService: TrustsStoreService = mock[TrustsStoreService]

  override def beforeEach(): Unit = {
    reset(registrationsRepository, mockTrustsStoreService)

    when(registrationsRepository.set(any())(any(), any()))
      .thenReturn(Future.successful(true))

    when(mockTrustsStoreService.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))

    when(registrationsRepository.modifyTaxLiabilityState(any())(any()))
      .thenReturn(Future.successful(()))
  }

  "CheckDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, checkDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckDetailsView]
      val printHelper = application.injector.instanceOf[TrustDetailsPrintHelper]
      val answerSection = printHelper.checkDetailsSection(userAnswers)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(Seq(answerSection), fakeDraftId)(request, messages).toString

      application.stop()
    }

    "set task to completed and redirect for a POST" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
        .build()

      val request = FakeRequest(POST, checkDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      val inOrder = Mockito.inOrder(mockTrustsStoreService, registrationsRepository)
      inOrder.verify(mockTrustsStoreService).updateTaskStatus(eqTo(userAnswers.draftId), eqTo(Completed))(any(), any())
      inOrder.verify(registrationsRepository).set(eqTo(userAnswers))(any(), any())

      application.stop()
    }

  }
}
