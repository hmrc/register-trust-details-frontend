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

import controllers.actions.StandardActionSets
import forms.YesNoFormProvider
import javax.inject.Inject
import navigation.Navigator
import pages.register.trust_details.EstablishedUnderScotsLawPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.trust_details.EstablishedUnderScotsLawView

import scala.concurrent.{ExecutionContext, Future}

class EstablishedUnderScotsLawController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    registrationsRepository: RegistrationsRepository,
                                                    navigator: Navigator,
                                                    yesNoFormProvider: YesNoFormProvider,
                                                    standardActions: StandardActionSets,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: EstablishedUnderScotsLawView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(draftId: String) = standardActions.identifiedUserWithData(draftId)

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("establishedUnderScotsLawYesNo")

  def onPageLoad(draftId: String): Action[AnyContent] = actions(draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(EstablishedUnderScotsLawPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId))
  }

  def onSubmit(draftId: String) = actions(draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EstablishedUnderScotsLawPage, value))
            _              <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EstablishedUnderScotsLawPage, draftId, updatedAnswers))
        }
      )
  }
}
