package com.xhochy.wikipedia

import com.xhochy.LZ4Utils
import java.io.FileInputStream
import java.nio.{ ByteBuffer, ByteOrder }
import java.util.zip.GZIPInputStream
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import org.yaml.snakeyaml.Yaml
import scala.collection.JavaConverters._
import scala.collection.mutable.HashMap

object CategoryHistogram extends App {
  if (args.length < 5) {
    println("Need to specify at least the 1 file used and the translated name for 'Category'")
    System.exit(1)
  }

  val dumpFile = args(0)
  val reverseIndexFile = args(1)
  val pagelinksFile = args(2)
  val categoryFile = args(3)
  val categoryNamesFile = args(4)

  val fis = new FileInputStream(dumpFile)
  val dump = new GZIPInputStream(fis)
  val parser = new MySQLDumpParser(dump)

  val categoryCount = new HashMap[Int, Int]
  val categoryNames = new HashMap[Int, String]

  println("## Parsing mysqldump'd pages table to search for categories")
  parser.parseInsertInto(x => {
      val id = x(0).toInt
      if (x(1).toInt == 14) {
        categoryCount(id) = 0
        categoryNames(id) = x(2)
      }
    })

  // Cleanup pages-dump resources
  dump.close()
  fis.close()

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

  println("## Parsing mysqldump'd pagelinks table")
  val pagelinksDump = new GZIPInputStream(new FileInputStream(pagelinksFile))
  val pagelinksParser = new MySQLDumpParser(pagelinksDump)

  pagelinksParser.parseInsertInto(x => {
      val id = x(0).toInt
      if (categoryCount.contains(id)) {
        val page:(Int, String) = (x(1).toInt, x(2))
        if (reverseIndex.contains(page)) {
          categoryCount(id) += 1
        }
      }
    })

  {
    val bw = LZ4Utils.openLZ4Writer(categoryFile)
    (new Yaml()).dump(categoryCount.asJava, bw._1)
    LZ4Utils.closeLZ4Writer(bw);
  }
  {
    val bw = LZ4Utils.openLZ4Writer(categoryNamesFile)
    (new Yaml()).dump(categoryNames.asJava, bw._1)
    LZ4Utils.closeLZ4Writer(bw);
  }
}

// vim: set ts=2 sw=2 et:
