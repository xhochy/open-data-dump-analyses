package com.xhochy

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.{AvroParquetReader, AvroParquetWriter}
import org.apache.parquet.hadoop.metadata.CompressionCodecName

class AdjacencyArraySchema {
  val schemaDefinition = """{
    "namespace": "com.xhochy",
    "type": "record",
    "name": "AdjacencyArray",
    "fields": [
      {"name": "id", "type": "int"},
      {"name": "degree", "type": "int"},
      {"name": "neighbours", "type": {"type": "array", "items": "int"}}
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
