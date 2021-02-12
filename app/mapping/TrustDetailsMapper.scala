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
        administrationCountryReads(userAnswers) and
        residentialStatusReads(userAnswers)and
        TrustOwnsUkPropertyOrLandPage.path.readNullable[Boolean] and
        TrustListedOnEeaRegisterPage.path.readNullable[Boolean] and
        TrustHasBusinessRelationshipInUkPage.path.readNullable[Boolean] and
        trustUkResidentReads(userAnswers)
      )(TrustDetailsType.apply _)

    userAnswers.data.validate[TrustDetailsType](reads) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors) =>
        logger.error(s"Failed to rehydrate TrustDetailsType from UserAnswers due to $errors")
        None
    }
  }

  private def administrationCountryReads(ua: UserAnswers): Reads[Option[String]] = {
    if (ua.isTaxable) {
      CountryAdministeringTrustPage.path.read[String]
        .map(Some(_): Option[String])
        .orElse(Reads(_ => JsSuccess(Some(GB))))
    } else {
      Reads(_ => JsSuccess(None))
    }
  }

  private def trustUkResidentReads(ua: UserAnswers): Reads[Option[Boolean]] = {
    if (ua.is5mldEnabled) {
      TrusteesBasedInTheUKPage.path.read[TrusteesBasedInTheUK].flatMap {
        case UKBasedTrustees => Reads(_ => JsSuccess(Some(true)))
        case NonUkBasedTrustees => Reads(_ => JsSuccess(Some(false)))
        case InternationalAndUKTrustees =>
          SettlorsBasedInTheUKPage.path.read[Boolean].flatMap {
            case true => Reads(_ => JsSuccess(Some(true)))
            case false => Reads(_ => JsSuccess(Some(false)))
          }
      }
    } else {
      Reads(_ => JsSuccess(None))
    }
  }

  private def residentialStatusReads(ua: UserAnswers): Reads[Option[ResidentialStatusType]] = {
    if (ua.isTaxable) {
      lazy val uk: Reads[Option[ResidentialStatusType]] = (
        ukReads and Reads(_ => JsSuccess(None: Option[NonUKType]))
        )(ResidentialStatusType.apply _).map(Some(_))

      lazy val nonUk: Reads[Option[ResidentialStatusType]] = (
        Reads(_ => JsSuccess(None: Option[UkType])) and nonUkReads
        )(ResidentialStatusType.apply _).map(Some(_))

      TrusteesBasedInTheUKPage.path.read[TrusteesBasedInTheUK].flatMap {
        case UKBasedTrustees => uk
        case NonUkBasedTrustees => nonUk
        case InternationalAndUKTrustees =>
          SettlorsBasedInTheUKPage.path.read[Boolean].flatMap {
            case true => uk
            case false => nonUk
          }
      }
    } else {
      Reads(_ => JsSuccess(None))
    }
  }

  private def ukReads: Reads[Option[UkType]] = {
    (
      EstablishedUnderScotsLawPage.path.read[Boolean] and
        TrustPreviouslyResidentPage.path.readNullable[String]
      )(UkType.apply _).map(Some(_))
  }

  private def nonUkReads: Reads[Option[NonUKType]] = {
    (
      RegisteringTrustFor5APage.path.read[Boolean] and
        InheritanceTaxActPage.path.readNullable[Boolean] and
        AgentOtherThanBarristerPage.path.readNullable[Boolean] and
        Reads(_ => JsSuccess(None))
      )(NonUKType.apply _).map(Some(_))
  }

}
