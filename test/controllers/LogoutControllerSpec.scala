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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.{any, argThat, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify, when}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.net.URLEncoder
import scala.concurrent.ExecutionContext

class LogoutControllerSpec extends SpecBase {

  "logout should redirect and not audit when auditing is disabled" in {

    val mockAuditConnector = Mockito.mock(classOf[AuditConnector])
    val mockAppConfig      = Mockito.mock(classOf[FrontendAppConfig])

    when(mockAppConfig.logoutAudit).thenReturn(false)
    when(mockAppConfig.logoutWithBasGatewayUrl).thenReturn(frontendAppConfig.logoutWithBasGatewayUrl)
    when(mockAppConfig.feedbackFrontendUrl).thenReturn(frontendAppConfig.feedbackFrontendUrl)

    val application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AuditConnector].toInstance(mockAuditConnector),
          bind[FrontendAppConfig].toInstance(mockAppConfig)
        )
        .build()

    val continueUrl = URLEncoder.encode(frontendAppConfig.feedbackFrontendUrl, "UTF-8")

    val expectedUrl = s"${frontendAppConfig.logoutWithBasGatewayUrl}?continue=$continueUrl"

    val request = FakeRequest(GET, routes.LogoutController.logout().url)
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER

    val value = redirectLocation(result).value

    value mustBe expectedUrl

    value must include("useServiceNavigation")

    verify(mockAuditConnector, never())
      .sendExplicitAudit(eqTo("trusts"), any[Map[String, String]])(any[HeaderCarrier], any[ExecutionContext])

    application.stop()
  }

  "logout should redirect and audit when auditing is enabled" in {
    val mockAuditConnector = Mockito.mock(classOf[AuditConnector])
    val mockAppConfig      = Mockito.mock(classOf[FrontendAppConfig])

    when(mockAppConfig.logoutAudit).thenReturn(true)
    when(mockAppConfig.logoutWithBasGatewayUrl).thenReturn(frontendAppConfig.logoutWithBasGatewayUrl)
    when(mockAppConfig.feedbackFrontendUrl).thenReturn(frontendAppConfig.feedbackFrontendUrl)

    val application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AuditConnector].toInstance(mockAuditConnector),
          bind[FrontendAppConfig].toInstance(mockAppConfig)
        )
        .build()

    val request = FakeRequest(GET, routes.LogoutController.logout().url)
    val result  = route(application, request).value

    val continueUrl = URLEncoder.encode(mockAppConfig.feedbackFrontendUrl, "UTF-8")

    val expectedUrl = s"${mockAppConfig.logoutWithBasGatewayUrl}?continue=$continueUrl"

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustBe expectedUrl

    verify(mockAuditConnector)
      .sendExplicitAudit(
        eqTo("trusts"),
        argThat[Map[String, String]] { m =>
          m.get("event").contains("signout") &&
          m.get("service").contains("register-trust-details-frontend") &&
          m.get("sessionId").exists(_.nonEmpty) &&
          m.contains("userGroup")
        }
      )(any[HeaderCarrier], any[ExecutionContext])

    application.stop()
  }

  "logout should redirect to a feedback url that requests the service navigation component" in {
    val mockAuditConnector = Mockito.mock(classOf[AuditConnector])

    val application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AuditConnector].toInstance(mockAuditConnector))
        .build()

    val request = FakeRequest(GET, routes.LogoutController.logout().url)
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value must include("useServiceNavigation")

    application.stop()
  }

}
