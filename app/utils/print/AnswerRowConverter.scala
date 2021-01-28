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
import pages.register.trust_details.TrusteesBasedInTheUKPage
import play.api.i18n.Messages
import queries.Gettable
import utils.countryOptions.CountryOptions
import viewmodels.AnswerRow

import java.time.LocalDate

class AnswerRowConverter @Inject()(checkAnswersFormatters: CheckAnswersFormatters,
                                   countryOptions: CountryOptions) {

  def bind(userAnswers: ReadableUserAnswers)
          (implicit messages: Messages): Bound = new Bound(userAnswers)

  class Bound(userAnswers: ReadableUserAnswers)(implicit messages: Messages) {

    def stringQuestion(query: Gettable[String],
                       labelKey: String,
                       changeUrl: String): Option[AnswerRow] = {
      userAnswers.get(query) map { x =>
        AnswerRow(
          s"$labelKey.checkYourAnswersLabel",
          checkAnswersFormatters.escape(x),
          Some(changeUrl)
        )
      }
    }

    def yesNoQuestion(query: Gettable[Boolean],
                      labelKey: String,
                      changeUrl: String): Option[AnswerRow] = {
      userAnswers.get(query) map { x =>
        AnswerRow(
          s"$labelKey.checkYourAnswersLabel",
          checkAnswersFormatters.yesOrNo(x),
          Some(changeUrl)
        )
      }
    }

    def dateQuestion(query: Gettable[LocalDate],
                     labelKey: String,
                     changeUrl: String): Option[AnswerRow] = {
      userAnswers.get(query) map { x =>
        AnswerRow(
          s"$labelKey.checkYourAnswersLabel",
          checkAnswersFormatters.formatDate(x),
          Some(changeUrl)
        )
      }
    }

    def countryQuestion(query: Gettable[String],
                        labelKey: String,
                        changeUrl: String): Option[AnswerRow] = {
      userAnswers.get(query) map { x =>
        AnswerRow(
          s"$labelKey.checkYourAnswersLabel",
          checkAnswersFormatters.country(x, countryOptions),
          Some(changeUrl)
        )
      }
    }

    def trusteesBasedInUK(draftId: String): Option[AnswerRow] = {
      userAnswers.get(TrusteesBasedInTheUKPage) map { x =>
        AnswerRow(
          "trusteesBasedInTheUK.checkYourAnswersLabel",
          checkAnswersFormatters.answer("trusteesBasedInTheUK", x),
          Some(routes.TrusteesBasedInTheUKController.onPageLoad(draftId).url)
        )
      }
    }

  }
}
