package com.xhochy.wikipedia

import com.xhochy.LZ4Utils
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import org.apache.avro.generic.GenericRecord
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
  val reverseIndex = new HashMap[(Int, String), Int]()
  val index = new WikiPageIndex()
  val reader = index.getReader(reverseIndexFile)
  var record:GenericRecord = reader.read()
  while (record != null) {
    record = reader.read()
    val title = record.get("title").asInstanceOf[String]
    val namespace = record.get("namespace_id").asInstanceOf[Int]
    val id = record.get("id").asInstanceOf[Int] 
    reverseIndex += (((namespace, title), id))
  }
  reader.close()

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
