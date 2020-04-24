import com.typesafe.sbt.packager.windows.{AddDirectoryToPath, ComponentFile, WindowsFeature}

name := "pdf2cbz"

version := "1.0.0"

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

lazy val pdfBoxVersion = "2.0.19"
lazy val cliVersion = "1.4"
lazy val commonsIoVersion = "2.6"
lazy val slf4jVersion = "1.7.30"

libraryDependencies ++= Seq(
  "org.apache.pdfbox" % "pdfbox" % pdfBoxVersion,
  "commons-cli" % "commons-cli" % cliVersion,
  "commons-io" % "commons-io" % commonsIoVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
)