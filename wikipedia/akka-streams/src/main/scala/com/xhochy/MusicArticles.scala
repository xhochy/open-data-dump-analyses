package com.xhochy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.io.{Framing, InputStreamSource}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import java.io.{BufferedWriter, ByteArrayOutputStream, File, FileInputStream, FileOutputStream, OutputStreamWriter}
import java.util.Base64
import org.apache.commons.compress.compressors.bzip2.{BZip2CompressorInputStream, BZip2CompressorOutputStream}
import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Source => IOSource}

object MusicArticles {
  val INFOBOX_ARTIST_START = "{{Infobox musical artist"
  val INFOBOX_ARTIST_END = "}}"

  def source(filename:String): Source[WikiArticle, Unit] = {
    val fis = new FileInputStream(filename)
    val bcis = new BZip2CompressorInputStream(fis)
    val iter = new WikiArticleIterator(bcis)
    Source(() => iter)
  }

  def compressContent(content: WikiArticle) = {
    val baos = new ByteArrayOutputStream()
    val bcos = new BZip2CompressorOutputStream(baos)
    bcos.write(content.text.getBytes)
    bcos.close()
    baos.toByteArray
  }

  def guessType(content: WikiArticle)(implicit ec: ExecutionContext):Future[Article] = {
    Future {
      if (content.text.contains(INFOBOX_ARTIST_START)) {
        new Article(content.title, ArticleType.Artist, compressContent(content))
      } else if (content.text.contains("{{Infobox album")) {
        new Article(content.title, ArticleType.Album, compressContent(content))
      } else if (content.text.contains("{{Infobox single")) {
        new Article(content.title, ArticleType.Song, compressContent(content))
      } else {
        new Article(content.title, ArticleType.Other, new Array[Byte](0))
      }
    }
  }

  def storeArticle(x:Int, article:Article) = {
    val filename = "articles/" +
      Base64.getUrlEncoder()
        .encodeToString(article.title.getBytes())
        .replace("/", "_") +
      ".txt.bz2"
    val fos = new FileOutputStream(filename)
    fos.write(article.compressedContent)
    fos.close()
    print(x.toString + "\r")
    x + 1
  }

  def main(args: Array[String]) {
    implicit val system = ActorSystem("music-articles")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val numCPUs = Runtime.getRuntime().availableProcessors()

    val sink = Sink.fold(0)(storeArticle)
    val src = source(args(0))
    val counter = src.mapAsyncUnordered(numCPUs)(guessType).filter(_.articleType == ArticleType.Artist).toMat(sink)(Keep.right)
    val sum: Future[Int] = counter.run()
    sum.andThen({
        case _ =>
          system.shutdown()
          system.awaitTermination()
      })
    sum.foreach(c => println(s"Total artist pages found: $c"))
  }
}


// vim: set ts=2 sw=2 et sts=2:
