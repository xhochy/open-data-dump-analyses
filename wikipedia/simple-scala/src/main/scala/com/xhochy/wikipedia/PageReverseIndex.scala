package com.xhochy.wikipedia

import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import org.apache.avro.generic.GenericData
import org.apache.parquet.avro.AvroParquetWriter


object PageReverseIndex extends App {
  if (args.length < 2) {
    println("Need to specify at least the 2 files used")
  }

  val dumpFile = args(0)
  val reverseIndexFile = args(1)

  val dump = new GZIPInputStream(new FileInputStream(dumpFile))
  val parser = new MySQLDumpParser(dump)

  println("## Parsing mysqldump'd pages table")
  val index = new WikiPageIndex()
  val schema = index.avroSchema
  val writer = index.getWriter(reverseIndexFile)
  parser.parseInsertInto(x => {
      val record = new GenericData.Record(schema)
      record.put("namespace_id", x(1).toInt)
      record.put("title", x(2))
      record.put("id", x(0).toInt)
      writer.write(record);
    })

  writer.close()
}

// vim: set ts=2 sw=2 et:
