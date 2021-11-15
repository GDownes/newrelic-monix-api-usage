lazy val akkaHttpVersion = "10.2.4"
lazy val akkaVersion    = "2.6.15"

lazy val newrelicAgentPath = "/Users/gerarddownes/dev/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.4"
    )),
    name := "newrelic-monix-api-usage",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"             %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka"             %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka"             %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka"             %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"                % "logback-classic"           % "1.2.3",
      "io.monix"                      %% "monix-eval"               % "3.4.0",
      "com.newrelic.agent.java"       %% "newrelic-scala-monix-api"  % "7.5.0" from("file:///Users/gerarddownes/dev/newrelic-java-agent/newrelic-scala-monix-api/build/libs/newrelic-scala-monix-api_2.13-7.5.0-SNAPSHOT.jar")
    )
  )

Compile / run / fork := true
run / javaOptions ++= Seq(s"-javaagent:$newrelicAgentPath", "-Dnewrelic.config.log_level=FINEST", "-Dnewrelic.debug=true")
