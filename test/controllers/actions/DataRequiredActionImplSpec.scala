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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImplSpec extends SpecBase {

  implicit private val ec: ExecutionContext = ExecutionContext.global

  private val action = new DataRequiredActionImpl()

  "DataRequiredActionImpl" should {

    "redirect to SessionExpired when userAnswers are missing" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/some-path")

      val optReq: OptionalDataRequest[AnyContentAsEmpty.type] =
        OptionalDataRequest(
          request = request,
          internalId = "int-123",
          userAnswers = None
        )

      val resultF: Future[Result] =
        action.invokeBlock(optReq, (_: DataRequest[AnyContentAsEmpty.type]) => Future.successful(Ok))

      status(resultF)           mustBe SEE_OTHER
      redirectLocation(resultF) mustBe Some(routes.SessionExpiredController.onPageLoad.url)
    }

    "call the block with a DataRequest when userAnswers are present" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/some-path")

      val optReq: OptionalDataRequest[AnyContentAsEmpty.type] =
        OptionalDataRequest(
          request = request,
          internalId = "int-123",
          userAnswers = Some(emptyUserAnswers) // from SpecBase
        )

      val resultF: Future[Result] =
        action.invokeBlock(
          optReq,
          (dr: DataRequest[AnyContentAsEmpty.type]) =>
            Future.successful(
              Ok(s"id=${dr.internalId};auth=${dr.userAnswers.internalAuthId}")
            )
        )

      status(resultF) mustBe OK
      val body = contentAsString(resultF)
      body must include("id=int-123")
      body must include(s"auth=$userInternalId") // from SpecBase
    }
  }

}
