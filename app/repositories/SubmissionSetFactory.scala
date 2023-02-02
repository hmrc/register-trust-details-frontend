/*
 * Copyright 2023 HM Revenue & Customs
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

import mapping.TrustDetailsMapper
import models._
import play.api.i18n.Messages
import play.api.libs.json.{JsNull, JsValue, Json}
import utils.print.TrustDetailsPrintHelper
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject
import scala.concurrent.Future

class SubmissionSetFactory @Inject()(trustDetailsMapper: TrustDetailsMapper,
                                     trustDetailsPrintHelper: TrustDetailsPrintHelper) {

  def createFrom(userAnswers: UserAnswers)
                (implicit messages: Messages): Future[RegistrationSubmission.DataSet] = {

    Future.successful {
      RegistrationSubmission.DataSet(
        data = Json.toJson(userAnswers),
        registrationPieces = mappedDataIfCompleted(userAnswers),
        answerSections = answerSectionsIfCompleted(userAnswers)
      )
    }
  }

  private def mappedPieces(trustDetailsJson: JsValue) =
    List(RegistrationSubmission.MappedPiece("trust/details", trustDetailsJson))

  private def mappedDataIfCompleted(userAnswers: UserAnswers): List[RegistrationSubmission.MappedPiece] = {
    trustDetailsMapper.build(userAnswers) match {
      case Some(trustDetails) => mappedPieces(Json.toJson(trustDetails))
      case _ => mappedPieces(JsNull)
    }
  }

  private def answerSectionsIfCompleted(userAnswers: UserAnswers)
                               (implicit messages: Messages): List[RegistrationSubmission.AnswerSection] = {
    val answerSection = trustDetailsPrintHelper.printSection(userAnswers)
    List(answerSection).map(convertForSubmission)
  }

  private def convertForSubmission(section: AnswerSection): RegistrationSubmission.AnswerSection = {
    RegistrationSubmission.AnswerSection(section.headingKey, section.rows.map(convertForSubmission), section.sectionKey)
  }

  private def convertForSubmission(row: AnswerRow): RegistrationSubmission.AnswerRow = {
    RegistrationSubmission.AnswerRow(row.label, row.answer.toString, row.labelArg)
  }

}
