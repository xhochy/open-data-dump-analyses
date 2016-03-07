package com.xhochy.wikipedia

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.{AvroSchemaConverter, AvroWriteSupport}
import org.apache.parquet.hadoop.ParquetWriter
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
  val avroSchema = new Schema.Parser().parse(schemaDefinition)
  val blockSize = 256 * 1024 * 1024
  val pageSize = 64 * 1024
  val compressionCodecName = CompressionCodecName.SNAPPY
  val parquetSchema = new AvroSchemaConverter().convert(avroSchema);
  val writeSupport = new AvroWriteSupport[GenericRecord](parquetSchema, avroSchema);

  def getWriter(filename:String) = {
    new ParquetWriter(new Path(filename), writeSupport, compressionCodecName, blockSize, pageSize);
  }

}

// vim: set ts=2 sw=2 et sts=2:
