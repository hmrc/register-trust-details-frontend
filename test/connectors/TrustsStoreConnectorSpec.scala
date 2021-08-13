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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.TaskStatus.Completed
import models.{FeatureResponse, Task}
import org.scalatest.{MustMatchers, OptionValues}
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class TrustsStoreConnectorSpec extends SpecBase with MustMatchers with OptionValues with WireMockHelper {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Seq(
      "microservice.services.trusts-store.port" -> server.port(),
      "auditing.enabled" -> false): _*
    ).build()

  private lazy val connector = injector.instanceOf[TrustsStoreConnector]

  "TrustsStoreConnector" when {

    ".updateTaskStatus" must {

      val url = s"/trusts-store/register/tasks/update-trust-details/$draftId"

      "return OK with the current task status" in {

        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok())
        )

        whenReady(connector.updateTaskStatus(draftId, Completed)) {
          _.status mustBe 200
        }
      }

      "return default tasks when a failure occurs" in {

        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(serverError())
        )

        connector.updateTaskStatus(draftId, Completed) map {
          _.status mustBe 500
        }
      }
    }

    ".getTaskStatus" must {

      val url = s"/trusts-store/register/tasks/$draftId"

      "return OK with the current task status" in {

        val json = Json.parse(
          """
            |{
            |  "trustDetails": "completed"
            |}
            |""".stripMargin)

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(json.toString()))
        )

        whenReady(connector.getTaskStatus(draftId)) {
          _ mustBe Task(Completed)
        }
      }
    }

    ".getFeature" must {

      val url = s"/trusts-store/features/5mld"

      "return a feature flag of true if 5mld is enabled" in {

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(
                  Json.stringify(
                    Json.toJson(FeatureResponse("5mld", isEnabled = true))
                  )
                )
            )
        )

        whenReady(connector.getFeature("5mld")) {
          _ mustBe FeatureResponse("5mld", isEnabled = true)
        }
      }

      "return a feature flag of false if 5mld is not enabled" in {

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(
                  Json.stringify(
                    Json.toJson(FeatureResponse("5mld", isEnabled = false))
                  )
                )
            )
        )

        whenReady(connector.getFeature("5mld")) {
          _ mustBe FeatureResponse("5mld", isEnabled = false)
        }
      }
    }
  }
}
