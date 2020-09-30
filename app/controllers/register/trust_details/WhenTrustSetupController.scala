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

import java.time.LocalDate

import config.FrontendAppConfig
import controllers.actions.register.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import forms.WhenTrustSetupFormProvider
import javax.inject.Inject
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.register.settlors.deceased_settlor.SettlorDateOfDeathPage
import pages.trust_details.WhenTrustSetupPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.register.trust_details.WhenTrustSetupView

import scala.concurrent.{ExecutionContext, Future}

class WhenTrustSetupController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          registrationsRepository: RegistrationsRepository,
                                          navigator: Navigator,
                                          identify: RegistrationIdentifierAction,
                                          getData: DraftIdRetrievalActionProvider,
                                          requireData: RegistrationDataRequiredAction,
                                          formProvider: WhenTrustSetupFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: WhenTrustSetupView,
                                          appConfig: FrontendAppConfig
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


 private def actions(draftId: String) = identify andThen getData(draftId) andThen requireData

  private def form(draftId: String)(implicit hc: HeaderCarrier): Future[Form[LocalDate]] =
    minDate(draftId) map { config =>
      formProvider.withConfig(config)
    }

  def onPageLoad(draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhenTrustSetupPage) match {
        case None => form(draftId)
        case Some(value) => form(draftId).map(_.fill(value))
      }

      preparedForm map { form =>
        Ok(view(form, draftId))
      }

  }

  def onSubmit(draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      form(draftId).flatMap(_.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhenTrustSetupPage, value))
            _              <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhenTrustSetupPage, draftId, updatedAnswers))
        }
      ))
  }

  private def minDate(draftId: String)(implicit hc: HeaderCarrier): Future[(LocalDate, String)] = {
    registrationsRepository.getMainAnswers(draftId) map {
      _.flatMap {
        _.get(SettlorDateOfDeathPage) map { dateOfDeath =>
          (dateOfDeath, "beforeDateOfDeath")
        }
      } getOrElse ((appConfig.minDate, "past"))
    }
  }
}

