package com.uwekorn

import java.io.{ BufferedReader, File, FileReader }
import java.nio.file.Files
import java.sql.Timestamp
import net.tixxit.delimited._
import org.apache.avro.Schema
import org.apache.avro.generic.{ GenericData, GenericRecord }
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName

object ParquetAvro extends App {
  // Define the Avro schema. This is used to generate the Avro record.
  val schemaDefinition = """{
    "namespace": "com.uwekorn",
    "type": "record",
    "name": "TaxiTrip",
    "fields": [
      {"name": "VendorID", "type": "long"},
      {"name": "tpep_pickup_datetime", "type": "long", "logicalType": "timestamp-millis"},
      {"name": "tpep_dropoff_datetime", "type": "long", "logicalType": "timestamp-millis"},
      {"name": "passenger_count", "type": "long"},
      {"name": "trip_distance", "type": "double"},
      {"name": "pickup_longitude", "type": "double"},
      {"name": "pickup_latitude", "type": "double"},
      {"name": "RatecodeID", "type": "long"},
      {"name": "store_and_fwd_flag", "type": "boolean"},
      {"name": "dropoff_longitude", "type": "double"},
      {"name": "dropoff_latitude", "type": "double"},
      {"name": "payment_type", "type": "long"},
      {"name": "fare_amount", "type": "double"},
      {"name": "extra", "type": "double"},
      {"name": "mta_tax", "type": "double"},
      {"name": "tip_amount", "type": "double"},
      {"name": "tolls_amount", "type": "double"},
      {"name": "improvement_surcharge", "type": "double"},
      {"name": "total_amount", "type": "double"}
    ]
  }"""
  val schema = new Schema.Parser().parse(schemaDefinition)
  
  // Read in the CSV file using Tixxit. We construct a streaming reader for
  // a comma separated file.
  val inFilename = "data/yellow_tripdata_2016-01.csv"
  val parser: DelimitedParser = DelimitedParser(DelimitedFormat.CSV)
  val reader = new BufferedReader(new FileReader(inFilename))
  val rows: Iterator[Either[DelimitedError, Row]] = parser.parseReader(reader)
  
  // Gracefully delete the output file if it already exists.
  val filename = "test.parquet"
  Files.deleteIfExists(new File(filename).toPath())
  
  // Construct the parquet-avro writer from the above defined schema.
  val writer = AvroParquetWriter.builder[GenericRecord](new Path(filename))
      .withSchema(schema)
      .withCompressionCodec(CompressionCodecName.SNAPPY)
      .build()

  // First row is the header, drop it.
  rows.drop(1).foreach(_.fold(_ => println("There was an errenous row"), row => {
      val record = new GenericData.Record(schema)
      record.put("VendorID", row(0).toInt)
      record.put("tpep_pickup_datetime", Timestamp.valueOf(row(1)).getTime)
      record.put("tpep_dropoff_datetime", Timestamp.valueOf(row(2)).getTime)
      record.put("passenger_count", row(3).toInt)
      record.put("trip_distance", row(4).toDouble)
      record.put("pickup_longitude", row(5).toDouble)
      record.put("pickup_latitude", row(6).toDouble)
      record.put("RatecodeID", row(7).toInt)
      record.put("store_and_fwd_flag", row(8) == "Y")
      record.put("dropoff_longitude", row(9).toDouble)
      record.put("dropoff_latitude", row(10).toDouble)
      record.put("payment_type", row(11).toInt)
      record.put("fare_amount", row(12).toDouble)
      record.put("extra", row(13).toDouble)
      record.put("mta_tax", row(14).toDouble)
      record.put("tip_amount", row(15).toDouble)
      record.put("tolls_amount", row(16).toDouble)
      record.put("improvement_surcharge", row(17).toDouble)
      record.put("total_amount", row(18).toDouble)
      writer.write(record)
    }))
  writer.close()
}


// vim: set ts=2 sw=2 et sts=2:
