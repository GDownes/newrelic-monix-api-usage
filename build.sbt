lazy val akkaHttpVersion = "10.2.4"
lazy val akkaVersion    = "2.6.15"
lazy val sttpVersion    = "3.3.6"

lazy val newrelicAgentPath = "/home/gdownes/dev/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.4"
    )),
    name := "akka-http-sttp",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"             %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka"             %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka"             %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka"             %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"                % "logback-classic"           % "1.2.3",
      "com.softwaremill.sttp.client3" %% "core"                     % sttpVersion,
      "com.softwaremill.sttp.client3" %% "akka-http-backend"        % sttpVersion,
      "com.softwaremill.sttp.client3" %% "http4s-ce2-backend"       % sttpVersion,
      "org.http4s"                    %% "http4s-blaze-client"      % "0.21.24"
    )
  )

Compile / run / fork := true
run / javaOptions ++= Seq(s"-javaagent:$newrelicAgentPath", "-Dnewrelic.config.log_level=FINEST", "-Dnewrelic.debug=true")
