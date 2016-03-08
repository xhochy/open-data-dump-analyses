mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
    {
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case "META-INF/README.txt" => MergeStrategy.discard
        case "META-INF/CHANGES.txt" => MergeStrategy.discard
        case "META-INF/NOTICE" => MergeStrategy.discard
        case "META-INF/NOTICE.TXT" => MergeStrategy.discard
        case "META-INF/LICENSE" => MergeStrategy.concat
        case "META-INF/LICENSE.txt" => MergeStrategy.concat
        case "META-INF/LICENSES.txt" => MergeStrategy.concat
        case x => MergeStrategy.first
    }
}

// set the name of the project
name := "wikipedia-dump-analysis"

version := "0.1.0"

scalaVersion := "2.11.7"

organization := "com.xhochy"

mainClass := Some("com.xhochy.App")

libraryDependencies ++= {
  Seq(
    "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
    "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",
    "commons-codec" % "commons-codec" % "1.9",
    "org.apache.commons" % "commons-compress" % "1.9",
    "net.jpountz.lz4" % "lz4" % "1.2.0",
    "org.specs2" %% "specs2" % "2.3.12" % "test",
    "org.scalatest" %% "scalatest" % "2.1.6" % "test",
    "org.apache.avro" % "avro" % "1.8.0",
    "org.apache.parquet" % "parquet-avro" % "1.8.1",
    "org.apache.hadoop" % "hadoop-common" % "2.7.2",
    "org.yaml" % "snakeyaml" % "1.13"
  )
}

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

// https://groups.google.com/forum/?hl=en#!activity/liftweb/Um5ghzYMDUoJ/liftweb/DDTzzxRbCNU/qEo0lIbTv4kJ
// needed for javaMail 1.4.4
resolvers += "Java.net Maven2 Repo" at "http://download.java.net/maven/2/"

// only show warnings and errors on the screen for all tasks (the default is Info)
//  individual tasks can then be more verbose using the previous setting
logLevel := Level.Info
