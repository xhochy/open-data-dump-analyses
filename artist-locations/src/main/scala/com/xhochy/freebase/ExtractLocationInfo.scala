package com.xhochy.freebase

import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import scala.collection.immutable.HashSet
import scalax.io.Resource

object ExtractLocationInfo extends App {
  object Relation extends Enumeration {
    type Relation = Value
    val ContainedBy, Name, Type = Value
  }
  import Relation._

  if (args.length < 3) {
    println("Need to specify at least the 3 files used")
  }

  val dumpFile = args(0)
  val locationIdsFile = args(1)
  val locationInfoFile = args(2)

  val relevantRelations = HashSet(
    "http://rdf.freebase.com/ns/type.object.name",
    "http://rdf.freebase.com/ns/location.location.containedby",
    "http://rdf.freebase.com/ns/type.object.type"
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
      val relation = l.slice(l.indexOf("<", l.indexOf(">") + 1) + 1, l.indexOf(">", l.indexOf(">") + 1))
      if (relation == "http://rdf.freebase.com/ns/type.object.name") {
        //  <http://rdf.freebase.com/ns/m.03d_2z>   <http://rdf.freebase.com/ns/type.object.name>   "Bloc Party"@id .
        (id, Name, l.slice(l.indexOf(">", l.indexOf(">") + 1) + 1, l.lastIndexOf(".")).trim)
      } else if (relation == "http://rdf.freebase.com/ns/location.location.containedby") {
        // <http://rdf.freebase.com/ns/m.026v7z>   <http://rdf.freebase.com/ns/location.location.containedby> <http://rdf.freebase.com/ns/m.04jpl>  .
        val container = l.slice(l.indexOf("<", l.indexOf("<", 2) + 1) + 30, l.lastIndexOf(">"))
        (id, ContainedBy, container)
      } else {
        // relation == http://rdf.freebase.com/ns/type.object.type
        // <http://rdf.freebase.com/ns/m.04jpl>    <http://rdf.freebase.com/ns/type.object.type>   <http://rdf.freebase.com/ns/rail.railway_terminus>  .
        val typeName = l.slice(l.indexOf("<", l.indexOf("<", 2) + 1) + 28, l.lastIndexOf(">"))
        (id, Type, typeName)
      }
    }).toList

  println("## Extracting location names")
  val names = relevantLines.filter(_._2 == Name).groupBy(_._1).flatMap(x => {
      val languages = x._2.map(y => {
          // (language, name)
          val language = y._3.slice(y._3.lastIndexOf("@") + 1, y._3.length)
          val name = y._3.slice(1, y._3.lastIndexOf("@") - 1)
          (language, name)
        }).toMap
      if (languages.contains("en")) {
        // First choice: English
        List[(String, String)]((x._1, languages("en")))
      } else if (languages.contains("de")) {
        // Second choice: German
        List[(String, String)]((x._1, languages("de")))
      } else {
        // Third choice: Just take anything
        if (languages.size > 0) {
          List[(String, String)]((x._1, languages.head._2))
        } else {
          List[(String, String)]()
        }
      }
    })

  println("## Extracting location types")
  val types = relevantLines.filter(_._2 == Type).groupBy(_._1).map(x => (x._1, x._2.map(_._3)))

  println("## Building location hierachy")
  // TODO

  println("## Calculating hierachy line for each location")
  // TODO

  println(relevantLines.length)
}
// vim: set ts=2 sw=2 et:
