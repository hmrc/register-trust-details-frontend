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
import utils.answers.CheckAnswersFormatters
import viewmodels.{AnswerRow, AnswerSection}

class TrustDetailsPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def printSection(userAnswers: ReadableUserAnswers, draftId: String)(implicit messages: Messages): AnswerSection = {
    AnswerSection(
      Some(messages("answerPage.section.trustDetails.subheading")),
      answers(userAnswers, draftId)
    )
  }

  def checkDetailsSection(userAnswers: ReadableUserAnswers, draftId: String)(implicit messages: Messages): AnswerSection = {
    AnswerSection(
      None,
      answers(userAnswers, draftId)
    )
  }

  def answers(userAnswers: ReadableUserAnswers, draftId: String)
             (implicit messages: Messages): Seq[AnswerRow] = {

    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers)

    Seq(
      bound.stringQuestion(TrustNamePage, "trustName", routes.TrustNameController.onPageLoad(draftId).url),
      bound.dateQuestion(WhenTrustSetupPage, "whenTrustSetup", routes.WhenTrustSetupController.onPageLoad(draftId).url),
      bound.yesNoQuestion(GovernedInsideTheUKPage, "governedInsideTheUK", routes.GovernedInsideTheUKController.onPageLoad(draftId).url),
      bound.countryQuestion(CountryGoverningTrustPage, "countryGoverningTrust", routes.CountryGoverningTrustController.onPageLoad(draftId).url),
      bound.yesNoQuestion(AdministrationInsideUKPage, "administrationInsideUK", routes.AdministrationInsideUKController.onPageLoad(draftId).url),
      bound.countryQuestion(CountryAdministeringTrustPage, "countryAdministeringTrust", routes.CountryAdministeringTrustController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustOwnsUkPropertyOrLandPage, "trustOwnsUkPropertyOrLand", routes.TrustOwnsUkPropertyOrLandController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustListedOnEeaRegisterPage, "trustListedOnEeaRegister", routes.TrustListedOnEeaRegisterController.onPageLoad(draftId).url),
      trusteesBasedInUK(draftId, userAnswers),
      bound.yesNoQuestion(SettlorsBasedInTheUKPage, "settlorsBasedInTheUK", routes.SettlorsBasedInTheUKController.onPageLoad(draftId).url),
      bound.yesNoQuestion(EstablishedUnderScotsLawPage, "establishedUnderScotsLaw", routes.EstablishedUnderScotsLawController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustResidentOffshorePage, "trustResidentOffshore", routes.TrustResidentOffshoreController.onPageLoad(draftId).url),
      bound.countryQuestion(TrustPreviouslyResidentPage, "trustPreviouslyResident", routes.TrustPreviouslyResidentController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustHasBusinessRelationshipInUkPage, "trustHasBusinessRelationshipInUk", routes.TrustHasBusinessRelationshipInUkController.onPageLoad(draftId).url),
      bound.yesNoQuestion(RegisteringTrustFor5APage, "registeringTrustFor5A", routes.RegisteringTrustFor5AController.onPageLoad(draftId).url),
      bound.yesNoQuestion(InheritanceTaxActPage, "inheritanceTaxAct", routes.InheritanceTaxActController.onPageLoad(draftId).url),
      bound.yesNoQuestion(AgentOtherThanBarristerPage, "agentOtherThanBarrister", routes.AgentOtherThanBarristerController.onPageLoad(draftId).url)
    ).flatten

  }

  private def trusteesBasedInUK(draftId: String, userAnswers: ReadableUserAnswers)
                               (implicit messages: Messages): Option[AnswerRow] = userAnswers.get(TrusteesBasedInTheUKPage) map {
    x => AnswerRow(
      "trusteesBasedInTheUK.checkYourAnswersLabel",
      CheckAnswersFormatters.answer("trusteesBasedInTheUK", x),
      Some(routes.TrusteesBasedInTheUKController.onPageLoad(draftId).url)
    )
  }

}
