package com.xhochy

import java.io.InputStream
import scala.io.Source
import scala.xml.pull.{EvElemEnd, EvElemStart, EvText, XMLEventReader}

case class WikiArticle(title: String, text: String)

class WikiArticleIterator(val is: InputStream) extends Iterator[WikiArticle] {
  val xml = new XMLEventReader(Source.fromInputStream(is))
  var page:Option[WikiArticle] = nextPage()

  def nextPage():Option[WikiArticle] = {
    var inPage = false
    var textStarted = false
    var titleStarted = false
    val titleBuilder = new StringBuilder()
    val builder = new StringBuilder()
    while (xml.hasNext) {
      val event = xml.next()
      event match {
        case EvElemStart(_, "page", _, _) => {
          inPage = true
        }
        case EvElemEnd(_, "page") => {
          // We have parsed a page successfully
          return Some(WikiArticle(titleBuilder.toString, builder.toString))
        }
        case EvElemStart(_, "text", _, _) => {
          if (inPage) {
            textStarted = true
          }
        }
        case EvText(t) => {
          if (textStarted) {
           builder.append(t)
          } else if (titleStarted) {
            titleBuilder.append(t)
          }
        }
        case EvElemEnd(_, "text") => {
          if (textStarted) {
            textStarted = false
          }
        }
        case EvElemStart(_, "title", _, _) => {
          titleStarted = true
        }
        case EvElemEnd(_, "title") => {
          titleStarted = false
        }
        case _ => {}
      }
    }

    // On reaching this point we have exhausted the XML document.
    return None
  }

  // Proxy the state of the Option monad
  def hasNext = !page.isEmpty

  def next():WikiArticle = {
    val content = page.get
    page = nextPage()
    content
  }
}


// vim: set ts=2 sw=2 et sts=2:
