import sbt._

object Dependencies {

  object Versions {
    val cats                = "2.5.0"
    val catsEffect          = "2.4.1"
    val fs2                 = "2.5.4"
    val http4s              = "0.21.22"
    val circe               = "0.13.0"
    val pureConfig          = "0.14.1"

    val kindProjector       = "0.10.3"
    val logback             = "1.2.3"
    val scalaCheck          = "1.15.3"
    val scalaTest           = "3.1.0"
    val catsScalaCheck      = "0.3.0"
    val akkaVersion         = "2.7.0"
    val akkaHttpVersion     = "10.4.0"
    val scalaCache          = "0.28.0"
    val guavaCacheVersion   = "10.0.1"
    val scalaMockVersion     = "5.1.0"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe"    %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

    lazy val cats                = "org.typelevel"         %% "cats-core"                  % Versions.cats
    lazy val catsEffect          = "org.typelevel"         %% "cats-effect"                % Versions.catsEffect
    lazy val fs2                 = "co.fs2"                %% "fs2-core"                   % Versions.fs2

    lazy val http4sDsl           = http4s("http4s-dsl")
    lazy val http4sServer        = http4s("http4s-blaze-server")
    lazy val http4sCirce         = http4s("http4s-circe")
    lazy val circeCore           = circe("circe-core")
    lazy val circeGeneric        = circe("circe-generic")
    lazy val circeGenericExt     = circe("circe-generic-extras")
    lazy val circeParser         = circe("circe-parser")
    lazy val pureConfig          = "com.github.pureconfig" %% "pureconfig"                 % Versions.pureConfig

    // Compiler plugins
    lazy val kindProjector       = "org.typelevel"         %% "kind-projector"             % Versions.kindProjector

    // Runtime
    lazy val logback             = "ch.qos.logback"        %  "logback-classic"            % Versions.logback

    // Test
    lazy val scalaTest           = "org.scalatest"         %% "scalatest"                  % Versions.scalaTest
    lazy val scalaCheck          = "org.scalacheck"        %% "scalacheck"                 % Versions.scalaCheck
    lazy val catsScalaCheck      = "io.chrisdavenport"     %% "cats-scalacheck"            % Versions.catsScalaCheck

    lazy val akkaStream          = "com.typesafe.akka"     %% "akka-stream"                % Versions.akkaVersion
    lazy val akkaHttpSprayJson   = "com.typesafe.akka"     %% "akka-http-spray-json"       % Versions.akkaHttpVersion
    lazy val akkaHttp            = "com.typesafe.akka"     %% "akka-http"                  % Versions.akkaHttpVersion
    lazy val scalaCache          = "com.github.cb372"      %% "scalacache-core"            % Versions.scalaCache
    lazy val scalaCacheCaffeine  = "com.github.cb372"      %% "scalacache-caffeine"        % Versions.scalaCache
    lazy val scalaMock           = "org.scalamock"         %% "scalamock"                  % Versions.scalaMockVersion

  }

}
