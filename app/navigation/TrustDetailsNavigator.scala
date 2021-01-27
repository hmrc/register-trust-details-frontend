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

package navigation

import config.FrontendAppConfig
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
      conditionalNavigation(draftId)
  }

  private def simpleNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case TrustNamePage => _ => controllers.register.trust_details.routes.WhenTrustSetupController.onPageLoad(draftId)
    case WhenTrustSetupPage => _ => controllers.register.trust_details.routes.GovernedInsideTheUKController.onPageLoad(draftId)
    case CountryGoverningTrustPage => _ => controllers.register.trust_details.routes.AdministrationInsideUKController.onPageLoad(draftId)
    case CountryAdministeringTrustPage => _ => controllers.register.trust_details.routes.TrusteesBasedInTheUKController.onPageLoad(draftId)
    case EstablishedUnderScotsLawPage => _ => controllers.register.trust_details.routes.TrustResidentOffshoreController.onPageLoad(draftId)
    case TrustPreviouslyResidentPage => _ => controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    case AgentOtherThanBarristerPage => _ => controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    case CheckDetailsPage => _ => Call(GET, config.registrationProgressUrl(draftId))
  }

  private def yesNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case GovernedInsideTheUKPage => ua => yesNoNav(
      ua = ua,
      fromPage = GovernedInsideTheUKPage,
      yesCall = controllers.register.trust_details.routes.AdministrationInsideUKController.onPageLoad(draftId),
      noCall = controllers.register.trust_details.routes.CountryGoverningTrustController.onPageLoad(draftId)
    )
    case AdministrationInsideUKPage => ua => yesNoNav(
      ua = ua,
      fromPage = AdministrationInsideUKPage,
      yesCall = controllers.register.trust_details.routes.TrusteesBasedInTheUKController.onPageLoad(draftId),
      noCall = controllers.register.trust_details.routes.CountryAdministeringTrustController.onPageLoad(draftId)
    )
    case SettlorsBasedInTheUKPage => ua => yesNoNav(
      ua = ua,
      fromPage = SettlorsBasedInTheUKPage,
      yesCall = controllers.register.trust_details.routes.EstablishedUnderScotsLawController.onPageLoad(draftId),
      noCall = controllers.register.trust_details.routes.RegisteringTrustFor5AController.onPageLoad(draftId)
    )
    case TrustResidentOffshorePage => ua => yesNoNav(
      ua = ua,
      fromPage = TrustResidentOffshorePage,
      yesCall = controllers.register.trust_details.routes.TrustPreviouslyResidentController.onPageLoad(draftId),
      noCall = controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    )
    case RegisteringTrustFor5APage => ua => yesNoNav(
      ua = ua,
      fromPage = RegisteringTrustFor5APage,
      yesCall = controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId),
      noCall = controllers.register.trust_details.routes.InheritanceTaxActController.onPageLoad(draftId)
    )
    case InheritanceTaxActPage => ua => yesNoNav(
      ua = ua,
      fromPage = InheritanceTaxActPage,
      yesCall = controllers.register.trust_details.routes.AgentOtherThanBarristerController.onPageLoad(draftId),
      noCall = controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    )
  }

  private def conditionalNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case TrusteesBasedInTheUKPage => ua => ua.get(TrusteesBasedInTheUKPage) match {
      case Some(UKBasedTrustees) => controllers.register.trust_details.routes.EstablishedUnderScotsLawController.onPageLoad(draftId)
      case Some(NonUkBasedTrustees) => controllers.register.trust_details.routes.RegisteringTrustFor5AController.onPageLoad(draftId)
      case Some(InternationalAndUKTrustees) => controllers.register.trust_details.routes.SettlorsBasedInTheUKController.onPageLoad(draftId)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

}
