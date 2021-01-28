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

import java.time.LocalDate
import com.google.inject.Inject
import models.ReadableUserAnswers
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.Gettable
import utils.answers.CheckAnswersFormatters
import utils.answers.CheckAnswersFormatters._
import utils.countryOptions.CountryOptions
import viewmodels.AnswerRow

class AnswerRowConverter @Inject()(countryOptions: CountryOptions) {

  def bind(userAnswers: ReadableUserAnswers)
          (implicit messages: Messages): Bound = new Bound(userAnswers)

  class Bound(userAnswers: ReadableUserAnswers)(implicit messages: Messages) {

    def stringQuestion(query: Gettable[String],
                       labelKey: String,
                       changeUrl: String): Option[AnswerRow] = {
      userAnswers.get(query) map {x =>
        AnswerRow(
          s"$labelKey.checkYourAnswersLabel",
          HtmlFormat.escape(x),
          Some(changeUrl)
        )
      }
    }

    def yesNoQuestion(query: Gettable[Boolean],
                      labelKey: String,
                      changeUrl: String): Option[AnswerRow] = {
      userAnswers.get(query) map {x =>
        AnswerRow(
          s"$labelKey.checkYourAnswersLabel",
          yesOrNo(x),
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
          HtmlFormat.escape(x.format(dateFormatter)),
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
          HtmlFormat.escape(CheckAnswersFormatters.country(x, countryOptions)),
          Some(changeUrl)
        )
      }
    }
  }
}
