package com.xhochy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.io.{Framing, InputStreamSource}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import java.io.{BufferedWriter, File, FileInputStream, FileOutputStream, OutputStreamWriter}
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

  def guessType(content: WikiArticle)(implicit ec: ExecutionContext):Future[Article] = {
    Future {
      try {
        if (content.text.contains(INFOBOX_ARTIST_START)) {
          new Article(content.title, ArticleType.Artist, content.text)
        } else if (content.text.contains("{{Infobox album")) {
          new Article(content.title, ArticleType.Album, content.text)
        } else if (content.text.contains("{{Infobox single")) {
          new Article(content.title, ArticleType.Song, content.text)
        } else {
          new Article(content.title, ArticleType.Other, content.text)
        }
      } catch {
        case e:Exception => print(e)
        System.exit(1)
        new Article(content.title, ArticleType.Other, content.text)
      }
    }
  }

  def main(args: Array[String]) {
    implicit val system = ActorSystem("music-articles")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val numCPUs = Runtime.getRuntime().availableProcessors()

    val sink = Sink.fold(0)((x:Int, y:Article) => {
        print(x.toString + "\r")
        val filename = "articles/" + Base64.getUrlEncoder().encodeToString(y.title.getBytes()).replace("/", "_") + ".txt.bz2"
        println(filename)
        val fos = new FileOutputStream(filename)
        val bcos = new BZip2CompressorOutputStream(fos)
        bcos.write(y.text.getBytes)
        bcos.close()
        fos.close()
        x + 1
      })
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
