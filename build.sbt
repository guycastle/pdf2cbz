import com.typesafe.sbt.packager.windows.{AddDirectoryToPath, ComponentFile, WindowsFeature}

name := "pdf2cbz"

version := "1.0.1-SNAPSHOT"

scalaVersion := "2.13.2"

scalacOptions ++= Seq(
  "-encoding", "utf8", // Option and arguments on same line
  "-Xfatal-warnings",  // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

enablePlugins(JavaAppPackaging)
enablePlugins(WindowsPlugin)

assemblyJarName in assembly := "pdf2cbz.jar"

mainClass in assembly := Some("be.guycastle.Pdf2Cbz")

mainClass in Compile := Some("be.guycastle.Pdf2Cbz")

mappings in Windows := (mappings in Universal).value

// general package information (can be scoped to Windows)
maintainer := "Guillaume Vandecasteele <guillaume@trust1team.com>"
packageSummary := "pdf2cbz"
packageDescription := """PDF 2 CBZ MSI."""

// wix build information
wixProductId := "a1c2c7a8-d14d-41ae-95e5-a79fd79ca53b"
wixProductUpgradeId := "13570be8-5057-4eb4-a44d-6e07fea74c8c"

discoveredMainClasses in Compile := Seq()

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("org.apache.commons.io.**" -> "shadeio.@1").inAll
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val pdfBoxVersion = "2.0.19"
lazy val cliVersion = "1.4"
lazy val commonsIoVersion = "2.6"
lazy val slf4jVersion = "1.7.30"
lazy val jaiImageIoVersion = "1.4.0"
lazy val jaiJpeg2000Version = "1.3.0"

libraryDependencies ++= Seq(
  "com.github.jai-imageio" % "jai-imageio-core" % jaiImageIoVersion,
  "com.github.jai-imageio" % "jai-imageio-jpeg2000" % jaiJpeg2000Version,
  "org.apache.pdfbox" % "pdfbox" % pdfBoxVersion,
  "commons-cli" % "commons-cli" % cliVersion,
  "commons-io" % "commons-io" % commonsIoVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
)