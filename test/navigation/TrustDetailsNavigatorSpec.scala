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

package navigation

import base.SpecBase
import config.FrontendAppConfig
import controllers.register.trust_details.routes
import generators.Generators
import models.TrusteesBasedInTheUK._
import models.UserAnswers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.register.TrustHaveAUTRPage
import pages.register.trust_details.{AgentOtherThanBarristerPage, _}
import play.api.libs.json.JsBoolean

import java.time.LocalDate

class TrustDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators  {

  private val feAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  private val navigator: TrustDetailsNavigator = injector.instanceOf[TrustDetailsNavigator]

  private val date: LocalDate = LocalDate.parse("1996-02-03")

  "TrustDetailsNavigator" when {

    "in taxable mode" must {

      val baseAnswers: UserAnswers = emptyUserAnswers.copy(isTaxable = true, isExpress = true)

      "TrustName -> WhenTrustSetup" in {
        val answers = baseAnswers.setAtPath(TrustHaveAUTRPage.path, JsBoolean(false)).success.value

        navigator.nextPage(TrustNamePage, fakeDraftId, answers)
          .mustBe(routes.WhenTrustSetupController.onPageLoad(fakeDraftId))
      }

      "WhenTrustSetup -> GovernedInsideTheUK" in {
        val answers = baseAnswers.set(WhenTrustSetupPage, date).success.value

        navigator.nextPage(WhenTrustSetupPage, fakeDraftId, answers)
          .mustBe(routes.GovernedInsideTheUKController.onPageLoad(fakeDraftId))
      }

      "GovernedInsideTheUK -> Yes -> AdministrationInsideUK" in {
        val answers = baseAnswers.set(GovernedInsideTheUKPage, true).success.value

        navigator.nextPage(GovernedInsideTheUKPage, fakeDraftId, answers)
          .mustBe(routes.AdministrationInsideUKController.onPageLoad(fakeDraftId))
      }

      "GovernedInsideTheUK -> No -> CountryGoverningTrust" in {
        val answers = baseAnswers.set(GovernedInsideTheUKPage, false).success.value

        navigator.nextPage(GovernedInsideTheUKPage, fakeDraftId, answers)
          .mustBe(routes.CountryGoverningTrustController.onPageLoad(fakeDraftId))
      }

      "CountryGoverningTrust -> AdministrationInsideUK" in {
        val answers = baseAnswers.set(CountryGoverningTrustPage, "FR").success.value

        navigator.nextPage(CountryGoverningTrustPage, fakeDraftId, answers)
          .mustBe(routes.AdministrationInsideUKController.onPageLoad(fakeDraftId))
      }

      "AdministrationInsideUK -> Yes -> TrustOwnsUkPropertyOrLand" in {
        val answers = baseAnswers.set(AdministrationInsideUKPage, true).success.value

        navigator.nextPage(AdministrationInsideUKPage, fakeDraftId, answers)
          .mustBe(routes.TrustOwnsUkPropertyOrLandController.onPageLoad(fakeDraftId))
      }

      "AdministrationInsideUK -> No -> CountryAdministeringTrust" in {
        val answers = baseAnswers.set(AdministrationInsideUKPage, false).success.value

        navigator.nextPage(AdministrationInsideUKPage, fakeDraftId, answers)
          .mustBe(routes.CountryAdministeringTrustController.onPageLoad(fakeDraftId))
      }

      "CountryAdministeringTrust -> TrustOwnsUkPropertyOrLand" in {
        val answers = baseAnswers.set(CountryAdministeringTrustPage, "FR").success.value

        navigator.nextPage(CountryAdministeringTrustPage, fakeDraftId, answers)
          .mustBe(routes.TrustOwnsUkPropertyOrLandController.onPageLoad(fakeDraftId))
      }

      "TrustOwnsUkPropertyOrLand -> TrustListedOnEeaRegister" in {
        navigator.nextPage(TrustOwnsUkPropertyOrLandPage, fakeDraftId, emptyUserAnswers)
          .mustBe(routes.TrustListedOnEeaRegisterController.onPageLoad(draftId))
      }

      "TrustListedOnEeaRegister -> TrusteesBasedInTheUK" in {
        navigator.nextPage(TrustListedOnEeaRegisterPage, fakeDraftId, emptyUserAnswers)
          .mustBe(routes.TrusteesBasedInTheUKController.onPageLoad(draftId))
      }

      "TrusteesBasedInTheUK -> UKBasedTrustees -> EstablishedUnderScotsLaw" in {
        val answers = baseAnswers.set(TrusteesBasedInTheUKPage, UKBasedTrustees).success.value

        navigator.nextPage(TrusteesBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.EstablishedUnderScotsLawController.onPageLoad(fakeDraftId))
      }

      "TrusteesBasedInTheUK -> NonUkBasedTrustees -> TrustHasBusinessRelationshipInUk" in {
        val answers = baseAnswers.set(TrusteesBasedInTheUKPage, NonUkBasedTrustees).success.value

        navigator.nextPage(TrusteesBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.TrustHasBusinessRelationshipInUkController.onPageLoad(fakeDraftId))
      }

      "TrusteesBasedInTheUK -> InternationalAndUKTrustees -> SettlorsBasedInTheUk" in {
        val answers = baseAnswers.set(TrusteesBasedInTheUKPage, InternationalAndUKTrustees).success.value

        navigator.nextPage(TrusteesBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.SettlorsBasedInTheUKController.onPageLoad(fakeDraftId))
      }

      "SettlorsBasedInTheUK -> Yes -> EstablishedUnderScotsLaw" in {
        val answers = baseAnswers.set(SettlorsBasedInTheUKPage, true).success.value

        navigator.nextPage(SettlorsBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.EstablishedUnderScotsLawController.onPageLoad(fakeDraftId))
      }

      "SettlorsBasedInTheUK -> No -> TrustHasBusinessRelationshipInUk" in {
        val answers = baseAnswers.set(SettlorsBasedInTheUKPage, false).success.value

        navigator.nextPage(SettlorsBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.TrustHasBusinessRelationshipInUkController.onPageLoad(fakeDraftId))
      }

      "TrustHasBusinessRelationshipInUk -> RegisteringTrustFor5A" in {
        navigator.nextPage(TrustHasBusinessRelationshipInUkPage, fakeDraftId, baseAnswers)
          .mustBe(routes.RegisteringTrustFor5AController.onPageLoad(fakeDraftId))
      }

      "Schedule3aExempt toggle is off" when {
        "RegisteringTrustFor5A -> Yes -> CheckDetails" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(false)
          val nav = new TrustDetailsNavigator(feAppConfig)
            val answers = baseAnswers.set(RegisteringTrustFor5APage, true).success.value

            nav.nextPage(RegisteringTrustFor5APage, fakeDraftId, answers)
              .mustBe(routes.CheckDetailsController.onPageLoad(fakeDraftId))
          }
        "InheritanceTaxAct -> No -> CheckDetails" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(false)
          val nav = new TrustDetailsNavigator(feAppConfig)
          val answers = baseAnswers.set(InheritanceTaxActPage, false).success.value

          nav.nextPage(InheritanceTaxActPage, fakeDraftId, answers)
            .mustBe(routes.CheckDetailsController.onPageLoad(draftId))
        }
        "AgentOtherThanBarrister -> CheckDetails" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(false)
          val nav = new TrustDetailsNavigator(feAppConfig)
          nav.nextPage(AgentOtherThanBarristerPage, fakeDraftId, emptyUserAnswers)
            .mustBe(routes.CheckDetailsController.onPageLoad(draftId))
        }
        "TrustResidentOffshore -> No -> CheckDetails" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(false)
          val nav = new TrustDetailsNavigator(feAppConfig)
          val answers = baseAnswers.set(TrustResidentOffshorePage, false).success.value

          nav.nextPage(TrustResidentOffshorePage, fakeDraftId, answers)
            .mustBe(routes.CheckDetailsController.onPageLoad(draftId))
        }
        "TrustPreviouslyResident -> CheckDetails" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(false)
          val nav = new TrustDetailsNavigator(feAppConfig)
          val answers = baseAnswers.set(TrustPreviouslyResidentPage, "FR").success.value

          nav.nextPage(TrustPreviouslyResidentPage, fakeDraftId, answers)
            .mustBe(routes.CheckDetailsController.onPageLoad(draftId))
        }
      }

      "Schedule3aExempt toggle is on" when {
        "RegisteringTrustFor5A -> Yes -> Schedule3aExempt" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(true)
          val nav = new TrustDetailsNavigator(feAppConfig)
          val answers = baseAnswers.set(RegisteringTrustFor5APage, true).success.value

          nav.nextPage(RegisteringTrustFor5APage, fakeDraftId, answers)
            .mustBe(routes.Schedule3aExemptYesNoController.onPageLoad(fakeDraftId))
        }
        "InheritanceTaxAct -> No -> Schedule3aExempt" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(true)
          val nav = new TrustDetailsNavigator(feAppConfig)
          val answers = baseAnswers.set(InheritanceTaxActPage, false).success.value

          nav.nextPage(InheritanceTaxActPage, fakeDraftId, answers)
            .mustBe(routes.Schedule3aExemptYesNoController.onPageLoad(draftId))
        }
        "AgentOtherThanBarrister -> Schedule3aExempt" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(true)
          val nav = new TrustDetailsNavigator(feAppConfig)
          nav.nextPage(AgentOtherThanBarristerPage, fakeDraftId, emptyUserAnswers)
            .mustBe(routes.Schedule3aExemptYesNoController.onPageLoad(draftId))
        }
        "TrustResidentOffshore -> No -> Schedule3aExempt" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(true)
          val nav = new TrustDetailsNavigator(feAppConfig)
          val answers = baseAnswers.set(TrustResidentOffshorePage, false).success.value

          nav.nextPage(TrustResidentOffshorePage, fakeDraftId, answers)
            .mustBe(routes.Schedule3aExemptYesNoController.onPageLoad(draftId))
        }
        "TrustPreviouslyResident -> Schedule3aExempt" in {
          when(feAppConfig.schedule3aExemptEnabled).thenReturn(true)
          val nav = new TrustDetailsNavigator(feAppConfig)
          val answers = baseAnswers.set(TrustPreviouslyResidentPage, "FR").success.value

          nav.nextPage(TrustPreviouslyResidentPage, fakeDraftId, answers)
            .mustBe(routes.Schedule3aExemptYesNoController.onPageLoad(draftId))
        }
      }

      "RegisteringTrustFor5A -> No -> InheritanceTaxAct" in {
        val answers = baseAnswers.set(RegisteringTrustFor5APage, false).success.value

        navigator.nextPage(RegisteringTrustFor5APage, fakeDraftId, answers)
          .mustBe(routes.InheritanceTaxActController.onPageLoad(fakeDraftId))
      }

      "InheritanceTaxAct -> Yes -> AgentOtherThanBarrister" in {
        val answers = baseAnswers.set(InheritanceTaxActPage, true).success.value

        navigator.nextPage(InheritanceTaxActPage, fakeDraftId, answers)
          .mustBe(routes.AgentOtherThanBarristerController.onPageLoad(fakeDraftId))
      }

      "EstablishedUnderScotsLaw -> TrustResidentOffshore" in {
        navigator.nextPage(EstablishedUnderScotsLawPage, fakeDraftId, emptyUserAnswers)
          .mustBe(routes.TrustResidentOffshoreController.onPageLoad(fakeDraftId))
      }

      "TrustResidentOffshore -> Yes -> TrustPreviouslyResident" in {
        val answers = baseAnswers.set(TrustResidentOffshorePage, true).success.value

        navigator.nextPage(TrustResidentOffshorePage, fakeDraftId, answers)
          .mustBe(routes.TrustPreviouslyResidentController.onPageLoad(fakeDraftId))
      }

      "CheckDetails -> RegistrationProgress" in {
        val route = navigator.nextPage(CheckDetailsPage, fakeDraftId, baseAnswers)

        route.url mustBe "http://localhost:9781/trusts-registration/draftId/registration-progress"
      }
    }

    "in none taxable mode" must {

      val baseAnswers: UserAnswers = emptyUserAnswers.copy(isTaxable = false)

      "TrustName -> WhenTrustSetup" in {
        val answers = baseAnswers.setAtPath(TrustHaveAUTRPage.path, JsBoolean(false)).success.value

        navigator.nextPage(TrustNamePage, fakeDraftId, answers)
          .mustBe(routes.WhenTrustSetupController.onPageLoad(fakeDraftId))
      }

      "WhenTrustSetup -> TrustOwnsUkPropertyOrLand" in {
        val answers = baseAnswers.set(WhenTrustSetupPage, date).success.value

        navigator.nextPage(WhenTrustSetupPage, fakeDraftId, answers)
          .mustBe(routes.TrustOwnsUkPropertyOrLandController.onPageLoad(fakeDraftId))
      }

      "TrustOwnsUkPropertyOrLand -> TrustListedOnEeaRegister" in {
        navigator.nextPage(TrustOwnsUkPropertyOrLandPage, fakeDraftId, emptyUserAnswers)
          .mustBe(routes.TrustListedOnEeaRegisterController.onPageLoad(draftId))
      }

      "TrustListedOnEeaRegister -> TrusteesBasedInTheUK" in {
        navigator.nextPage(TrustListedOnEeaRegisterPage, fakeDraftId, emptyUserAnswers)
          .mustBe(routes.TrusteesBasedInTheUKController.onPageLoad(draftId))
      }

      "TrusteesBasedInTheUK -> UKBasedTrustees -> CheckDetails" in {
        val answers = baseAnswers.set(TrusteesBasedInTheUKPage, UKBasedTrustees).success.value

        navigator.nextPage(TrusteesBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.CheckDetailsController.onPageLoad(fakeDraftId))
      }

      "TrusteesBasedInTheUK -> NonUkBasedTrustees -> TrustHasBusinessRelationshipInUk" in {
        val answers = baseAnswers.set(TrusteesBasedInTheUKPage, NonUkBasedTrustees).success.value

        navigator.nextPage(TrusteesBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.TrustHasBusinessRelationshipInUkController.onPageLoad(fakeDraftId))
      }

      "TrusteesBasedInTheUK -> InternationalAndUKTrustees -> SettlorsBasedInTheUk" in {
        val answers = baseAnswers.set(TrusteesBasedInTheUKPage, InternationalAndUKTrustees).success.value

        navigator.nextPage(TrusteesBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.SettlorsBasedInTheUKController.onPageLoad(fakeDraftId))
      }

      "SettlorsBasedInTheUK -> Yes -> CheckDetails" in {
        val answers = baseAnswers.set(SettlorsBasedInTheUKPage, true).success.value

        navigator.nextPage(SettlorsBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.CheckDetailsController.onPageLoad(fakeDraftId))
      }

      "SettlorsBasedInTheUK -> No -> TrustHasBusinessRelationshipInUk" in {
        val answers = baseAnswers.set(SettlorsBasedInTheUKPage, false).success.value

        navigator.nextPage(SettlorsBasedInTheUKPage, fakeDraftId, answers)
          .mustBe(routes.TrustHasBusinessRelationshipInUkController.onPageLoad(fakeDraftId))
      }

      "TrustHasBusinessRelationshipInUk -> CheckDetails" in {
        navigator.nextPage(TrustHasBusinessRelationshipInUkPage, fakeDraftId, baseAnswers)
          .mustBe(routes.CheckDetailsController.onPageLoad(fakeDraftId))
      }


      "CheckDetails -> RegistrationProgress" in {
        val route = navigator.nextPage(CheckDetailsPage, fakeDraftId, baseAnswers)

        route.url mustBe "http://localhost:9781/trusts-registration/draftId/registration-progress"
      }
    }
  }
}
