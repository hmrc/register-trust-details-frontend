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

package pages.register.trust_details

import models.TrusteesBasedInTheUK._
import models.{TrusteesBasedInTheUK, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import sections.TrustDetails

import scala.util.Try

case object TrusteesBasedInTheUKPage extends QuestionPage[TrusteesBasedInTheUK] {

  override def path: JsPath = JsPath \ TrustDetails \ toString

  override def toString: String = "trusteesBasedInTheUK"

  override def cleanup(value: Option[TrusteesBasedInTheUK], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(NonUkBasedTrustees) =>
        userAnswers
          .remove(SettlorsBasedInTheUKPage)
          .flatMap(_.remove(EstablishedUnderScotsLawPage))
          .flatMap(_.remove(TrustResidentOffshorePage))
          .flatMap(_.remove(TrustPreviouslyResidentPage))
      case Some(UKBasedTrustees)    =>
        userAnswers
          .remove(SettlorsBasedInTheUKPage)
          .flatMap(_.remove(TrustHasBusinessRelationshipInUkPage))
          .flatMap(_.remove(RegisteringTrustFor5APage))
          .flatMap(_.remove(InheritanceTaxActPage))
          .flatMap(_.remove(AgentOtherThanBarristerPage))
      case _                        =>
        super.cleanup(value, userAnswers)
    }

}
