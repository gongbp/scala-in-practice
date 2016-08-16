
name := "scala-in-practice"

version := "1.0"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

fork := false

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.9.5"

libraryDependencies += "com.github.scala-blitz" %% "scala-blitz" % "1.2"

libraryDependencies += "com.netflix.rxjava" % "rxjava-scala" % "0.19.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "1.0.1"

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.2"

libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.3.2"

libraryDependencies += "com.storm-enroute" %% "scalameter-core" % "0.6"

libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.0.6"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-experimental" % "0.4"

libraryDependencies += "com.storm-enroute" %% "reactive-collections" % "0.5"

libraryDependencies += "joda-time" % "joda-time" % "2.7"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
