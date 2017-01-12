name := "CrjqSwiperMockServer"

version := "1.0"

lazy val `crjqswipermockserver` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq( jdbc , cache , ws   , specs2 % Test,
  "com.google.guava" % "guava" % "12.0",
  "org.webjars" % "bootstrap" % "3.3.4" exclude( "com.google.guava", "guava"))

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

includeFilter in (Assets, LessKeys.less) := "*.less"


