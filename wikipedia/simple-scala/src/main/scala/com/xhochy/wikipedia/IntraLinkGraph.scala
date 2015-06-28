package com.xhochy.wikipedia

import com.xhochy.LZ4Utils
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import scala.collection.mutable.HashMap

object IntraLinkGraph extends App {
  if (args.length < 3) {
    println("Need to specify at least the 3 files used")
    System.exit(1)
  }
  
  val dumpFile = args(0)
  val reverseIndexFile = args(1)
  val graphFile = args(2)

  println("## Loading reverseIndex")
  val br = LZ4Utils.openLZ4Reader(reverseIndexFile)
  val reverseIndex = new HashMap[(Int, String), Int]()
  var line:String = br._1.readLine()
  while (line != null) {
    val segments = line.split('|')
    val name = StringUtils.newStringUtf8(Base64.decodeBase64(segments(1)))
    reverseIndex += (((segments(0).toInt, name), segments(2).toInt))
    line = br._1.readLine()
  }
  // Close all streams NOW to save resources
  LZ4Utils.closeLZ4Reader(br)

  println("## Parsing mysqldump'd pagelinks table and writing out graph")
  val dump = new GZIPInputStream(new FileInputStream(dumpFile))
  val parser = new MySQLDumpParser(dump)
  val bw = LZ4Utils.openLZ4Writer(graphFile)

  parser.parseInsertInto(x => {
      val page:(Int, String) = (x(1).toInt, x(2))
      if (reverseIndex.contains(page)) {
        bw._1.write(x(0))
        bw._1.write('-')
        bw._1.write(reverseIndex(page).toString)
        bw._1.newLine()
      }
    })

  LZ4Utils.closeLZ4Writer(bw);
}

// vim: set ts=2 sw=2 et:
