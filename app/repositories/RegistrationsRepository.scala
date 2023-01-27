/*
 * Copyright 2023 HM Revenue & Customs
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

package repositories

import config.FrontendAppConfig
import connectors.SubmissionDraftConnector
import models.{ReadOnlyUserAnswers, ReadableUserAnswers, TaskStatus, UserAnswers}
import pages.register.trust_details.WhenTrustSetupPage
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.{Logging, http}
import services.TrustsStoreService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Session

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultRegistrationsRepository @Inject()(submissionDraftConnector: SubmissionDraftConnector,
                                               config: FrontendAppConfig,
                                               submissionSetFactory: SubmissionSetFactory,
                                               trustStoreService: TrustsStoreService
                                              )(implicit ec: ExecutionContext) extends RegistrationsRepository with Logging {

  private val userAnswersSection = config.repositoryKey
  private val mainAnswersSection = "main"

  override def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, messages: Messages): Future[Boolean] = {
    for {
      dataSet <- submissionSetFactory.createFrom(userAnswers)
      response <- submissionDraftConnector.setDraftSectionSet(
        userAnswers.draftId,
        userAnswersSection,
        dataSet
      )
    } yield {
      response.status == http.Status.OK
    }
  }

  override def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    submissionDraftConnector.getDraftSection(draftId, userAnswersSection).map {
      response =>
        response.data.validate[UserAnswers] match {
          case JsSuccess(userAnswers, _) => Some(userAnswers)
          case _ => None
        }
    }
  }

  override def getMainAnswers(draftId: String)(implicit hc: HeaderCarrier): Future[Option[ReadableUserAnswers]] = {
    submissionDraftConnector.getDraftSection(draftId, mainAnswersSection).map {
      response =>
        response.data.validate[ReadOnlyUserAnswers] match {
          case JsSuccess(userAnswers, _) => Some(userAnswers)
          case _ => None
        }
    }
  }

  override def modifyTaxLiabilityState(userAnswers: UserAnswers)
                                      (implicit hc: HeaderCarrier): Future[Unit] = {

    userAnswers.get(WhenTrustSetupPage) match {
      case Some(trustStartDate) =>
        submissionDraftConnector.getTaxLiabilityStartDate(userAnswers.draftId) flatMap {
          case Some(date) =>
            if (trustStartDate.isEqual(date)) {
              logger.info(s"[.modifyTaxLiabilityState][${Session.id(hc)}] tax liability does not need reset")
              Future.successful(())
            } else {
              resetTaxLiability(userAnswers)
            }
          case None =>
            logger.info(s"[.modifyTaxLiabilityState][${Session.id(hc)}] tax liability has not been answered, nothing to reset")
            Future.successful(())
        }
      case None =>
        logger.info(s"[.modifyTaxLiabilityState][${Session.id(hc)}] no trust start date, nothing to reset")
        Future.successful(())
    }
  }

  private def resetTaxLiability(userAnswers: UserAnswers)
                               (implicit hc: HeaderCarrier): Future[Unit] = {
    val updateStatus = trustStoreService.updateTaxLiabilityTaskStatus(userAnswers.draftId, TaskStatus.InProgress)
    val reset = submissionDraftConnector.resetTaxLiability(userAnswers.draftId)
    for {
      _ <- updateStatus
      _ <- reset
    } yield {
      logger.info(s"[.resetTaxLiability][${Session.id(hc)}] tax liability has been reset")
      ()
    }
  }
}

trait RegistrationsRepository {

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, messages: Messages): Future[Boolean]

  def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]]

  def getMainAnswers(draftId: String)(implicit hc: HeaderCarrier): Future[Option[ReadableUserAnswers]]

  def modifyTaxLiabilityState(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Unit]
}
