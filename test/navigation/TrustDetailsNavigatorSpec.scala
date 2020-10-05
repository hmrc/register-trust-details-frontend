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

package navigation

import java.time.LocalDate

import base.SpecBase
import controllers.register.trust_details.routes
import generators.Generators
import models.TrusteesBasedInTheUK._
import models.{NonResidentType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.register.TrustHaveAUTRPage
import pages.register.trust_details.{AgentOtherThanBarristerPage, _}

class TrustDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators  {

  lazy val navigator = injector.instanceOf[TrustDetailsNavigator]

    "go to TrustSetup from TrustName when user does not have a UTR" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(TrustHaveAUTRPage, false).success.value

          navigator.nextPage(TrustNamePage, fakeDraftId, answers)
            .mustBe(routes.WhenTrustSetupController.onPageLoad(fakeDraftId))
      }
    }

    "go to Is Trust Governed By Laws Inside The UK from Trust Setup Page" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(WhenTrustSetupPage, value = LocalDate.of(2010, 10, 10)).success.value

          navigator.nextPage(WhenTrustSetupPage, fakeDraftId, answers)
            .mustBe(routes.GovernedInsideTheUKController.onPageLoad(fakeDraftId))
      }
    }

    "go to is Trust Administration Done Inside UK from Is Trust Governed By Laws Inside The UK when the user answers Yes" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(GovernedInsideTheUKPage, value = true).success.value

          navigator.nextPage(GovernedInsideTheUKPage, fakeDraftId, answers)
            .mustBe(routes.AdministrationInsideUKController.onPageLoad(fakeDraftId))
      }
    }

    "go to What is the country governing the Trust from Is Trust Governed By Laws Inside The UK when the user answers No" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(GovernedInsideTheUKPage, value = false).success.value

          navigator.nextPage(GovernedInsideTheUKPage, fakeDraftId, answers)
            .mustBe(routes.CountryGoverningTrustController.onPageLoad(fakeDraftId))
      }
    }

    "go to Is Trust Administration Done Inside UK from What is Country Governing The Trust" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(CountryGoverningTrustPage, value = "France").success.value

          navigator.nextPage(CountryGoverningTrustPage, fakeDraftId, answers)
            .mustBe(routes.AdministrationInsideUKController.onPageLoad(fakeDraftId))
      }
    }

    "go to What Is Country Administering from Is Trust Administration Done Inside UK when user answers No" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(AdministrationInsideUKPage, value = false).success.value

          navigator.nextPage(AdministrationInsideUKPage, fakeDraftId, answers)
            .mustBe(routes.CountryAdministeringTrustController.onPageLoad(fakeDraftId))
      }
    }

    "go to Is Trust Resident from Is Trust Administration Done Inside UK when user answers Yes" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(AdministrationInsideUKPage, value = true).success.value

          navigator.nextPage(AdministrationInsideUKPage, fakeDraftId, answers)
            .mustBe(routes.TrusteesBasedInTheUKController.onPageLoad(fakeDraftId))
      }
    }

    "go to Is Trust Resident from What Is Country Administering" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(CountryAdministeringTrustPage, value = "France").success.value

          navigator.nextPage(CountryAdministeringTrustPage, fakeDraftId, answers)
            .mustBe(routes.TrusteesBasedInTheUKController.onPageLoad(fakeDraftId))
      }
    }

    "go to Registering for Purpose of 5A Schedule from Trust Resident in UK when user answers No" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(TrusteesBasedInTheUKPage, value = NonUkBasedTrustees).success.value

          navigator.nextPage(TrusteesBasedInTheUKPage, fakeDraftId, answers)
            .mustBe(routes.RegisteringTrustFor5AController.onPageLoad(fakeDraftId))
      }
    }

    "go to Inheritance Tax from Registering for Purpose of Schedule 5A when user answers No" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(RegisteringTrustFor5APage, value = false).success.value

          navigator.nextPage(RegisteringTrustFor5APage, fakeDraftId, answers)
            .mustBe(routes.InheritanceTaxActController.onPageLoad(fakeDraftId))
      }
    }

    "go to Check Trust Details Answers from Inheritance Tax when user answers No" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(InheritanceTaxActPage, value = false).success.value

          navigator.nextPage(InheritanceTaxActPage, fakeDraftId, answers)
            .mustBe(controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId))
      }
    }

    "go to Agent Other Than Barrister from Inheritance Tax when user answers Yes" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(InheritanceTaxActPage, value = true).success.value

          navigator.nextPage(InheritanceTaxActPage, fakeDraftId, answers)
            .mustBe(controllers.register.trust_details.routes.AgentOtherThanBarristerController.onPageLoad(fakeDraftId))
      }
    }

    "go to Check Trust Details Answers from Agent Other Than Barrister" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(AgentOtherThanBarristerPage, value = true).success.value

          navigator.nextPage(AgentOtherThanBarristerPage, fakeDraftId, answers)
            .mustBe(controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId))
      }
    }

    "go to Check Trust Details Answers from What is The Non Resident Type" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(NonResidentTypePage, value = NonResidentType.Domiciled).success.value

          navigator.nextPage(NonResidentTypePage, fakeDraftId, answers)
            .mustBe(controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId))
      }
    }

    "go to What Is Non Resident Type from Registering for Purpose of Schedule 5A when user answers Yes" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(RegisteringTrustFor5APage, value = true).success.value

          navigator.nextPage(RegisteringTrustFor5APage, fakeDraftId, answers)
            .mustBe(routes.NonResidentTypeController.onPageLoad(fakeDraftId))
      }
    }

    "go to Trust Established Under Scots Law from Trust Resident in UK when user answers Yes" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(TrusteesBasedInTheUKPage, value = UKBasedTrustees).success.value

          navigator.nextPage(TrusteesBasedInTheUKPage, fakeDraftId, answers)
            .mustBe(routes.EstablishedUnderScotsLawController.onPageLoad(fakeDraftId))
      }
    }

    "go to Was Trust Resident Previously Offshore from Trust Established Under Scots Law" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(EstablishedUnderScotsLawPage, value = false).success.value

          navigator.nextPage(EstablishedUnderScotsLawPage, fakeDraftId, answers)
            .mustBe(routes.TrustResidentOffshoreController.onPageLoad(fakeDraftId))
      }
    }

    "go to Where Was The Trust Previously Resident from Was Trust Resident Offshore when user answers Yes" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(TrustResidentOffshorePage, value = true).success.value

          navigator.nextPage(TrustResidentOffshorePage, fakeDraftId, answers)
            .mustBe(routes.TrustPreviouslyResidentController.onPageLoad(fakeDraftId))
      }
    }

    "go to Check Trust Details Answers from Was Trust Resident Offshore when user answers No" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(TrustResidentOffshorePage, value = false).success.value

          navigator.nextPage(TrustResidentOffshorePage, fakeDraftId, answers)
            .mustBe(controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId))
      }
    }

    "go to Check Trust Details Answers from Where Was The Trust Previously Resident" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(TrustPreviouslyResidentPage, value = "France").success.value

          navigator.nextPage(TrustPreviouslyResidentPage, fakeDraftId, answers)
            .mustBe(controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId))
      }
    }

    "go to RegistrationProgress from Check Trust Details Answers Page" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val route = navigator.nextPage(CheckDetailsPage, fakeDraftId, userAnswers)

          route.url mustBe "http://localhost:9781/trusts-registration/draftId/registration-progress"
      }

    }

}
