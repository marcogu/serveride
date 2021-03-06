name := "CrjqSwiperMockServer"

version := "1.0"

lazy val `crjqswipermockserver` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq( jdbc , cache , ws   , specs2 % Test,
  "org.postgresql" % "postgresql" % "9.4.1211",
  "com.oracle" % "ojdbc6" % "11.1.0.7.0" ,
  "com.google.guava" % "guava" % "12.0",
  "org.webjars" % "bootstrap" % "3.3.4" exclude( "com.google.guava", "guava"))

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "oracle-odbc6" at "http://maven.jahia.org/maven2/"

includeFilter in (Assets, LessKeys.less) := "*.less"


