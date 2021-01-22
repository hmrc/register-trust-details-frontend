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
import models.{NonUKType, ResidentialStatusType, TrustDetailsType, UkType, UserAnswers}
import play.api.Logging
import pages.register.trust_details._

class TrustDetailsMapper extends Mapping[TrustDetailsType] with Logging {

  override def build(userAnswers: UserAnswers): Option[TrustDetailsType] = {
    for {
      startDateOption <- userAnswers.get(WhenTrustSetupPage)
      lawCountry = userAnswers.get(CountryGoverningTrustPage)
      administrationCountryOption <- administrationCountry(userAnswers)
      residentialStatusOption <- residentialStatus(userAnswers)
    } yield {
      models.TrustDetailsType(
        startDate = startDateOption,
        lawCountry = lawCountry,
        administrationCountry = Some(administrationCountryOption),
        residentialStatus = Some(residentialStatusOption)
      )
    }
  }

  private def administrationCountry(userAnswers: UserAnswers): Option[String] = {
    userAnswers.get(AdministrationInsideUKPage) match {
      case Some(true) =>
        Some("GB")
      case Some(false) =>
        userAnswers.get(CountryAdministeringTrustPage)
      case None =>
        logger.info(s"[administrationCountry][build] unable to determine where trust is administered")
        None
    }
  }
  
  private def residentialStatus(userAnswers: UserAnswers): Option[ResidentialStatusType] = {
    userAnswers.get(TrusteesBasedInTheUKPage) match {
      case Some(UKBasedTrustees) =>
        ukResidentMap(userAnswers)
      case Some(NonUkBasedTrustees) =>
        nonUkResidentMap(userAnswers)
      case Some(InternationalAndUKTrustees) =>
        userAnswers.get(SettlorsBasedInTheUKPage) match {
          case Some(true) =>
            ukResidentMap(userAnswers)
          case Some(false) =>
            nonUkResidentMap(userAnswers)
          case  _ =>
            logger.info("[residentialStatus][build] unable to determine if all settlors are based in the UK")
            None
        }
      case _ =>
        logger.info(s"[residentialStatus][build] unable to determine where trust is resident")
        None
    }
  }

  private def nonUkResidentMap(userAnswers: UserAnswers) = {
    val registeringTrustFor5A = userAnswers.get(RegisteringTrustFor5APage)

    val nonUKConstruct: Option[NonUKType] = registeringTrustFor5A match {
      case Some(true) =>
        Some(
          NonUKType(
            sch5atcgga92 = true,
            s218ihta84 = None,
            agentS218IHTA84 = None,
            trusteeStatus = None)
        )

      case Some(false) =>
        inheritanceTaxAndAgentBarristerMap(userAnswers)

      case _ =>
        logger.info(s"[nonUkResidentMap][build] unable to build non UK resident or inheritance")
        None
    }

    nonUKConstruct match {
      case x if x.isDefined =>
        Some(models.ResidentialStatusType(None, x)
        )
      case _ =>
        logger.info(s"[nonUkResidentMap][build] unable to create residential status")
        None
    }
  }

  private def ukResidentMap(userAnswers: UserAnswers) = {
    val scotsLaw = userAnswers.get(EstablishedUnderScotsLawPage)
    val trustOffShoreYesNo = userAnswers.get(TrustResidentOffshorePage)
    val trustOffShoreCountry = userAnswers.get(TrustPreviouslyResidentPage)
    scotsLaw.map {
      scots =>
        ResidentialStatusType(
          uk = Some(UkType(
            scottishLaw = scots,
            preOffShore = trustOffShoreYesNo match {
              case Some(true) => trustOffShoreCountry
              case _ => None
            }
          )),
          nonUK = None
        )
    }
  }

  private def inheritanceTaxAndAgentBarristerMap(userAnswers: UserAnswers): Option[NonUKType] = {
    val s218ihta84 = userAnswers.get(InheritanceTaxActPage)
    val agentS218IHTA84 = userAnswers.get(AgentOtherThanBarristerPage)

    s218ihta84 match {
      case Some(_) =>
        Some(
          NonUKType(
            sch5atcgga92 = false,
            s218ihta84 = s218ihta84,
            agentS218IHTA84 = agentS218IHTA84,
            trusteeStatus = None)
        )

      case _ => None
    }
  }
}
