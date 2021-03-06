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
        case x => old(x)
    }
}

// set the name of the project
name := "wikipedia-dump-akka-streams"

version := "0.1.0"

scalaVersion := "2.11.7"

organization := "com.xhochy"

mainClass := Some("com.xhochy.MusicArticles")

libraryDependencies ++= {
  	Seq(
        "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
        "org.apache.commons" % "commons-compress" % "1.9",
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
        "org.scala-lang.modules" %% "scala-xml" % "1.0.4"
  	)
}

scalacOptions ++= Seq("-unchecked", "-deprecation")

fork in run := true
