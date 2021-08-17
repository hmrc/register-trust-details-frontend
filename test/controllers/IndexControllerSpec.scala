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

package controllers

import base.SpecBase
import connectors.SubmissionDraftConnector
import models.TaskStatus._
import models.UserAnswers
import models.registration.Matched.Success
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.BeforeAndAfterEach
import pages.register.ExistingTrustMatched
import play.api.inject.bind
import play.api.libs.json.JsString
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val trustsStoreService: TrustsStoreService = mock[TrustsStoreService]
  private val submissionDraftConnector: SubmissionDraftConnector = mock[SubmissionDraftConnector]

  override def beforeEach(): Unit = {
    reset(registrationsRepository, trustsStoreService)

    when(registrationsRepository.set(any())(any(), any()))
      .thenReturn(Future.successful(true))

    when(trustsStoreService.getTaskStatus(any())(any(), any())).thenReturn(Future.successful(InProgress))

    when(trustsStoreService.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))
  }

  "Index Controller" when {

    "pre-existing user answers" when {

      "trust details completed" must {
        "redirect to CheckDetailsController" in {

          val answers = emptyUserAnswers

          val application = applicationBuilder()
            .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
            .build()

          when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(answers)))
          when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(answers)))
          when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
          when(trustsStoreService.getTaskStatus(any())(any(), any())).thenReturn(Future.successful(Completed))

          val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(fakeDraftId).url

          val inOrder = Mockito.inOrder(trustsStoreService)
          inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
          inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

          application.stop()
        }
      }

      "trust details in progress or not started" when {

        "trust has been matched" must {
          "redirect to WhenTrustSetupController" in {

            val answers = emptyUserAnswers
              .setAtPath(ExistingTrustMatched.path, JsString(Success.toString)).success.value

            val application = applicationBuilder()
              .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
              .build()

            when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
            when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(answers)))
            when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

            val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe controllers.register.trust_details.routes.WhenTrustSetupController.onPageLoad(fakeDraftId).url

            val inOrder = Mockito.inOrder(trustsStoreService)
            inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
            inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

            application.stop()
          }
        }

        "trust has not been matched" must {
          "redirect to TrustNameController" in {

            val answers = emptyUserAnswers

            val application = applicationBuilder()
              .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
              .build()

            when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(answers)))
            when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(answers)))
            when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

            val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe controllers.register.trust_details.routes.TrustNameController.onPageLoad(fakeDraftId).url

            val inOrder = Mockito.inOrder(trustsStoreService)
            inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
            inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

            application.stop()
          }
        }
      }

      "update value of is5mldEnabled and isTaxable to true in user answers" in {

        val userAnswers = emptyUserAnswers.copy(is5mldEnabled = false)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
          .build()

        when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))
        when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(true))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

        route(application, request).value.map { _ =>
          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

          uaCaptor.getValue.is5mldEnabled mustBe true
          uaCaptor.getValue.isTaxable mustBe true

          val inOrder = Mockito.inOrder(trustsStoreService)
          inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
          inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

          application.stop()
        }
      }

      "update value of is5mldEnabled and isTaxable to false in user answers" in {

        val userAnswers = emptyUserAnswers.copy(is5mldEnabled = false)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
          .build()

        when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
        when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

        route(application, request).value.map { _ =>
          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

          uaCaptor.getValue.is5mldEnabled mustBe false
          uaCaptor.getValue.isTaxable mustBe false

          val inOrder = Mockito.inOrder(trustsStoreService)
          inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
          inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

          application.stop()
        }
      }
    }

    "no pre-existing user answers" must {

      "redirect to WhenTrustSetupController" when {
        "trust has been matched" in {

          val answers = emptyUserAnswers
            .setAtPath(ExistingTrustMatched.path, JsString(Success.toString)).success.value

          val application = applicationBuilder()
            .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
            .build()

          when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
          when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(answers)))
          when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

          val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.register.trust_details.routes.WhenTrustSetupController.onPageLoad(fakeDraftId).url

          val inOrder = Mockito.inOrder(trustsStoreService)
          inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
          inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

          application.stop()
        }
      }

      "redirect to TrustNameController" when {
        "trust has not been matched" in {

          val answers = emptyUserAnswers

          val application = applicationBuilder()
            .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
            .build()

          when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
          when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(answers)))
          when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

          val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.register.trust_details.routes.TrustNameController.onPageLoad(fakeDraftId).url

          val inOrder = Mockito.inOrder(trustsStoreService)
          inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
          inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

          application.stop()
        }
      }

      "instantiate new set of user answers" when {

        "5mld enabled" must {
          "add is5mldEnabled = true to user answers" in {

            val answers = emptyUserAnswers

            val application = applicationBuilder(userAnswers = None)
              .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
              .build()

            when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
            when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(answers)))
            when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

            val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

            route(application, request).value.map { _ =>
              val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
              verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

              uaCaptor.getValue.is5mldEnabled mustBe true
              uaCaptor.getValue.draftId mustBe fakeDraftId
              uaCaptor.getValue.internalAuthId mustBe "internalId"

              val inOrder = Mockito.inOrder(trustsStoreService)
              inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
              inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

              application.stop()
            }
          }
        }

        "5mld not enabled" must {
          "add is5mldEnabled = false to user answers" in {

            val answers = emptyUserAnswers

            val application = applicationBuilder(userAnswers = None)
              .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
              .build()

            when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
            when(registrationsRepository.getMainAnswers(any())(any())).thenReturn(Future.successful(Some(answers)))
            when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

            val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

            route(application, request).value.map { _ =>
              val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
              verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

              uaCaptor.getValue.is5mldEnabled mustBe false
              uaCaptor.getValue.draftId mustBe fakeDraftId
              uaCaptor.getValue.internalAuthId mustBe "internalId"

              val inOrder = Mockito.inOrder(trustsStoreService)
              inOrder.verify(trustsStoreService).getTaskStatus(eqTo(draftId))(any(), any())
              inOrder.verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

              application.stop()
            }
          }
        }
      }
    }
  }
}
