name := "DeDup"

version := "0.1"

scalaVersion := "2.13.10"

val commonsLangVersion = "3.12.0"
val commonsTextVersion = "1.10.0"
val jacksonAnnotationVersion = "2.15.0"
val jacksonCoreVersion = "2.15.0"
val jacksonDatabindVersion = "2.15.0"
val jsonSimpleVersion = "1.1.1"
val luceneVersion = "9.6.0"
val jakartaServletApiVersion = "6.0.0"
val jakartaWsRsVersion= "3.1.0"

val log4jVersion = "2.20.0" //"2.19.0"


libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-lang3" % commonsLangVersion,
  "org.apache.commons" % "commons-text" % commonsTextVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonAnnotationVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonCoreVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion,
  "com.googlecode.json-simple" % "json-simple" % jsonSimpleVersion,
  "org.apache.lucene" % "lucene-analysis-common" % luceneVersion,
  "org.apache.lucene" % "lucene-core" % luceneVersion,
  "org.apache.lucene" % "lucene-queryparser" % luceneVersion,
  "org.apache.lucene" % "lucene-suggest" % luceneVersion,
  "org.apache.lucene" % "lucene-backward-codecs" % luceneVersion,
  "org.apache.lucene" % "lucene-codecs" % luceneVersion % Test,
  "jakarta.servlet" % "jakarta.servlet-api" % jakartaServletApiVersion % "provided",
  "jakarta.ws.rs" % "jakarta.ws.rs-api" % jakartaWsRsVersion,
  
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-web" % log4jVersion
)

Test / logBuffered := false
trapExit := false

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Ywarn-unused")

enablePlugins(JettyPlugin)

assembly / assemblyMergeStrategy := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

/*
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}
*/

//test in assembly := {}
