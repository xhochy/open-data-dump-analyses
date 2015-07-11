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

object MusicArticles {
  val INFOBOX_ARTIST_START = "{{Infobox musical artist"
  val INFOBOX_ARTIST_END = "}}"

  val parser = new InfoboxArtistParser()

  def source(filename:String): Source[WikiArticle, Unit] = {
    val fis = new FileInputStream(filename)
    val bcis = new BZip2CompressorInputStream(fis)
    val iter = new WikiArticleIterator(bcis)
    Source(() => iter)
  }

  def guessType(content: WikiArticle)(implicit ec: ExecutionContext):Future[Article] = {
    Future {
      if (content.text.contains(INFOBOX_ARTIST_START)) {
        val infoboxStart = content.text.indexOfSlice(INFOBOX_ARTIST_START)
        val infoboxEnd = content.text.indexOfSlice(INFOBOX_ARTIST_END, infoboxStart) + INFOBOX_ARTIST_END.size
        val infobox = content.text.slice(infoboxStart, infoboxEnd)
        parser.parseAll(parser.box, infobox) match {
          case parser.Success(box, _) => {
            println(box)
            new ArtistArticle(content.title, List(""))
          }
          case x => {
            println("Could not parse the following artist infobox:")
            println(x)
            System.exit(0)
            new Article(content.title, ArticleType.Artist)
          }
        }
      } else if (content.text.contains("{{Infobox album")) {
        new Article(content.title, ArticleType.Album)
      } else if (content.text.contains("{{Infobox single")) {
        new Article(content.title, ArticleType.Song)
      } else {
        new Article(content.title, ArticleType.Other)
      }
    }
  }

  def main(args: Array[String]) {
    implicit val system = ActorSystem("music-articles")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val numCPUs = Runtime.getRuntime().availableProcessors()

    val sink = Sink.fold(0)((x:Int, y:Article) => {
        if ((x % 25) == 0) {
          println(x)
        }
        x + 1
      })
    val src = source(args(0))
    val counter = src.mapAsyncUnordered(numCPUs)(guessType).filter(_.articleType == ArticleType.Artist).toMat(sink)(Keep.right)
    val sum: Future[Int] = counter.run()
    sum.foreach(c => println(s"Total artist pages found: $c"))
  }
}


// vim: set ts=2 sw=2 et sts=2:
