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
name := "musicbrainz"

version := "0.1.0"

scalaVersion := "2.11.2"

organization := "com.xhochy"

mainClass := Some("com.xhochy.musicbrainz.Main")

libraryDependencies ++= {
  	Seq(
            "org.apache.hadoop" % "hadoop-common" % "2.5.1",
            "org.apache.hadoop" % "hadoop-mapreduce-client-app" % "2.5.1"
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