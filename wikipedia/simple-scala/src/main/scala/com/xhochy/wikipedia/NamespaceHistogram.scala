package com.xhochy.wikipedia

import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import scala.collection.mutable.HashMap

object NamespaceHistogram extends App {
  if (args.length < 1) {
    println("Need to specify at least the 1 file used")
  }

  val dumpFile = args(0)

  val dump = new GZIPInputStream(new FileInputStream(dumpFile))
  val parser = new MySQLDumpParser(dump)

  val map = new HashMap[Int,Int]
  
  println("## Parsing mysqldump'd pages table")
  parser.parseInsertInto(x => {
      val namespaceId = x(1).toInt
      if (map.contains(namespaceId)) {
        map(namespaceId) = map(namespaceId) + 1
      } else {
        map(namespaceId) = 1
      }
    })

  map.toList.sortBy(_._1).foreach(x => {
      println("% 3d -> %d".format(x._1, x._2))
    })
}
// vim: set ts=2 sw=2 et:
