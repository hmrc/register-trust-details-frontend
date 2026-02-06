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

package repositories

import base.SpecBase
import connectors.SubmissionDraftConnector
import models._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import pages.register.trust_details.WhenTrustSetupPage
import play.api.http
import play.api.http.Status.OK
import play.api.libs.json.Json
import services.TrustsStoreService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RegistrationRepositorySpec extends SpecBase {

  private val unusedSubmissionSetFactory = Mockito.mock(classOf[SubmissionSetFactory])

  private def createRepository(
    connector: SubmissionDraftConnector,
    submissionSetFactory: SubmissionSetFactory,
    trustsStoreService: TrustsStoreService
  ) =
    new DefaultRegistrationsRepository(connector, frontendAppConfig, submissionSetFactory, trustsStoreService)

  "RegistrationRepository" when {
    "getting user answers" must {
      "read answers from my section" in {
        implicit lazy val hc: HeaderCarrier = HeaderCarrier()

        val draftId = "DraftId"

        val userAnswers = UserAnswers(draftId = draftId, internalAuthId = "internalAuthId")

        val mockConnector         = Mockito.mock(classOf[SubmissionDraftConnector])
        val mockTrustStoreService = Mockito.mock(classOf[TrustsStoreService])

        val repository = createRepository(mockConnector, unusedSubmissionSetFactory, mockTrustStoreService)

        val response = SubmissionDraftResponse(LocalDateTime.now, Json.toJson(userAnswers), None)

        when(mockConnector.getDraftSection(any(), any())(any(), any())).thenReturn(Future.successful(response))

        val result = Await.result(repository.get(draftId), Duration.Inf)

        result mustBe Some(userAnswers)
        verify(mockConnector).getDraftSection(draftId, frontendAppConfig.repositoryKey)(hc, executionContext)
      }
      "read answers from main section" in {
        implicit lazy val hc: HeaderCarrier = HeaderCarrier()

        val draftId = "DraftId"

        val dummyData = Json.parse("""
            |{
            | "data" : {
            |   "someField": "someValue"
            | }
            |}
            |""".stripMargin)

        val mockConnector         = Mockito.mock(classOf[SubmissionDraftConnector])
        val mockTrustStoreService = Mockito.mock(classOf[TrustsStoreService])

        val repository = createRepository(mockConnector, unusedSubmissionSetFactory, mockTrustStoreService)

        val response = SubmissionDraftResponse(LocalDateTime.now, Json.toJson(dummyData), None)

        when(mockConnector.getDraftSection(any(), any())(any(), any())).thenReturn(Future.successful(response))

        val result = Await.result(repository.getMainAnswers(draftId), Duration.Inf)

        val expectedAnswers     = Json.obj("someField" -> "someValue")
        val expectedUserAnswers = ReadOnlyUserAnswers(expectedAnswers)

        result mustBe Some(expectedUserAnswers)
        verify(mockConnector).getDraftSection(draftId, "main")(hc, executionContext)
      }

    }

    "setting user answers" must {
      "write answers to my section" in {
        implicit lazy val hc: HeaderCarrier = HeaderCarrier()

        val draftId = "DraftId"

        val userAnswers = UserAnswers(draftId = draftId, internalAuthId = "internalAuthId")

        val mockConnector         = Mockito.mock(classOf[SubmissionDraftConnector])
        val mockTrustStoreService = Mockito.mock(classOf[TrustsStoreService])

        val submissionSet = RegistrationSubmission.DataSet(
          Json.obj(),
          List.empty,
          List.empty
        )

        val mockSubmissionSetFactory = Mockito.mock(classOf[SubmissionSetFactory])
        when(mockSubmissionSetFactory.createFrom(any())(any())).thenReturn(Future.successful(submissionSet))

        val repository = createRepository(mockConnector, mockSubmissionSetFactory, mockTrustStoreService)

        when(mockConnector.setDraftSectionSet(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(http.Status.OK, "")))

        val result = Await.result(repository.set(userAnswers), Duration.Inf)

        result mustBe true
        verify(mockConnector).setDraftSectionSet(draftId, frontendAppConfig.repositoryKey, submissionSet)(
          hc,
          executionContext
        )
      }
    }

    ".modifyTaxLiabilityState" must {

      "reset and set tax-liability to in-progress" when {

        "start date has been answered and then modified" in {
          val mockConnector            = Mockito.mock(classOf[SubmissionDraftConnector])
          val mockTrustStoreService    = Mockito.mock(classOf[TrustsStoreService])
          val mockSubmissionSetFactory = Mockito.mock(classOf[SubmissionSetFactory])

          val userAnswers = emptyUserAnswers
            .set(WhenTrustSetupPage, LocalDate.of(2015, 11, 5))
            .success
            .value

          implicit val hc: HeaderCarrier = HeaderCarrier()

          val repository = createRepository(mockConnector, mockSubmissionSetFactory, mockTrustStoreService)

          when(mockConnector.getTaxLiabilityStartDate(any())(any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2020, 1, 1))))

          when(mockConnector.resetTaxLiability(any())(any(), any()))
            .thenReturn(Future.successful(HttpResponse.apply(OK, "")))

          when(mockTrustStoreService.updateTaxLiabilityTaskStatus(any(), any())(any(), any()))
            .thenReturn(Future.successful(HttpResponse.apply(OK, "")))

          Await.result(
            repository.modifyTaxLiabilityState(userAnswers),
            Duration.Inf
          )

          verify(mockConnector, times(1)).resetTaxLiability(any())(any(), any())
          verify(mockTrustStoreService, times(1))
            .updateTaxLiabilityTaskStatus(any(), eqTo(TaskStatus.InProgress))(any(), any())
        }

      }

      "not modify state" when {

        "start date has not been modified" in {
          val mockConnector            = Mockito.mock(classOf[SubmissionDraftConnector])
          val mockTrustStoreService    = Mockito.mock(classOf[TrustsStoreService])
          val mockSubmissionSetFactory = Mockito.mock(classOf[SubmissionSetFactory])

          val userAnswers = emptyUserAnswers
            .set(WhenTrustSetupPage, LocalDate.of(2020, 1, 1))
            .success
            .value

          implicit val hc: HeaderCarrier = HeaderCarrier()

          val repository = createRepository(mockConnector, mockSubmissionSetFactory, mockTrustStoreService)

          when(mockConnector.getTaxLiabilityStartDate(any())(any(), any()))
            .thenReturn(Future.successful(Some(LocalDate.of(2020, 1, 1))))

          Await.result(
            repository.modifyTaxLiabilityState(userAnswers),
            Duration.Inf
          )

          verify(mockConnector, never()).resetTaxLiability(any())(any(), any())
          verify(mockTrustStoreService, never())
            .updateTaxLiabilityTaskStatus(any(), eqTo(TaskStatus.InProgress))(any(), any())
        }

        "no start date in tax liability" in {
          val mockConnector            = Mockito.mock(classOf[SubmissionDraftConnector])
          val mockTrustStoreService    = Mockito.mock(classOf[TrustsStoreService])
          val mockSubmissionSetFactory = Mockito.mock(classOf[SubmissionSetFactory])

          val userAnswers = emptyUserAnswers
            .set(WhenTrustSetupPage, LocalDate.of(2020, 1, 1))
            .success
            .value

          implicit val hc: HeaderCarrier = HeaderCarrier()

          val repository = createRepository(mockConnector, mockSubmissionSetFactory, mockTrustStoreService)

          when(mockConnector.getTaxLiabilityStartDate(any())(any(), any()))
            .thenReturn(Future.successful(None))

          Await.result(
            repository.modifyTaxLiabilityState(userAnswers),
            Duration.Inf
          )

          verify(mockConnector, never()).resetTaxLiability(any())(any(), any())
          verify(mockTrustStoreService, never())
            .updateTaxLiabilityTaskStatus(any(), eqTo(TaskStatus.InProgress))(any(), any())
        }

        "no start date in user answers" in {
          val mockConnector            = Mockito.mock(classOf[SubmissionDraftConnector])
          val mockTrustStoreService    = Mockito.mock(classOf[TrustsStoreService])
          val mockSubmissionSetFactory = Mockito.mock(classOf[SubmissionSetFactory])

          val userAnswers = emptyUserAnswers

          implicit val hc: HeaderCarrier = HeaderCarrier()

          val repository = createRepository(mockConnector, mockSubmissionSetFactory, mockTrustStoreService)

          Await.result(
            repository.modifyTaxLiabilityState(userAnswers),
            Duration.Inf
          )

          verify(mockConnector, never()).resetTaxLiability(any())(any(), any())
          verify(mockTrustStoreService, never())
            .updateTaxLiabilityTaskStatus(any(), eqTo(TaskStatus.InProgress))(any(), any())
        }

      }

    }
  }

}
