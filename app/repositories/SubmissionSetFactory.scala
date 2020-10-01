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

package repositories

import javax.inject.Inject
import mapping.TrustDetailsMapper
import models._
import play.api.libs.json.{JsNull, JsValue, Json}
import utils.RegistrationProgress

class SubmissionSetFactory @Inject()(registrationProgress: RegistrationProgress,
                                    trustDetailsMapper: TrustDetailsMapper) {

  def createFrom(userAnswers: UserAnswers): RegistrationSubmission.DataSet = {

    val status = registrationProgress.trustDetailsStatus(userAnswers)

    RegistrationSubmission.DataSet(
      Json.toJson(userAnswers),
      status,
      mappedDataIfCompleted(userAnswers, status),
      List.empty
    )
  }

  private def mappedPieces(protectorsJson: JsValue) =
    List(RegistrationSubmission.MappedPiece("trust/entities/trust-details", protectorsJson))

  private def mappedDataIfCompleted(userAnswers: UserAnswers, status: Option[Status]) = {
    if (status.contains(Status.Completed)) {
      trustDetailsMapper.build(userAnswers) match {
        case Some(assets) => mappedPieces(Json.toJson(assets))
        case _ => mappedPieces(JsNull)
      }
    } else {
      mappedPieces(JsNull)
    }
  }
}
