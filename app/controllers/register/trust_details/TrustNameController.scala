/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions.register._
import forms.TrustNameFormProvider
import javax.inject.Inject
import navigation.Navigator
import pages.register.TrustHaveAUTRPage
import pages.register.trust_details.TrustNamePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.register.trust_details.TrustNameView

import scala.concurrent.{ExecutionContext, Future}

class TrustNameController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     registrationsRepository: RegistrationsRepository,
                                     navigator: Navigator,
                                     formProvider: TrustNameFormProvider,
                                     standardActions: StandardActionSets,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: TrustNameView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(draftId: String) = standardActions.identifiedUserWithData(draftId)

  val form = formProvider()

  def onPageLoad(draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(TrustNamePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      showHintText(draftId) map { hintTextShown =>
        Ok(view(preparedForm, draftId, hintTextShown))
      }

  }

  def onSubmit(draftId: String): Action[AnyContent] = actions(draftId: String).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          showHintText(draftId) map { hintTextShown =>
            BadRequest(view(formWithErrors, draftId, hintTextShown))
          },
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TrustNamePage, value))
            _              <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(TrustNamePage, draftId, updatedAnswers))
        }
      )
  }

  private def showHintText(draftId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    registrationsRepository.getMainAnswers(draftId) map {
      _.exists {
        _.get(TrustHaveAUTRPage).contains(true)
      }
    }
}
