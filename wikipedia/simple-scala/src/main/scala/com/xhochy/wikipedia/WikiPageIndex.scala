package com.xhochy.wikipedia

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.{AvroParquetReader, AvroParquetWriter}
import org.apache.parquet.hadoop.metadata.CompressionCodecName

class WikiPageIndex {
  val schemaDefinition = """{
    "namespace": "com.xhochy",
    "type": "record",
    "name": "WikiPageIndex",
    "fields": [
        {"name": "namespace_id", "type": "int"},
        {"name": "title",  "type": "string"},
        {"name": "id", "type": "int"}
    ]
  }"""
  val schema = new Schema.Parser().parse(schemaDefinition)

  def getWriter(filename:String) = {
    AvroParquetWriter.builder[GenericRecord](new Path(filename))
      .withSchema(schema)
      .withCompressionCodec(CompressionCodecName.SNAPPY)
      .build()
  }

  def getReader(filename:String) = {
    new AvroParquetReader[GenericRecord](new Path(filename))
  }

}

// vim: set ts=2 sw=2 et sts=2:
