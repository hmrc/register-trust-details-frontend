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

package controllers.register.trust_details

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.Status.Completed
import navigation.Navigator
import pages.TrustDetailsStatus
import pages.register.trust_details.CheckDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.TrustDetailsPrintHelper
import viewmodels.AnswerSection
import views.html.register.trust_details.CheckDetailsView

import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        repository: RegistrationsRepository,
                                        navigator: Navigator,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        val appConfig: FrontendAppConfig,
                                        printHelper: TrustDetailsPrintHelper
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId) {
    implicit request =>

      val section: AnswerSection = printHelper.checkDetailsSection(request.userAnswers, draftId)
      Ok(view(Seq(section), draftId))
  }

  def onSubmit(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).async {
    implicit request =>

      val answers = request.userAnswers.set(TrustDetailsStatus, Completed)

      for {
        updatedAnswers <- Future.fromTry(answers)
        _ <- repository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(CheckDetailsPage, draftId, request.userAnswers))
  }
}
