import sbt.*

object AppDependencies {

  val bootstrapVersion = "7.23.0"

  private lazy val compile = Seq(
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-28"     % "8.5.0",
    "uk.gov.hmrc"             %% "domain"                         % "8.3.0-play-28",
    "uk.gov.hmrc"             %% "play-conditional-form-mapping"  % "1.13.0-play-28",
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28"     % bootstrapVersion
  )

  private lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.jsoup"                   %  "jsoup"                    % "1.17.2",
    "org.scalatest"               %% "scalatest"                % "3.2.18",
    "org.scalatestplus"           %% "scalacheck-1-17"          % "3.2.18.0",
    "org.mockito"                 %% "mockito-scala-scalatest"  % "1.17.30",
    "org.wiremock"                %  "wiremock-standalone"      % "3.4.2",
    "io.github.wolfendale"        %% "scalacheck-gen-regexp"    % "1.1.0",
    "com.vladsch.flexmark"        %  "flexmark-all"             % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
