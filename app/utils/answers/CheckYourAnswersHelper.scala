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

package utils.answers

import javax.inject.Inject
import viewmodels.AnswerRow
import models.{Address, UserAnswers}
import pages.QuestionPage
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.twirl.api.HtmlFormat
import utils.countryOptions.CountryOptions
import CheckAnswersFormatters._

class CheckYourAnswersHelper @Inject()(countryOptions: CountryOptions)
                                      (userAnswers: UserAnswers,
                                       arg: String = "",
                                       canEdit: Boolean)
                                      (implicit messages: Messages) {


  def stringQuestion(query: QuestionPage[String],
                     labelKey: String,
                     changeUrl: Option[String]): Option[AnswerRow] = {
    userAnswers.get(query) map {x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = HtmlFormat.escape(x),
        changeUrl = changeUrl,
        labelArg = arg,
        canEdit = canEdit
      )
    }
  }

  def yesNoQuestion(query: QuestionPage[Boolean],
                    labelKey: String,
                    changeUrl: Option[String]): Option[AnswerRow] = {
    userAnswers.get(query) map {x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = yesOrNo(x),
        changeUrl = changeUrl,
        labelArg = arg,
        canEdit = canEdit
      )
    }
  }

  def addressQuestion[T <: Address](query: QuestionPage[T],
                                    labelKey: String,
                                    changeUrl: Option[String])
                                   (implicit reads: Reads[T]): Option[AnswerRow] = {
    userAnswers.get(query) map {x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = addressFormatter(x, countryOptions),
        changeUrl = changeUrl,
        labelArg = arg,
        canEdit = canEdit
      )
    }
  }

}