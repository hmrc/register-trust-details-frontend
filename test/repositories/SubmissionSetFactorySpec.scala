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

package repositories

import base.SpecBase
import generators.ModelGenerators
import models.{RegistrationSubmission, TrustDetailsType, TrusteesBasedInTheUK, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.register.trust_details.{TrusteesBasedInTheUKPage, WhenTrustSetupPage}
import play.api.libs.json.{JsNull, Json}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class SubmissionSetFactorySpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val factory: SubmissionSetFactory = injector.instanceOf[SubmissionSetFactory]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Submission set factory" must {

    "return no answer sections if there are no answers" in {
      val userAnswers = emptyUserAnswers

      val convertedAnswerSection = RegistrationSubmission.AnswerSection(
        headingKey = None,
        rows = Nil,
        sectionKey = Some("answerPage.section.trustDetails.heading")
      )

      whenReady(factory.createFrom(userAnswers)) {
        _ mustBe RegistrationSubmission.DataSet(
          data = Json.toJson(userAnswers),
          registrationPieces = List(RegistrationSubmission.MappedPiece("trust/details", JsNull)),
          answerSections = List(convertedAnswerSection)
        )
      }
    }

    "return answer sections if there are answers" in {

      val expectedMappedEtmpData = TrustDetailsType(
        startDate = LocalDate.parse("2000-01-01"),
        lawCountry = None,
        administrationCountry = None,
        residentialStatus = None,
        trustUKProperty = None,
        trustRecorded = None,
        trustUKRelation = None,
        trustUKResident = Some(true)
      )

      val convertedAnswerSection = RegistrationSubmission.AnswerSection(
        headingKey = None,
        rows = Seq(
          RegistrationSubmission.AnswerRow("whenTrustSetupDate.checkYourAnswersLabel", "1 January 2000", ""),
          RegistrationSubmission
            .AnswerRow("trusteesBasedInTheUK.checkYourAnswersLabel", "All of the trustees are based in the UK", "")
        ),
        sectionKey = Some("answerPage.section.trustDetails.heading")
      )

      val userAnswers = UserAnswers(draftId, Json.obj(), internalAuthId = userInternalId, isTaxable = false)
        .set(WhenTrustSetupPage, LocalDate.parse("2000-01-01"))
        .success
        .value
        .set(TrusteesBasedInTheUKPage, TrusteesBasedInTheUK.UKBasedTrustees)
        .success
        .value

      val expectedDataSet = RegistrationSubmission.DataSet(
        data = Json.toJson(userAnswers),
        registrationPieces = List(
          RegistrationSubmission.MappedPiece(
            "trust/details",
            Json.toJson(expectedMappedEtmpData)
          )
        ),
        answerSections = List(convertedAnswerSection)
      )

      whenReady(factory.createFrom(userAnswers)) { x =>
        x mustBe expectedDataSet
      }
    }

  }

}
