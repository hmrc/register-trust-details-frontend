/*
 * Copyright 2026 HM Revenue & Customs
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

package handlers

import base.SpecBase
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.{ErrorTemplate, PageNotFoundView}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ErrorHandlerSpec extends SpecBase {

  private val messageApi: MessagesApi            = app.injector.instanceOf[MessagesApi]
  private val errorTemplate: ErrorTemplate       = app.injector.instanceOf[ErrorTemplate]
  private val pageNotFoundView: PageNotFoundView = app.injector.instanceOf[PageNotFoundView]
  private val errorHandler                       = new ErrorHandler(messageApi, errorTemplate, pageNotFoundView)

  "ErrorHandler" must {

    "standardErrorTemplate" in {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      val result = Await.result(
        errorHandler.standardErrorTemplate(
          pageTitle = "pageTitle",
          heading = "heading",
          message = "message"
        ),
        Duration.Inf
      )

      result mustBe errorTemplate("pageTitle", "heading", "message")
    }

    "notFoundTemplate" in {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      val result = Await.result(errorHandler.notFoundTemplate(fakeRequest), Duration.Inf)

      result mustBe pageNotFoundView()
    }
  }

}
