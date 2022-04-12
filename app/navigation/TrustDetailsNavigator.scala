/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.register.trust_details.routes._
import controllers.routes._
import models.ReadableUserAnswers
import models.TrusteesBasedInTheUK._
import pages.Page
import pages.register.trust_details._
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs.GET

import javax.inject.Inject

class TrustDetailsNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call = {
    route(draftId)(page)(userAnswers)
  }

  private def route(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    simpleNavigation(draftId) orElse
      yesNavigation(draftId) orElse
      otherNavigation(draftId)
  }

  private def simpleNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case TrustNamePage => _ => WhenTrustSetupController.onPageLoad(draftId)
    case WhenTrustSetupPage => ua => navigateAwayFromTrustSetupPage(ua, draftId)
    case CountryGoverningTrustPage => _ => AdministrationInsideUKController.onPageLoad(draftId)
    case CountryAdministeringTrustPage => ua => TrustOwnsUkPropertyOrLandController.onPageLoad(draftId)
    case TrustOwnsUkPropertyOrLandPage => _ => TrustListedOnEeaRegisterController.onPageLoad(draftId)
    case TrustListedOnEeaRegisterPage => _ => TrusteesBasedInTheUKController.onPageLoad(draftId)
    case TrustHasBusinessRelationshipInUkPage => ua => navigateAwayFromRelationshipInTheUk(ua, draftId)
    case EstablishedUnderScotsLawPage => _ => TrustResidentOffshoreController.onPageLoad(draftId)
    case TrustPreviouslyResidentPage | AgentOtherThanBarristerPage => _ => CheckDetailsController.onPageLoad(draftId)
    case CheckDetailsPage => _ => Call(GET, config.registrationProgressUrl(draftId))
  }

  private def yesNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case GovernedInsideTheUKPage => ua =>
      yesNoNav(
        ua = ua,
        fromPage = GovernedInsideTheUKPage,
        yesCall = AdministrationInsideUKController.onPageLoad(draftId),
        noCall = CountryGoverningTrustController.onPageLoad(draftId)
      )
    case AdministrationInsideUKPage => ua =>
      yesNoNav(
        ua = ua,
        fromPage = AdministrationInsideUKPage,
        yesCall = TrustOwnsUkPropertyOrLandController.onPageLoad(draftId),
        noCall = CountryAdministeringTrustController.onPageLoad(draftId)
      )
    case SettlorsBasedInTheUKPage => ua =>
      yesNoNav(
        ua = ua,
        fromPage = SettlorsBasedInTheUKPage,
        yesCall = navigateAwayFromUkSelection(ua, draftId),
        noCall = TrustHasBusinessRelationshipInUkController.onPageLoad(draftId)
      )
    case TrustResidentOffshorePage => ua =>
      yesNoNav(
        ua = ua,
        fromPage = TrustResidentOffshorePage,
        yesCall = TrustPreviouslyResidentController.onPageLoad(draftId),
        noCall = CheckDetailsController.onPageLoad(draftId)
      )
    case RegisteringTrustFor5APage => ua =>
      yesNoNav(
        ua = ua,
        fromPage = RegisteringTrustFor5APage,
        yesCall = CheckDetailsController.onPageLoad(draftId),
        noCall = InheritanceTaxActController.onPageLoad(draftId)
      )
    case InheritanceTaxActPage => ua =>
      yesNoNav(
        ua = ua,
        fromPage = InheritanceTaxActPage,
        yesCall = AgentOtherThanBarristerController.onPageLoad(draftId),
        noCall = CheckDetailsController.onPageLoad(draftId)
      )
  }

  private def otherNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case TrusteesBasedInTheUKPage => ua =>
      ua.get(TrusteesBasedInTheUKPage) match {
        case Some(UKBasedTrustees) =>
          navigateAwayFromUkSelection(ua, draftId)
        case Some(NonUkBasedTrustees) =>
          TrustHasBusinessRelationshipInUkController.onPageLoad(draftId)
        case Some(InternationalAndUKTrustees) =>
          SettlorsBasedInTheUKController.onPageLoad(draftId)
        case _ =>
          SessionExpiredController.onPageLoad()
      }
  }

  private def navigateAwayFromUkSelection(ua: ReadableUserAnswers, draftId: String): Call = {
    if (ua.isTaxable) {
      EstablishedUnderScotsLawController.onPageLoad(draftId)
    } else {
      CheckDetailsController.onPageLoad(draftId)
    }
  }

  private def navigateAwayFromTrustSetupPage(ua: ReadableUserAnswers, draftId: String): Call = {
    if (ua.isTaxable) {
      GovernedInsideTheUKController.onPageLoad(draftId)
    } else {
      TrustOwnsUkPropertyOrLandController.onPageLoad(draftId)
    }
  }

  private def navigateAwayFromRelationshipInTheUk(ua: ReadableUserAnswers, draftId: String): Call = {
    if (ua.isTaxable) {
      RegisteringTrustFor5AController.onPageLoad(draftId)
    } else {
      CheckDetailsController.onPageLoad(draftId)
    }
  }

}
