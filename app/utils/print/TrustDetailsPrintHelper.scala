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

package utils.print

import com.google.inject.Inject
import controllers.register.trust_details.routes
import models.ReadableUserAnswers
import pages.register.trust_details._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

class TrustDetailsPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def printSection(userAnswers: ReadableUserAnswers, draftId: String)(implicit messages: Messages): AnswerSection = {
    AnswerSection(
      rows = answers(userAnswers, draftId),
      sectionKey = Some("answerPage.section.trustDetails.heading")
    )
  }

  def checkDetailsSection(userAnswers: ReadableUserAnswers, draftId: String)(implicit messages: Messages): AnswerSection = {
    AnswerSection(
      rows = answers(userAnswers, draftId)
    )
  }

  private def answers(userAnswers: ReadableUserAnswers, draftId: String)
             (implicit messages: Messages): Seq[AnswerRow] = {

    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers)

    Seq(
      bound.stringQuestion(TrustNamePage, "trustName", routes.TrustNameController.onPageLoad(draftId).url),
      bound.dateQuestion(WhenTrustSetupPage, "whenTrustSetupDate", routes.WhenTrustSetupController.onPageLoad(draftId).url),
      bound.yesNoQuestion(GovernedInsideTheUKPage, "governedInsideTheUKYesNo", routes.GovernedInsideTheUKController.onPageLoad(draftId).url),
      bound.countryQuestion(CountryGoverningTrustPage, "countryGoverningTrust", routes.CountryGoverningTrustController.onPageLoad(draftId).url),
      bound.yesNoQuestion(AdministrationInsideUKPage, "administrationInsideUKYesNo", routes.AdministrationInsideUKController.onPageLoad(draftId).url),
      bound.countryQuestion(CountryAdministeringTrustPage, "countryAdministeringTrust", routes.CountryAdministeringTrustController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustOwnsUkPropertyOrLandPage, "trustOwnsUkPropertyOrLandYesNo", routes.TrustOwnsUkPropertyOrLandController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustListedOnEeaRegisterPage, "trustListedOnEeaRegisterYesNo", routes.TrustListedOnEeaRegisterController.onPageLoad(draftId).url),
      bound.enumQuestion(TrusteesBasedInTheUKPage, "trusteesBasedInTheUK", routes.TrusteesBasedInTheUKController.onPageLoad(draftId).url),
      bound.yesNoQuestion(SettlorsBasedInTheUKPage, "settlorsBasedInTheUKYesNo", routes.SettlorsBasedInTheUKController.onPageLoad(draftId).url),
      bound.yesNoQuestion(EstablishedUnderScotsLawPage, "establishedUnderScotsLawYesNo", routes.EstablishedUnderScotsLawController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustResidentOffshorePage, "trustResidentOffshoreYesNo", routes.TrustResidentOffshoreController.onPageLoad(draftId).url),
      bound.countryQuestion(TrustPreviouslyResidentPage, "trustPreviouslyResident", routes.TrustPreviouslyResidentController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustHasBusinessRelationshipInUkPage, "trustHasBusinessRelationshipInUkYesNo", routes.TrustHasBusinessRelationshipInUkController.onPageLoad(draftId).url),
      bound.yesNoQuestion(RegisteringTrustFor5APage, "registeringTrustFor5AYesNo", routes.RegisteringTrustFor5AController.onPageLoad(draftId).url),
      bound.yesNoQuestion(InheritanceTaxActPage, "inheritanceTaxActYesNo", routes.InheritanceTaxActController.onPageLoad(draftId).url),
      bound.yesNoQuestion(AgentOtherThanBarristerPage, "agentOtherThanBarristerYesNo", routes.AgentOtherThanBarristerController.onPageLoad(draftId).url)
    ).flatten

  }

}
