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

package utils.print

import com.google.inject.Inject
import controllers.register.trust_details.routes
import models.ReadableUserAnswers
import pages.register.trust_details._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import utils.answers.CheckAnswersFormatters
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection}

class TrustDetailsPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                        countryOptions: CountryOptions
                                       ) {

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
      countryGoverningTrust(draftId, userAnswers),
      bound.yesNoQuestion(AdministrationInsideUKPage, "administrationInsideUK", routes.AdministrationInsideUKController.onPageLoad(draftId).url),
      countryAdministeringTrust(draftId, userAnswers),
      trusteesBasedInUK(draftId, userAnswers),
      bound.yesNoQuestion(SettlorsBasedInTheUKPage, "settlorsBasedInTheUK", routes.SettlorsBasedInTheUKController.onPageLoad(draftId).url),
      bound.yesNoQuestion(EstablishedUnderScotsLawPage, "establishedUnderScotsLaw", routes.EstablishedUnderScotsLawController.onPageLoad(draftId).url),
      bound.yesNoQuestion(TrustResidentOffshorePage, "trustResidentOffshore", routes.TrustResidentOffshoreController.onPageLoad(draftId).url),
      trustPreviouslyResident(draftId, userAnswers),
      bound.yesNoQuestion(RegisteringTrustFor5APage, "registeringTrustFor5A", routes.RegisteringTrustFor5AController.onPageLoad(draftId).url),
      nonResidentType(draftId, userAnswers),
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

  private def nonResidentType(draftId: String, userAnswers: ReadableUserAnswers)
                             (implicit messages: Messages): Option[AnswerRow] = userAnswers.get(NonResidentTypePage) map {
    x => AnswerRow(
      "nonresidentType.checkYourAnswersLabel",
      CheckAnswersFormatters.answer("nonresidentType", x),
      Some(routes.NonResidentTypeController.onPageLoad(draftId).url)
    )
  }

  private def countryGoverningTrust(draftId: String, userAnswers: ReadableUserAnswers): Option[AnswerRow] = userAnswers.get(CountryGoverningTrustPage) map {
    x => AnswerRow(
      "countryGoverningTrust.checkYourAnswersLabel",
      HtmlFormat.escape(CheckAnswersFormatters.country(x, countryOptions)),
      Some(controllers.register.trust_details.routes.CountryGoverningTrustController.onPageLoad(draftId).url))
  }

  private def countryAdministeringTrust(draftId: String, userAnswers: ReadableUserAnswers): Option[AnswerRow] = userAnswers.get(CountryAdministeringTrustPage) map {
    x => AnswerRow(
      "countryAdministeringTrust.checkYourAnswersLabel",
      HtmlFormat.escape(CheckAnswersFormatters.country(x, countryOptions)),
      Some(controllers.register.trust_details.routes.CountryAdministeringTrustController.onPageLoad(draftId).url))
  }

  private def trustPreviouslyResident(draftId: String, userAnswers: ReadableUserAnswers): Option[AnswerRow] = userAnswers.get(TrustPreviouslyResidentPage) map {
    x => AnswerRow(
      "trustPreviouslyResident.checkYourAnswersLabel",
      HtmlFormat.escape(CheckAnswersFormatters.country(x, countryOptions)),
      Some(controllers.register.trust_details.routes.TrustPreviouslyResidentController.onPageLoad(draftId).url))
  }


}
