import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.uwekorn",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "nyc-taxi-data",
    libraryDependencies ++= Seq(hadoop_common, parquet_avro, tixxit),
    fork in run := true
  )
