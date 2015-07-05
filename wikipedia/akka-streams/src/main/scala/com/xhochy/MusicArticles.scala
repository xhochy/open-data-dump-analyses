package com.xhochy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.io.{Framing, InputStreamSource}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import java.io.{File, FileInputStream}
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Source => IOSource}

object ArticleType extends Enumeration {
  type ArticleType = Value
  val Artist, Song, Album, Other = Value
}


object MusicArticles {
  def source(filename:String): Source[WikiArticle, Unit] = {
    val fis = new FileInputStream(filename)
    val bcis = new BZip2CompressorInputStream(fis)
    val iter = new WikiArticleIterator(bcis)
    Source(() => iter)
  }

  def guessType(content: WikiArticle)(implicit ec: ExecutionContext):Future[ArticleType.Value] = {
    Future {
      if (content.text.contains("{{Infobox musical artist")) {
        ArticleType.Artist
      } else if (content.text.contains("{{Infobox album")) {
        ArticleType.Album
      } else if (content.text.contains("{{Infobox single")) {
        ArticleType.Song
      } else {
        ArticleType.Other
      }
    }
  }

  def main(args: Array[String]) {
    implicit val system = ActorSystem("music-articles")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val numCPUs = Runtime.getRuntime().availableProcessors()

    val sink = Sink.fold(0)((x:Int, y:ArticleType.Value) => {
        if ((x % 25) == 0) {
          println(x)
        }
        x + 1
      })
    val src = source(args(0))
    val counter = src.mapAsyncUnordered(numCPUs)(guessType).filter(_ == ArticleType.Artist).toMat(sink)(Keep.right)
    val sum: Future[Int] = counter.run()
    sum.foreach(c => println(s"Total artist pages found: $c"))
  }
}


// vim: set ts=2 sw=2 et sts=2: