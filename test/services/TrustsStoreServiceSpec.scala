/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import base.SpecBase
import connectors.TrustsStoreConnector
import models.Task
import models.TaskStatus.Completed
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class TrustsStoreServiceSpec extends SpecBase {

  val mockConnector: TrustsStoreConnector = Mockito.mock(classOf[TrustsStoreConnector])

  val trustStoreService = new TrustsStoreService(mockConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  ".updateTaskStatus" must {
    "call trusts store connector" in {

      val draftId = "draftId"

      when(mockConnector.updateTaskStatus(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val result = trustStoreService.updateTaskStatus(draftId, Completed)

      whenReady(result) { res =>
        res.status mustBe OK
        verify(mockConnector).updateTaskStatus(eqTo(draftId), eqTo(Completed))(any(), any())
      }
    }
  }

  ".updateTaxLiabilityTaskStatus" must {
    "call trusts store connector" in {

      val draftId = "draftId"

      when(mockConnector.updateTaxLiabilityTaskStatus(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val result = trustStoreService.updateTaxLiabilityTaskStatus(draftId, Completed)

      whenReady(result) { res =>
        res.status mustBe OK
        verify(mockConnector).updateTaxLiabilityTaskStatus(eqTo(draftId), eqTo(Completed))(any(), any())
      }
    }
  }

  ".getTaskStatus" must {
    "call trusts store connector" in {

      val draftId = "draftId"

      when(mockConnector.getTaskStatus(any())(any(), any()))
        .thenReturn(Future.successful(Task(Completed)))

      val result = trustStoreService.getTaskStatus(draftId)

      whenReady(result) { res =>
        res mustBe Completed
        verify(mockConnector).getTaskStatus(eqTo(draftId))(any(), any())
      }
    }
  }
}
