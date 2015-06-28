package com.xhochy.wikipedia

import java.io.{ BufferedWriter, FileInputStream, FileOutputStream, OutputStreamWriter }
import java.util.zip.GZIPInputStream
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream

object PageLinksTriple extends App {
  if (args.length < 2) {
    println("Need to specify at least the 2 files used")
  }

  val dumpFile = args(0)
  val triplesFile = args(1)
  
  val dump = new GZIPInputStream(new FileInputStream(dumpFile))
  val parser = new MySQLDumpParser(dump)
  
  println("## Parsing mysqldump'd pages table")
  val fos = new FileOutputStream(triplesFile)
  val bcos = new BZip2CompressorOutputStream(fos)
  val osw = new OutputStreamWriter(bcos)
  val bw = new BufferedWriter(osw)
  parser.parseInsertInto(x => {
      // from ID
      bw.write(x(0).toString)
      bw.write(',')
      // namespace TO
      bw.write(x(1).toString)
      bw.write(',')
      // Title TO
      bw.write(Base64.encodeBase64String(StringUtils.getBytesUtf8(x(2))))
      bw.newLine()
    })

  // Correctly close all streams in the order they were opened
  bw.close()
  osw.close()
  bcos.close()
  fos.close()
}


// vim: set ts=2 sw=2 et sts=2:
