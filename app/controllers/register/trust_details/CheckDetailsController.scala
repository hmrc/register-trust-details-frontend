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

package controllers.register.trust_details

import config.FrontendAppConfig
import controllers.actions._
import models.TaskStatus.Completed
import navigation.Navigator
import pages.register.trust_details.CheckDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import services.TrustsStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.TrustDetailsPrintHelper
import viewmodels.AnswerSection
import views.html.register.trust_details.CheckDetailsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        repository: RegistrationsRepository,
                                        navigator: Navigator,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        val appConfig: FrontendAppConfig,
                                        printHelper: TrustDetailsPrintHelper,
                                        trustsStoreService: TrustsStoreService
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId) {
    implicit request =>

      val section: AnswerSection = printHelper.checkDetailsSection(request.userAnswers)
      Ok(view(Seq(section), draftId))
  }

  def onSubmit(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).async {
    implicit request =>

      for {
        _ <- trustsStoreService.updateTaskStatus(draftId, Completed)
        _ <- repository.set(request.userAnswers)
        _ <- repository.modifyTaxLiabilityState(request.userAnswers)
      } yield Redirect(navigator.nextPage(CheckDetailsPage, draftId, request.userAnswers))
  }
}
