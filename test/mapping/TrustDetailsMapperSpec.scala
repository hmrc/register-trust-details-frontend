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

import base.SpecBase
import models.TrusteesBasedInTheUK._
import models.{NonUKType, ResidentialStatusType, TrustDetailsType, UkType, UserAnswers}
import pages.register.trust_details._
import utils.Constants.GB

import java.time.LocalDate

class TrustDetailsMapperSpec extends SpecBase {

  private val mapper: TrustDetailsMapper = new TrustDetailsMapper

  private val name: String = "Name"
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val country: String = "FR"

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(TrustNamePage, name).success.value
    .set(WhenTrustSetupPage, date).success.value

  "TrustDetailsMapper" must {

    "build trust details from user answers" when {

      "4mld" when {

        "UK governed, UK administered, all trustees UK based and never based offshore" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(GovernedInsideTheUKPage, true).success.value
            .set(AdministrationInsideUKPage, true).success.value
            .set(TrusteesBasedInTheUKPage, UKBasedTrustees).success.value
            .set(EstablishedUnderScotsLawPage, true).success.value
            .set(TrustResidentOffshorePage, false).success.value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = None,
            administrationCountry = Some(GB),
            residentialStatus = Some(ResidentialStatusType(
              uk = Some(UkType(
                scottishLaw = true,
                preOffShore = None
              )),
              nonUK = None
            ))
          )
        }

        "UK governed, UK administered, all trustees UK based and previously based offshore" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(GovernedInsideTheUKPage, true).success.value
            .set(AdministrationInsideUKPage, true).success.value
            .set(TrusteesBasedInTheUKPage, UKBasedTrustees).success.value
            .set(EstablishedUnderScotsLawPage, false).success.value
            .set(TrustResidentOffshorePage, true).success.value
            .set(TrustPreviouslyResidentPage, country).success.value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = None,
            administrationCountry = Some(GB),
            residentialStatus = Some(ResidentialStatusType(
              uk = Some(UkType(
                scottishLaw = false,
                preOffShore = Some(country)
              )),
              nonUK = None
            ))
          )
        }

        "non-UK governed, non-UK administered, no trustees UK based and settlor benefits from assets" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(GovernedInsideTheUKPage, false).success.value
            .set(CountryGoverningTrustPage, country).success.value
            .set(AdministrationInsideUKPage, false).success.value
            .set(CountryAdministeringTrustPage, country).success.value
            .set(TrusteesBasedInTheUKPage, NonUkBasedTrustees).success.value
            .set(RegisteringTrustFor5APage, true).success.value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = Some(country),
            administrationCountry = Some(country),
            residentialStatus = Some(ResidentialStatusType(
              uk = None,
              nonUK = Some(NonUKType(
                sch5atcgga92 = true,
                s218ihta84 = None,
                agentS218IHTA84 = None,
                trusteeStatus = None
              ))
            ))
          )
        }

        "non-UK governed, non-UK administered, no trustees UK based and not registering for purpose of section 218" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(GovernedInsideTheUKPage, false).success.value
            .set(CountryGoverningTrustPage, country).success.value
            .set(AdministrationInsideUKPage, false).success.value
            .set(CountryAdministeringTrustPage, country).success.value
            .set(TrusteesBasedInTheUKPage, NonUkBasedTrustees).success.value
            .set(RegisteringTrustFor5APage, false).success.value
            .set(InheritanceTaxActPage, false).success.value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = Some(country),
            administrationCountry = Some(country),
            residentialStatus = Some(ResidentialStatusType(
              uk = None,
              nonUK = Some(NonUKType(
                sch5atcgga92 = false,
                s218ihta84 = Some(false),
                agentS218IHTA84 = None,
                trusteeStatus = None
              ))
            ))
          )
        }

        "non-UK governed, non-UK administered, some trustees UK based and registering for purpose of section 218" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(GovernedInsideTheUKPage, false).success.value
            .set(CountryGoverningTrustPage, country).success.value
            .set(AdministrationInsideUKPage, false).success.value
            .set(CountryAdministeringTrustPage, country).success.value
            .set(TrusteesBasedInTheUKPage, InternationalAndUKTrustees).success.value
            .set(SettlorsBasedInTheUKPage, false).success.value
            .set(RegisteringTrustFor5APage, false).success.value
            .set(InheritanceTaxActPage, true).success.value
            .set(AgentOtherThanBarristerPage, true).success.value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = Some(country),
            administrationCountry = Some(country),
            residentialStatus = Some(ResidentialStatusType(
              uk = None,
              nonUK = Some(NonUKType(
                sch5atcgga92 = false,
                s218ihta84 = Some(true),
                agentS218IHTA84 = Some(true),
                trusteeStatus = None
              ))
            ))
          )
        }
      }
    }
  }
}
