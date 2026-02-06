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

package mapping

import base.SpecBase
import models.TrusteesBasedInTheUK._
import models.{NonUKType, ResidentialStatusType, TrustDetailsType, UkType, UserAnswers}
import pages.register.trust_details._
import utils.Constants.GB

import java.time.LocalDate

class TrustDetailsMapperSpec extends SpecBase {

  private val mapper: TrustDetailsMapper = new TrustDetailsMapper

  private val name: String    = "Name"
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val country: String = "FR"

  "TrustDetailsMapper" must {

    "build trust details from user answers" when {

      "taxable not express" when {

        val baseAnswers: UserAnswers = emptyUserAnswers
          .copy(isTaxable = true, isExpress = false)
          .set(TrustNamePage, name)
          .success
          .value
          .set(WhenTrustSetupPage, date)
          .success
          .value

        "UK governed, UK administered, owns UK property/land, not recorded on another register, all trustees UK based and never based offshore" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(GovernedInsideTheUKPage, true)
            .success
            .value
            .set(AdministrationInsideUKPage, true)
            .success
            .value
            .set(TrustOwnsUkPropertyOrLandPage, true)
            .success
            .value
            .set(TrustListedOnEeaRegisterPage, false)
            .success
            .value
            .set(TrusteesBasedInTheUKPage, UKBasedTrustees)
            .success
            .value
            .set(EstablishedUnderScotsLawPage, true)
            .success
            .value
            .set(TrustResidentOffshorePage, false)
            .success
            .value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = None,
            administrationCountry = Some(GB),
            residentialStatus = Some(
              ResidentialStatusType(
                uk = Some(
                  UkType(
                    scottishLaw = true,
                    preOffShore = None
                  )
                ),
                nonUK = None
              )
            ),
            trustUKProperty = Some(true),
            trustRecorded = Some(false),
            trustUKRelation = None,
            trustUKResident = Some(true)
          )
        }

        "UK governed, UK administered, owns UK property/land, not recorded on another register, some trustees UK based, " +
          "some/all settlors UK based and previously based offshore" in {

            val userAnswers: UserAnswers = baseAnswers
              .set(GovernedInsideTheUKPage, true)
              .success
              .value
              .set(AdministrationInsideUKPage, true)
              .success
              .value
              .set(TrustOwnsUkPropertyOrLandPage, true)
              .success
              .value
              .set(TrustListedOnEeaRegisterPage, false)
              .success
              .value
              .set(TrusteesBasedInTheUKPage, InternationalAndUKTrustees)
              .success
              .value
              .set(SettlorsBasedInTheUKPage, true)
              .success
              .value
              .set(EstablishedUnderScotsLawPage, false)
              .success
              .value
              .set(TrustResidentOffshorePage, true)
              .success
              .value
              .set(TrustPreviouslyResidentPage, country)
              .success
              .value

            val result = mapper.build(userAnswers).get

            result mustBe TrustDetailsType(
              startDate = date,
              lawCountry = None,
              administrationCountry = Some(GB),
              residentialStatus = Some(
                ResidentialStatusType(
                  uk = Some(
                    UkType(
                      scottishLaw = false,
                      preOffShore = Some(country)
                    )
                  ),
                  nonUK = None
                )
              ),
              trustUKProperty = Some(true),
              trustRecorded = Some(false),
              trustUKRelation = None,
              trustUKResident = Some(true)
            )
          }

        "non-UK governed, non-UK administered, doesn't own UK property/land, recorded on another register, " +
          "no trustees UK based, trust has UK business relationship and settlor benefits from assets" in {

            val userAnswers: UserAnswers = baseAnswers
              .set(GovernedInsideTheUKPage, false)
              .success
              .value
              .set(CountryGoverningTrustPage, country)
              .success
              .value
              .set(AdministrationInsideUKPage, false)
              .success
              .value
              .set(CountryAdministeringTrustPage, country)
              .success
              .value
              .set(TrustOwnsUkPropertyOrLandPage, false)
              .success
              .value
              .set(TrustListedOnEeaRegisterPage, true)
              .success
              .value
              .set(TrusteesBasedInTheUKPage, NonUkBasedTrustees)
              .success
              .value
              .set(TrustHasBusinessRelationshipInUkPage, true)
              .success
              .value
              .set(RegisteringTrustFor5APage, true)
              .success
              .value

            val result = mapper.build(userAnswers).get

            result mustBe TrustDetailsType(
              startDate = date,
              lawCountry = Some(country),
              administrationCountry = Some(country),
              residentialStatus = Some(
                ResidentialStatusType(
                  uk = None,
                  nonUK = Some(
                    NonUKType(
                      sch5atcgga92 = true,
                      s218ihta84 = None,
                      agentS218IHTA84 = None,
                      trusteeStatus = None
                    )
                  )
                )
              ),
              trustUKProperty = Some(false),
              trustRecorded = Some(true),
              trustUKRelation = Some(true),
              trustUKResident = Some(false)
            )
          }

        "non-UK governed, non-UK administered, doesn't own UK property/land, recorded on another register, " +
          "no trustees UK based, trust doesn't have UK business relationship and not registering for purpose of section 218" in {

            val userAnswers: UserAnswers = baseAnswers
              .set(GovernedInsideTheUKPage, false)
              .success
              .value
              .set(CountryGoverningTrustPage, country)
              .success
              .value
              .set(AdministrationInsideUKPage, false)
              .success
              .value
              .set(CountryAdministeringTrustPage, country)
              .success
              .value
              .set(TrustOwnsUkPropertyOrLandPage, false)
              .success
              .value
              .set(TrustListedOnEeaRegisterPage, true)
              .success
              .value
              .set(TrusteesBasedInTheUKPage, NonUkBasedTrustees)
              .success
              .value
              .set(TrustHasBusinessRelationshipInUkPage, false)
              .success
              .value
              .set(RegisteringTrustFor5APage, false)
              .success
              .value
              .set(InheritanceTaxActPage, false)
              .success
              .value

            val result = mapper.build(userAnswers).get

            result mustBe TrustDetailsType(
              startDate = date,
              lawCountry = Some(country),
              administrationCountry = Some(country),
              residentialStatus = Some(
                ResidentialStatusType(
                  uk = None,
                  nonUK = Some(
                    NonUKType(
                      sch5atcgga92 = false,
                      s218ihta84 = Some(false),
                      agentS218IHTA84 = None,
                      trusteeStatus = None
                    )
                  )
                )
              ),
              trustUKProperty = Some(false),
              trustRecorded = Some(true),
              trustUKRelation = Some(false),
              trustUKResident = Some(false)
            )
          }

        "non-UK governed, non-UK administered, doesn't own UK property/land, recorded on another register," +
          " some trustees UK based, no settlors UK based, doesn't have UK Business relationship and registering for purpose of section 218" in {

            val userAnswers: UserAnswers = baseAnswers
              .set(GovernedInsideTheUKPage, false)
              .success
              .value
              .set(CountryGoverningTrustPage, country)
              .success
              .value
              .set(AdministrationInsideUKPage, false)
              .success
              .value
              .set(CountryAdministeringTrustPage, country)
              .success
              .value
              .set(TrustOwnsUkPropertyOrLandPage, false)
              .success
              .value
              .set(TrustListedOnEeaRegisterPage, true)
              .success
              .value
              .set(TrusteesBasedInTheUKPage, InternationalAndUKTrustees)
              .success
              .value
              .set(SettlorsBasedInTheUKPage, false)
              .success
              .value
              .set(TrustHasBusinessRelationshipInUkPage, false)
              .success
              .value
              .set(RegisteringTrustFor5APage, false)
              .success
              .value
              .set(InheritanceTaxActPage, true)
              .success
              .value
              .set(AgentOtherThanBarristerPage, true)
              .success
              .value

            val result = mapper.build(userAnswers).get

            result mustBe TrustDetailsType(
              startDate = date,
              lawCountry = Some(country),
              administrationCountry = Some(country),
              residentialStatus = Some(
                ResidentialStatusType(
                  uk = None,
                  nonUK = Some(
                    NonUKType(
                      sch5atcgga92 = false,
                      s218ihta84 = Some(true),
                      agentS218IHTA84 = Some(true),
                      trusteeStatus = None
                    )
                  )
                )
              ),
              trustUKProperty = Some(false),
              trustRecorded = Some(true),
              trustUKRelation = Some(false),
              trustUKResident = Some(false)
            )
          }
      }

      "taxable and express" when {

        val baseAnswers: UserAnswers = emptyUserAnswers
          .copy(isTaxable = true, isExpress = true)
          .set(TrustNamePage, name)
          .success
          .value
          .set(WhenTrustSetupPage, date)
          .success
          .value

        "UK governed, UK administered, owns UK property/land, not recorded on another register, all trustees UK based and never based offshore, Schedule 3a Exempt" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(GovernedInsideTheUKPage, true)
            .success
            .value
            .set(AdministrationInsideUKPage, true)
            .success
            .value
            .set(TrustOwnsUkPropertyOrLandPage, true)
            .success
            .value
            .set(TrustListedOnEeaRegisterPage, false)
            .success
            .value
            .set(TrusteesBasedInTheUKPage, UKBasedTrustees)
            .success
            .value
            .set(EstablishedUnderScotsLawPage, true)
            .success
            .value
            .set(TrustResidentOffshorePage, false)
            .success
            .value
            .set(Schedule3aExemptYesNoPage, true)
            .success
            .value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = None,
            administrationCountry = Some(GB),
            residentialStatus = Some(
              ResidentialStatusType(
                uk = Some(
                  UkType(
                    scottishLaw = true,
                    preOffShore = None
                  )
                ),
                nonUK = None
              )
            ),
            trustUKProperty = Some(true),
            trustRecorded = Some(false),
            trustUKRelation = None,
            trustUKResident = Some(true),
            schedule3aExempt = Some(true)
          )
        }

        "UK governed, UK administered, owns UK property/land, not recorded on another register, some trustees UK based, " +
          "some/all settlors UK based and previously based offshore, not Schedule 3a Exempt" in {

            val userAnswers: UserAnswers = baseAnswers
              .set(GovernedInsideTheUKPage, true)
              .success
              .value
              .set(AdministrationInsideUKPage, true)
              .success
              .value
              .set(TrustOwnsUkPropertyOrLandPage, true)
              .success
              .value
              .set(TrustListedOnEeaRegisterPage, false)
              .success
              .value
              .set(TrusteesBasedInTheUKPage, InternationalAndUKTrustees)
              .success
              .value
              .set(SettlorsBasedInTheUKPage, true)
              .success
              .value
              .set(EstablishedUnderScotsLawPage, false)
              .success
              .value
              .set(TrustResidentOffshorePage, true)
              .success
              .value
              .set(TrustPreviouslyResidentPage, country)
              .success
              .value
              .set(Schedule3aExemptYesNoPage, false)
              .success
              .value

            val result = mapper.build(userAnswers).get

            result mustBe TrustDetailsType(
              startDate = date,
              lawCountry = None,
              administrationCountry = Some(GB),
              residentialStatus = Some(
                ResidentialStatusType(
                  uk = Some(
                    UkType(
                      scottishLaw = false,
                      preOffShore = Some(country)
                    )
                  ),
                  nonUK = None
                )
              ),
              trustUKProperty = Some(true),
              trustRecorded = Some(false),
              trustUKRelation = None,
              trustUKResident = Some(true),
              schedule3aExempt = Some(false)
            )
          }
      }

      "non-taxable" when {

        val baseAnswers: UserAnswers = emptyUserAnswers
          .copy(isTaxable = false)
          .set(TrustNamePage, name)
          .success
          .value
          .set(WhenTrustSetupPage, date)
          .success
          .value

        "owns UK property/land, not recorded on another register, all trustees UK based" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(TrustOwnsUkPropertyOrLandPage, true)
            .success
            .value
            .set(TrustListedOnEeaRegisterPage, false)
            .success
            .value
            .set(TrusteesBasedInTheUKPage, UKBasedTrustees)
            .success
            .value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            trustUKProperty = Some(true),
            trustRecorded = Some(false),
            trustUKRelation = None,
            trustUKResident = Some(true)
          )
        }

        "owns UK property/land, not recorded on another register, some trustees UK based, some/all settlors UK based" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(TrustOwnsUkPropertyOrLandPage, true)
            .success
            .value
            .set(TrustListedOnEeaRegisterPage, false)
            .success
            .value
            .set(TrusteesBasedInTheUKPage, InternationalAndUKTrustees)
            .success
            .value
            .set(SettlorsBasedInTheUKPage, true)
            .success
            .value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            trustUKProperty = Some(true),
            trustRecorded = Some(false),
            trustUKRelation = None,
            trustUKResident = Some(true)
          )
        }

        "doesn't own UK property/land, recorded on another register, no trustees UK based, trust has UK business relationship" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(TrustOwnsUkPropertyOrLandPage, false)
            .success
            .value
            .set(TrustListedOnEeaRegisterPage, true)
            .success
            .value
            .set(TrusteesBasedInTheUKPage, NonUkBasedTrustees)
            .success
            .value
            .set(TrustHasBusinessRelationshipInUkPage, true)
            .success
            .value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            trustUKProperty = Some(false),
            trustRecorded = Some(true),
            trustUKRelation = Some(true),
            trustUKResident = Some(false)
          )
        }

        "doesn't own UK property/land, recorded on another register, some trustees UK based, no settlors UK based, doesn't have UK Business relationship" in {

          val userAnswers: UserAnswers = baseAnswers
            .set(TrustOwnsUkPropertyOrLandPage, false)
            .success
            .value
            .set(TrustListedOnEeaRegisterPage, true)
            .success
            .value
            .set(TrusteesBasedInTheUKPage, InternationalAndUKTrustees)
            .success
            .value
            .set(SettlorsBasedInTheUKPage, false)
            .success
            .value
            .set(TrustHasBusinessRelationshipInUkPage, false)
            .success
            .value

          val result = mapper.build(userAnswers).get

          result mustBe TrustDetailsType(
            startDate = date,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            trustUKProperty = Some(false),
            trustRecorded = Some(true),
            trustUKRelation = Some(false),
            trustUKResident = Some(false)
          )
        }
      }
    }
  }

}
