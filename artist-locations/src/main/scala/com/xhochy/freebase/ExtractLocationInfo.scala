package com.xhochy.freebase

import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import scalax.io.Resource

object ExtractLocationInfo extends App {
  if (args.length < 3) {
    println("Need to specify at least the 3 files used")
  }

  val dumpFile = args(0)
  val locationIdsFile = args(1)
  val locationInfoFIle = args(2)

  // Read IDs from file.
  val ids = Resource.fromInputStream(new GZIPInputStream(new FileInputStream(locationIdsFile))).lines().toSet
}
// vim: set ts=2 sw=2 et:
