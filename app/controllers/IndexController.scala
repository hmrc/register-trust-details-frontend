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

package controllers

import controllers.actions.register.RegistrationIdentifierAction
import javax.inject.Inject
import models.Status.Completed
import models.UserAnswers
import models.registration.Matched.Success
import models.requests.IdentifierRequest
import pages.TrustDetailsStatus
import pages.register.ExistingTrustMatched
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 repository: RegistrationsRepository,
                                 identify: RegistrationIdentifierAction
                               ) extends FrontendBaseController with I18nSupport {

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  def onPageLoad(draftId: String): Action[AnyContent] = identify.async { implicit request: IdentifierRequest[AnyContent] =>

    repository.get(draftId) flatMap {
      case Some(userAnswers) =>
        redirect(userAnswers, draftId)
      case _ =>
        val userAnswers = UserAnswers(draftId, Json.obj(), request.identifier)
        repository.set(userAnswers) flatMap {
          _ => redirect(userAnswers, draftId)
        }
    }
  }

  private def redirect(userAnswers: UserAnswers, draftId: String)(implicit request: IdentifierRequest[AnyContent]): Future[Result] = {
    val completed = userAnswers.get(TrustDetailsStatus).contains(Completed)

    if (completed) {
      Future.successful(Redirect(controllers.register.trust_details.routes.CheckDetailsController.onPageLoad(draftId)))
    } else {
      successfullyMatched(draftId) map {
        matched =>
          if (matched) {
            Redirect(controllers.register.trust_details.routes.WhenTrustSetupController.onPageLoad(draftId))
          } else {
            Redirect(controllers.register.trust_details.routes.TrustNameController.onPageLoad(draftId))
          }
      }
    }
  }

  private def successfullyMatched(draftId: String)(implicit request: IdentifierRequest[AnyContent]): Future[Boolean] = {
    repository.getMainAnswers(draftId) map {
        _.exists {
          _.get(ExistingTrustMatched).contains(Success)
        }
    }
  }
}
