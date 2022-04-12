/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig

import javax.inject.Inject
import models.{RegistrationSubmission, SubmissionDraftResponse}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SubmissionDraftConnector @Inject()(http: HttpClient, config : FrontendAppConfig) {

  val submissionsBaseUrl = s"${config.trustsUrl}/trusts/register/submission-drafts"

  def setDraftSectionSet(draftId: String, section: String, data: RegistrationSubmission.DataSet)
                        (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](s"$submissionsBaseUrl/$draftId/set/$section", Json.toJson(data))
  }

  def getDraftSection(draftId: String, section: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[SubmissionDraftResponse] = {
    http.GET[SubmissionDraftResponse](s"$submissionsBaseUrl/$draftId/$section")
  }

  def getIsTrustTaxable(draftId: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[Boolean] = {
    http.GET[Boolean](s"$submissionsBaseUrl/$draftId/is-trust-taxable").recover {
      case _ => true
    }
  }

  def getTrustStartDate(draftId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[LocalDate]] =
    getStartDate(s"$submissionsBaseUrl/$draftId/when-trust-setup")

  def getTaxLiabilityStartDate(draftId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[LocalDate]] =
    getStartDate(s"$submissionsBaseUrl/$draftId/tax-liability/when-trust-setup")

  def resetTaxLiability(draftId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    http.DELETE[HttpResponse](s"$submissionsBaseUrl/$draftId/tax-liability")

  private def getStartDate(url: String)
                          (implicit hc: HeaderCarrier,
                           ec: ExecutionContext
                          ): Future[Option[LocalDate]] =
    http.GET[HttpResponse](url).map {
      response =>
        (response.json \ "startDate").asOpt[LocalDate]
    }.recover {
      case _ => None
    }
}
