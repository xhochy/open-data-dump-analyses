import sbt._

object Dependencies {
  lazy val hadoop_common = "org.apache.hadoop" % "hadoop-common" % "2.7.3"
  lazy val parquet_avro = "org.apache.parquet" % "parquet-avro" % "1.9.0"
  lazy val tixxit = "net.tixxit" %% "delimited-core" % "0.8.0"
}
