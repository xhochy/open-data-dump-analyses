package com.xhochy.wikipedia

import com.xhochy.{ EdgeListToAdjacencyArray, LZ4Utils }
import org.yaml.snakeyaml.Yaml
import scala.collection.JavaConverters._

object CategoryLinkedArticleGraph extends App {
  if (args.length < 3) {
    println("Need to specify at least the 3 files used")
    System.exit(1)
  }
  
  val categoryFile = args(0)
  val graphFile = args(1)
  val outFile = args(2)
  
  println("## Loading categories")
  val br2 = LZ4Utils.openLZ4Reader(categoryFile)
  val categories = (new Yaml).load(br2._1)
    .asInstanceOf[java.util.Map[Int, Int]]
    .asScala.toList
    .map(_._1)
    .toSet
  // Close all streams NOW to save resources
  LZ4Utils.closeLZ4Reader(br2)

  println("## Pruning all non-category links")
  val bw = LZ4Utils.openLZ4Writer(outFile)
  EdgeListToAdjacencyArray.readEdgeList(graphFile, (u,v) => {
      if (categories.contains(u) || categories.contains(v)) {
        bw._1.write(u.toString)
        bw._1.write('-')
        bw._1.write(v.toString)
        bw._1.newLine()
      }
    })
  LZ4Utils.closeLZ4Writer(bw);
}

// vim: set ts=2 sw=2 et:
