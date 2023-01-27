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

package controllers

import connectors.SubmissionDraftConnector
import controllers.actions.register.RegistrationIdentifierAction
import models.TaskStatus._
import models.UserAnswers
import models.registration.Matched.Success
import models.requests.IdentifierRequest
import pages.register.ExistingTrustMatched
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RegistrationsRepository
import services.TrustsStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 repository: RegistrationsRepository,
                                 identify: RegistrationIdentifierAction,
                                 trustsStoreService: TrustsStoreService,
                                 submissionDraftConnector: SubmissionDraftConnector
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(draftId: String): Action[AnyContent] = identify.async { implicit request: IdentifierRequest[AnyContent] =>

    def redirect(userAnswers: UserAnswers): Future[Result] = {

      for {
        taskStatus <- trustsStoreService.getTaskStatus(draftId)
        _ <- repository.set(userAnswers)
        successfullyMatched <- repository.getMainAnswers(draftId).map(_.exists(_.get(ExistingTrustMatched).contains(Success)))
      } yield {
        if (taskStatus.isCompleted) {
          Redirect(controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId))
        } else {
          if (successfullyMatched) {
            Redirect(controllers.register.trust_details.routes.WhenTrustSetupController.onPageLoad(draftId))
          } else {
            Redirect(controllers.register.trust_details.routes.TrustNameController.onPageLoad(draftId))
          }
        }
      }
    }

    for {
      isTaxable <- submissionDraftConnector.getIsTrustTaxable(draftId)
      isExpress <- submissionDraftConnector.getIsExpressTrust(draftId)
      userAnswers <- repository.get(draftId)
      result <- userAnswers match {
        case Some(value) => redirect(value.copy(isTaxable = isTaxable, isExpress = isExpress))
        case None => redirect(UserAnswers(draftId, Json.obj(), request.identifier, isTaxable, isExpress))
      }
      _ <- trustsStoreService.updateTaskStatus(draftId, InProgress)
    } yield result
  }
}
