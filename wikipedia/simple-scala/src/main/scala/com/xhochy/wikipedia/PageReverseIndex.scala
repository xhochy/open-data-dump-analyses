package com.xhochy.wikipedia

import com.xhochy.LZ4Utils
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils

object PageReverseIndex extends App {
  if (args.length < 2) {
    println("Need to specify at least the 2 files used")
  }

  val dumpFile = args(0)
  val reverseIndexFile = args(1)

  val dump = new GZIPInputStream(new FileInputStream(dumpFile))
  val parser = new MySQLDumpParser(dump)

  println("## Parsing mysqldump'd pages table")
  val bw = LZ4Utils.openLZ4Writer(reverseIndexFile)
  parser.parseInsertInto(x => {
      // namespaceID|title|id
      bw._1.write(x(1))
      bw._1.write('|')
      bw._1.write(Base64.encodeBase64String(StringUtils.getBytesUtf8(x(2))))
      bw._1.write('|')
      bw._1.write(x(0))
      bw._1.newLine()
    })

  // Correctly close all streams in the order they were opened
  LZ4Utils.closeLZ4Writer(bw)
}

// vim: set ts=2 sw=2 et:
