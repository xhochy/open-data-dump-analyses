package com.xhochy

import java.io.{ BufferedReader, BufferedWriter, FileInputStream, FileOutputStream, InputStreamReader, OutputStreamWriter }
import net.jpountz.lz4.{ LZ4BlockInputStream, LZ4BlockOutputStream }

object LZ4Utils {
  /**
   * Open a LZ4 compressed file for reading.
   *
   * @param filename The file to be read.
   * @return A 4-tuple of all readers that were instantiated. For reading only 
   *         _1 is needed but the others shall be closed at the end.
   */
  def openLZ4Reader(filename:String):(BufferedReader, InputStreamReader, LZ4BlockInputStream, FileInputStream) = {
    val fis = new FileInputStream(filename)
    val lis = new LZ4BlockInputStream(fis)
    val isr = new InputStreamReader(lis)
    val br = new BufferedReader(isr)
    (br, isr, lis, fis)
  }

  /**
   * Close the 4-tuple of readers previously instantiaded by openLZ4Reader.
   *
   * @param readers the 4-tuple returned by openLZ4Reader.
   */
  def closeLZ4Reader(readers:(BufferedReader, InputStreamReader, LZ4BlockInputStream, FileInputStream)) = {
    readers._1.close()
    readers._2.close()
    readers._3.close()
    readers._4.close()
  }

  /**
   * Open a LZ4 compressed file for writing.
   *
   * @param filename The file to be written.
   * @return A 4-tuple of all writerss that were instantiated. For writing only 
   *         _1 is needed but the others shall be closed at the end.
   */
  def openLZ4Writer(filename:String):(BufferedWriter, OutputStreamWriter, LZ4BlockOutputStream, FileOutputStream) = {
    val fos = new FileOutputStream(filename)
    val los = new LZ4BlockOutputStream(fos)
    val osw = new OutputStreamWriter(los)
    val bw = new BufferedWriter(osw)
    (bw, osw, los, fos)
  }
  
  /**
   * Close the 4-tuple of writers previously instantiaded by openLZ4Writer.
   *
   * @param writers the 4-tuple returned by openLZ4Writer.
   */
  def closeLZ4Writer(writers:(BufferedWriter, OutputStreamWriter, LZ4BlockOutputStream, FileOutputStream)) = {
    writers._1.close()
    writers._2.close()
    writers._3.close()
    writers._4.close()
  }
}

// vim: set ts=2 sw=2 et:
