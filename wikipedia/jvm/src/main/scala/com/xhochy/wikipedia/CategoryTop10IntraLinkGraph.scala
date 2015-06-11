package com.xhochy.wikipedia

import com.xhochy.LZ4Utils
import java.io.FileInputStream
import java.nio.{ ByteBuffer, ByteOrder }
import java.util.zip.GZIPInputStream
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import org.yaml.snakeyaml.Yaml
import scala.collection.JavaConverters._
import scala.collection.mutable.{ ArrayBuffer, HashMap, HashSet, PriorityQueue }

object CategoryTop10IntraLinkGraph extends App {

  if (args.length < 4) {
    println("Need to specify at least the 3 files used")
    System.exit(1)
  }
  
  val categoryFile = args(0)
  val reverseIndexFile = args(1)
  val pagelinksFile = args(2)
  val graphFile = args(3)

  println("## Loading categories and determing Top 10")
  val br2 = LZ4Utils.openLZ4Reader(categoryFile)
  val top10Cats = (new Yaml).load(br2._1)
    .asInstanceOf[java.util.Map[Int, Int]]
    .asScala.toList
    .sortBy(-_._2).take(10).map(_._1)
    .toSet
  // Close all streams NOW to save resources
  LZ4Utils.closeLZ4Reader(br2)


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

  println("## Parsing mysqldump'd pagelinks table to map to new vertex space")
  val pagelinksDump = new GZIPInputStream(new FileInputStream(pagelinksFile))
  val parser = new MySQLDumpParser(pagelinksDump)
  val ids = new HashSet[Int]()
  ids ++= top10Cats
  // val bw = LZ4Utils.openLZ4Writer(graphFile)
  parser.parseInsertInto(x => {
      val id = x(0).toInt
      if (top10Cats.contains(id)) {
        val page:(Int, String) = (x(1).toInt, x(2))
        if (reverseIndex.contains(page)) {
          ids += reverseIndex(page)
        }
      }
    })
  // LZ4Utils.closeLZ4Writer(bw);

  // Calculate new ID space
  val idMap = ids.zipWithIndex.toMap
  
  println("## Parsing mysqldump'd pagelinks table and writing out new graph")
  val pagelinksDump2 = new GZIPInputStream(new FileInputStream(pagelinksFile))
  val parser2 = new MySQLDumpParser(pagelinksDump2)
  val bw = LZ4Utils.openLZ4Writer(graphFile)
  parser2.parseInsertInto(x => {
      val id = x(0).toInt
      if (ids.contains(id)) {
        val page:(Int, String) = (x(1).toInt, x(2))
        if (reverseIndex.contains(page) && ids.contains(reverseIndex(page))) {
          bw._1.write(idMap(id).toString)
          bw._1.write('-')
          bw._1.write(idMap(reverseIndex(page)).toString)
          bw._1.newLine()
        }
      }
    })
  LZ4Utils.closeLZ4Writer(bw);

}

// vim: set ts=2 sw=2 et:
