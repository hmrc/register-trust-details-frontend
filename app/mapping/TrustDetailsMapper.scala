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

package mapping

import models.TrusteesBasedInTheUK._
import models.{NonUKType, ResidentialStatusType, TrustDetailsType, TrusteesBasedInTheUK, UkType, UserAnswers}
import pages.register.trust_details._
import play.api.Logging
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsSuccess, Reads}
import utils.Constants.GB

import java.time.LocalDate

class TrustDetailsMapper extends Mapping[TrustDetailsType] with Logging {

  override def build(userAnswers: UserAnswers): Option[TrustDetailsType] = {

    def reads: Reads[TrustDetailsType] = (
      WhenTrustSetupPage.path.read[LocalDate] and
        CountryGoverningTrustPage.path.readNullable[String] and
        CountryAdministeringTrustPage.path.read[String].map(Some(_)).orElse(Reads(_ => JsSuccess(Some(GB)))) and
        residentialStatusReads
      )(TrustDetailsType.apply _)

    def residentialStatusReads: Reads[Option[ResidentialStatusType]] = {
      TrusteesBasedInTheUKPage.path.read[TrusteesBasedInTheUK].flatMap {
        case UKBasedTrustees =>
          (ukReads and Reads(_ => JsSuccess(None: Option[NonUKType])))(ResidentialStatusType.apply _)
        case NonUkBasedTrustees =>
          (Reads(_ => JsSuccess(None: Option[UkType])) and nonUkReads)(ResidentialStatusType.apply _)
        case InternationalAndUKTrustees =>
          SettlorsBasedInTheUKPage.path.read[Boolean].flatMap {
            case true => (ukReads and Reads(_ => JsSuccess(None: Option[NonUKType])))(ResidentialStatusType.apply _)
            case false => (Reads(_ => JsSuccess(None: Option[UkType])) and nonUkReads)(ResidentialStatusType.apply _)
          }
      }.map(Some(_))
    }

    def ukReads: Reads[Option[UkType]] = (
      EstablishedUnderScotsLawPage.path.read[Boolean] and
        TrustPreviouslyResidentPage.path.readNullable[String]
      )(UkType.apply _).map(Some(_))

    def nonUkReads: Reads[Option[NonUKType]] = (
      RegisteringTrustFor5APage.path.read[Boolean] and
        InheritanceTaxActPage.path.readNullable[Boolean] and
        AgentOtherThanBarristerPage.path.readNullable[Boolean] and
        Reads(_ => JsSuccess(None))
      )(NonUKType.apply _).map(Some(_))

    userAnswers.data.validate[TrustDetailsType](reads) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors) =>
        logger.error(s"Failed to rehydrate TrustDetailsType from UserAnswers due to $errors")
        None
    }
  }
}
