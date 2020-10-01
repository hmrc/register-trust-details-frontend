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

package utils

import models.Status.{Completed, InProgress}
import models.{ReadableUserAnswers, Status}
import pages.TrustDetailsStatus
import pages.register.trust_details.WhenTrustSetupPage

class RegistrationProgress  {

  def trustDetailsStatus(userAnswers: ReadableUserAnswers): Option[Status] =
  userAnswers.get(WhenTrustSetupPage) match {
    case None => None
    case Some(_) =>
      if (userAnswers.get(TrustDetailsStatus).contains(Completed)) {
        Some(Completed)
      } else {
        Some(InProgress)
      }
  }
}
