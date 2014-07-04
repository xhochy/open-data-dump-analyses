package com.xhochy.freebase

import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import scala.collection.immutable.HashSet
import scalax.io.Resource

object ExtractLocationHierachy extends App {
  object Relation extends Enumeration {
    type Relation = Value
    val ContainedBy = Value
  }
  import Relation._

  if (args.length < 3) {
    println("Need to specify at least the 3 files used")
  }

  val dumpFile = args(0)
  val locationIdsFile = args(1)
  val locationHierachyFile = args(2)

  val relevantRelations = HashSet(
    "http://rdf.freebase.com/ns/location.location.containedby"
  )

  // Read IDs from file.
  println("## Reading location IDs from file.")
  val ids = Resource.fromInputStream(new GZIPInputStream(new FileInputStream(locationIdsFile))).lines().toSet

  // Extract the relevant information from the freebase dump
  println("## Parsing the freebase dump.")
  val relevantLines = Resource.fromInputStream(new GZIPInputStream(new FileInputStream(dumpFile)))
    .lines()
    // .take(10000000)
    .filter(l => {
      // Check if the line is about an ID we care about.
      if (l.startsWith("<http://rdf.freebase.com/ns/m.") && ids.contains(l.slice(30, l.indexOf(">")))) {
        val relation = l.slice(l.indexOf("<", l.indexOf(">") + 1) + 1, l.indexOf(">", l.indexOf(">") + 1))
        // Check that the line is about a relevant relation.
        relevantRelations.contains(relation)
      } else false
    }).map(l => {
      val id = l.slice(30, l.indexOf(">"))
      // <http://rdf.freebase.com/ns/m.026v7z>   <http://rdf.freebase.com/ns/location.location.containedby> <http://rdf.freebase.com/ns/m.04jpl>  .
      val container = l.slice(l.indexOf("<", l.indexOf("<", 2) + 1) + 30, l.lastIndexOf(">"))
      (id, container)
    }).toList

  println("## Building location hierachy")
  val containers = ids.map((_, List())).toMap ++ relevantLines.groupBy(_._1).map(x => (x._1, x._2.map(_._2)))
  val containerHistogram = containers.map(_._2.length).groupBy(x => x).map(x => (x._1, x._2.size)).toList.sortBy(_._1)
  println("## Histogram of direct neighbours: " + containerHistogram.toString)

  println("## Calculating hierachy line for each location")
  // TODO

  println(relevantLines.length)
}
// vim: set ts=2 sw=2 et:
