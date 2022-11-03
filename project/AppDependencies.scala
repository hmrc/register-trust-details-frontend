import sbt._
import play.core.PlayVersion

object AppDependencies {

  private lazy val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"             %% "play-frontend-hmrc"             % "3.6.0-play-28",
    "uk.gov.hmrc"             %% "domain"                         % "8.1.0-play-28",
    "uk.gov.hmrc"             %% "play-conditional-form-mapping"  % "1.12.0-play-28",
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28"     % "7.8.0"
  )

  private lazy val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current,
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"           %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.jsoup"                   %  "jsoup"                    % "1.15.3",
    "org.scalatest"               %% "scalatest"                % "3.2.14",
    "org.mockito"                 %% "mockito-scala-scalatest"  % "1.17.12",
    "com.github.tomakehurst"      %  "wiremock-standalone"      % "2.27.2",
    "wolfendale"                  %% "scalacheck-gen-regexp"    % "0.1.2",
    "com.vladsch.flexmark"        %  "flexmark-all"             % "0.62.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
