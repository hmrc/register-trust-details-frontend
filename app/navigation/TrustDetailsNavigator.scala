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
import javax.inject.Inject
import models.ReadableUserAnswers
import models.TrusteesBasedInTheUK._
import pages.Page
import pages.register.trust_details._
import play.api.mvc.Call

class TrustDetailsNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call =
    route(draftId)(page)(userAnswers)

  private def route(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case TrustNamePage => _ => controllers.register.trust_details.routes.WhenTrustSetupController.onPageLoad(draftId)
    case WhenTrustSetupPage => _ => controllers.register.trust_details.routes.GovernedInsideTheUKController.onPageLoad(draftId)
    case GovernedInsideTheUKPage => isTrustGovernedInsideUKRoute(draftId)
    case CountryGoverningTrustPage => _ => controllers.register.trust_details.routes.AdministrationInsideUKController.onPageLoad(draftId)
    case AdministrationInsideUKPage => isTrustGeneralAdministrationRoute(draftId)
    case CountryAdministeringTrustPage => _ => controllers.register.trust_details.routes.TrusteesBasedInTheUKController.onPageLoad(draftId)
    case TrusteesBasedInTheUKPage => isTrusteesBasedInTheUKPage(draftId)

    case SettlorsBasedInTheUKPage => isSettlorsBasedInTheUKPage(draftId)
    case EstablishedUnderScotsLawPage => _ => controllers.register.trust_details.routes.TrustResidentOffshoreController.onPageLoad(draftId)
    case TrustResidentOffshorePage => wasTrustPreviouslyResidentOffshoreRoute(draftId)
    case RegisteringTrustFor5APage => registeringForPurposeOfSchedule5ARoute(draftId)
    case InheritanceTaxActPage => inheritanceTaxRoute(draftId)

    case NonResidentTypePage => _ => controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    case TrustPreviouslyResidentPage => _ => controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    case AgentOtherThanBarristerPage => _ =>  controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    case CheckDetailsPage => _ => completedRoute(draftId, config)
  }

  private def isTrustGovernedInsideUKRoute(draftId: String)(answers: ReadableUserAnswers) = answers.get(GovernedInsideTheUKPage) match {
    case Some(true)  => controllers.register.trust_details.routes.AdministrationInsideUKController.onPageLoad(draftId)
    case Some(false) => controllers.register.trust_details.routes.CountryGoverningTrustController.onPageLoad(draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def isTrustGeneralAdministrationRoute(draftId: String)(answers: ReadableUserAnswers) = answers.get(AdministrationInsideUKPage) match {
    case Some(true)  => controllers.register.trust_details.routes.TrusteesBasedInTheUKController.onPageLoad(draftId)
    case Some(false) => controllers.register.trust_details.routes.CountryAdministeringTrustController.onPageLoad(draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def isTrusteesBasedInTheUKPage(draftId: String)(answers: ReadableUserAnswers) = answers.get(TrusteesBasedInTheUKPage) match {
    case Some(UKBasedTrustees)   => controllers.register.trust_details.routes.EstablishedUnderScotsLawController.onPageLoad(draftId)
    case Some(NonUkBasedTrustees)  => controllers.register.trust_details.routes.RegisteringTrustFor5AController.onPageLoad(draftId)
    case Some(InternationalAndUKTrustees)  => controllers.register.trust_details.routes.SettlorsBasedInTheUKController.onPageLoad(draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def isSettlorsBasedInTheUKPage(draftId: String)(answers: ReadableUserAnswers) = answers.get(SettlorsBasedInTheUKPage) match {
    case Some(true)   => controllers.register.trust_details.routes.EstablishedUnderScotsLawController.onPageLoad(draftId)
    case Some(false)  => controllers.register.trust_details.routes.RegisteringTrustFor5AController.onPageLoad(draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def wasTrustPreviouslyResidentOffshoreRoute(draftId: String)(answers: ReadableUserAnswers) = answers.get(TrustResidentOffshorePage) match {
    case Some(true)   => controllers.register.trust_details.routes.TrustPreviouslyResidentController.onPageLoad(draftId)
    case Some(false)  => controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def registeringForPurposeOfSchedule5ARoute(draftId: String)(answers: ReadableUserAnswers) = answers.get(RegisteringTrustFor5APage) match {
    case Some(true)   => controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    case Some(false)  => controllers.register.trust_details.routes.InheritanceTaxActController.onPageLoad(draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def inheritanceTaxRoute(draftId: String)(answers: ReadableUserAnswers) = answers.get(InheritanceTaxActPage) match {
    case Some(true)   => controllers.register.trust_details.routes.AgentOtherThanBarristerController.onPageLoad(draftId)
    case Some(false)  => controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def completedRoute(draftId: String, config: FrontendAppConfig): Call = {
    Call("GET", config.registrationProgressUrl(draftId))
  }

}

