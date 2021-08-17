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

package repositories

import base.SpecBase
import generators.ModelGenerators
import mapping.TrustDetailsMapper
import models.Status.Completed
import models.{RegistrationSubmission, Status, TrustDetailsType}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsNull, Json}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import utils.RegistrationProgress
import utils.print.TrustDetailsPrintHelper
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate
import scala.concurrent.Future

class SubmissionSetFactorySpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val mockRegistrationProgress: RegistrationProgress = mock[RegistrationProgress]
  val mockMapper: TrustDetailsMapper = mock[TrustDetailsMapper]
  val mockPrintHelper: TrustDetailsPrintHelper = mock[TrustDetailsPrintHelper]
  val factory: SubmissionSetFactory = new SubmissionSetFactory(mockRegistrationProgress, mockMapper, mockPrintHelper)
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Submission set factory" must {

    "return no answer sections if not completed" in {

      forAll(arbitrary[Option[Status]].suchThat(!_.contains(Completed))) { status =>

        when(mockRegistrationProgress.trustDetailsStatus(any())(any(), any())).thenReturn(Future.successful(status))

        val userAnswers = emptyUserAnswers

        whenReady(factory.createFrom(userAnswers)) {
          _ mustBe RegistrationSubmission.DataSet(
            data = Json.toJson(userAnswers),
            status = status,
            registrationPieces = List(RegistrationSubmission.MappedPiece("trust/details", JsNull)),
            answerSections = List.empty
          )
        }
      }
    }

    "return answer sections if completed" in {

      val status = Some(Completed)

      val trustDetails = TrustDetailsType(LocalDate.parse("2000-01-01"), None, None, None, None, None, None, None)

      val answerSection = AnswerSection(
        rows = Seq(AnswerRow("Label", Html("Answer"), None))
      )

      val convertedAnswerSection = RegistrationSubmission.AnswerSection(
        headingKey = None,
        rows = Seq(RegistrationSubmission.AnswerRow("Label", "Answer", "")),
        sectionKey = None
      )

      when(mockRegistrationProgress.trustDetailsStatus(any())(any(), any())).thenReturn(Future.successful(status))
      when(mockMapper.build(any())).thenReturn(Some(trustDetails))
      when(mockPrintHelper.printSection(any())(any())).thenReturn(answerSection)

      val userAnswers = emptyUserAnswers

      whenReady(factory.createFrom(userAnswers)) {
        _ mustBe RegistrationSubmission.DataSet(
          data = Json.toJson(userAnswers),
          status = status,
          registrationPieces = List(RegistrationSubmission.MappedPiece("trust/details", Json.toJson(trustDetails))),
          answerSections = List(convertedAnswerSection)
        )
      }
    }

  }

}
