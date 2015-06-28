package com.xhochy.wikipedia

import java.io.InputStream
import scala.collection.mutable.{ ArrayBuffer, ListBuffer }

class MySQLDumpParser(val stream: InputStream) {

  def parseInsertInto(callback: (Seq[String]) => Unit) = {
    while (skipToInsertIntoValuesStart) {
      // States:
      // * [1]: We are parsing a tuple, '(' already read, not in a string.
      // * [2]: We are parsing a tuple, in a string.
      // * [3]: We closed a tuple and waiting for a new to open.
      var state = 1
      var buffer = new ArrayBuffer[Byte]()
      var stop = false
      var list = new ListBuffer[String]()
      while (!stop) {
        val lastChar = stream.read().toChar

        state match {
          case 1 => {
            lastChar match {
              case x if List(',', ')').contains(x) => {
                list += new String(buffer.toArray)
                buffer.clear()
                if (x == ')') {
                  state = 3
                  callback(list)
                  list = new ListBuffer[String]()
                }
              }
              case '\'' => {
                state = 2
                buffer.clear()
              }
              case x:Char => buffer += x.toByte
          }
        }
        case 2 => {
          lastChar match {
            // Escaped character, just read it
            case '\\' => buffer += stream.read().toByte
            case '\'' => state = 1
            case x:Char => buffer += x.toByte
          }
        }
        case 3 => {
          lastChar match {
            case '(' => state = 1
              case ';' => {
                stop = true
                // Skip this line
                while (stream.read().toChar != '\n') {}
              }
              case _ => { /* parse */ }
            }
          }
        }
      }
    }
  }

  // Skip the stream after the first '('.
  def skipToInsertIntoValuesStart:Boolean = {
    val insertBuffer = new Array[Byte](11)
    while (stream.available() > 0) {
      // len("INSERT INTO") == 11
      stream.read(insertBuffer)
      if (new String(insertBuffer) == "INSERT INTO") {
        // Skip until we can start
        var lastByte = stream.read()
        while (lastByte.toChar != '(' && lastByte > 0) {
            lastByte = stream.read()
        }
        return true
      } else {
        // Skip this line
        var lastByte = stream.read()
        while (lastByte.toChar != '\n' && lastByte > 0) {
          lastByte = stream.read()
        }
      }
    }
    false
  }
}

// vim: set ts=2 sw=2 et:
