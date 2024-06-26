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

import controllers.actions.StandardActionSets
import forms.CountryGoverningTrustFormProvider
import javax.inject.Inject
import navigation.Navigator
import pages.register.trust_details.CountryGoverningTrustPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptionsNonUK
import views.html.register.trust_details.CountryGoverningTrustView

import scala.concurrent.{ExecutionContext, Future}

class CountryGoverningTrustController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 registrationsRepository: RegistrationsRepository,
                                                 navigator: Navigator,
                                                 formProvider: CountryGoverningTrustFormProvider,
                                                 standardActions: StandardActionSets,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: CountryGoverningTrustView,
                                                 val countryOptions: CountryOptionsNonUK
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(draftId: String) = standardActions.identifiedUserWithData(draftId)

  val form: Form[String] = formProvider()

  def onPageLoad(draftId: String): Action[AnyContent] = actions(draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(CountryGoverningTrustPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, countryOptions.options(), draftId))
  }

  def onSubmit(draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, countryOptions.options(), draftId))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CountryGoverningTrustPage, value))
            _              <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CountryGoverningTrustPage, draftId, updatedAnswers))
        }
      )
  }
}
