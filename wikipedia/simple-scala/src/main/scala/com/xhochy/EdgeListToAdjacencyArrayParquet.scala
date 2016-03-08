package com.xhochy

import java.nio.{ ByteBuffer, ByteOrder }
import java.io.FileOutputStream
import org.apache.avro.generic.GenericData
import scala.collection.mutable.ArrayBuffer

object EdgeListToAdjacencyArrayParquet extends App {

  def readEdgeList(filename:String, func:(Int,Int) => Unit) = {
    val r = LZ4Utils.openLZ4Reader(filename)

    var line:String = r._1.readLine()
    while (line != null) {
      val x = line.split("(\\s+|-)")
      if (x.size == 2) {
        val u = x(0).toInt
        val v = x(1).toInt
        func(u, v)
      }
      line = r._1.readLine()
    }

    // Immediately close streams to save resources
    LZ4Utils.closeLZ4Reader(r)
  }

  if (args.length < 2) {
    println("Need to specify at least the 2 files used")
    System.exit(1)
  }

  val edgelistFile = args(0)
  val adjacencyArrayFile = args(1)


  println("## Determining the maximum vertex ID")
  var maxId = 0
  readEdgeList(edgelistFile, (u,v) => {
      maxId = List(maxId, u, v).max
    })

  println("## Writing out adjacency array graph")

  // Collect the number of edges per node
  val counts = Array.fill[Int](maxId + 1)(0)
  readEdgeList(edgelistFile, (u,v) => {
      counts(u) = counts(u) + 1
    })

  println("## Writing vertex indices")
  // Partion the number of vertices for which we read information from the
  // edge list to save memory.
  val range = (0 until (maxId + 1) by 1000000)
  val combinedRange = range.zip((range.toList ++ List(maxId + 1)).tail)

  println("## Writing edges")
  val index = new AdjacencyArraySchema()
  val schema = index.schema
  val writer = index.getWriter(adjacencyArrayFile)
  // Write the edges
  combinedRange.foreach((x) => {
      val minId = x._1
      val maxId = x._2
      val links = Array.fill[ArrayBuffer[Int]](maxId - minId)(new ArrayBuffer[Int])

      // Collect the neighbours for a vertex
      readEdgeList(edgelistFile, (u,v) => {
          if (minId <= u && u < maxId) {
            links(u - minId).append(v)
          }
        })

      links.zipWithIndex.foreach(x => {
          val neighbours = x._1
          val u = x._2 + minId
          val record = new GenericData.Record(schema)
          record.put("id", u)
          record.put("degree", neighbours.size)
          record.put("neighbours", neighbours.toArray)
          writer.write(record)
        })
    })

  writer.close()
}

// vim: set ts=2 sw=2 et sts=2:
