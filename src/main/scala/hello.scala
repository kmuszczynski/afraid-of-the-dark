// import org.bytedeco.opencv.global.opencv_imgcodecs._
import java.io.File
import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem

//main apka, w najprostszy sposób, takie nic odpalane z konsoli

object hello extends App {
    println("Hello there")

    implicit val actorSystem = ActorSystem("AcSys")
    implicit val materializer = ActorMaterializer()

    //zerżnięte z książki "Scala Cookbook"
    //wbudowana metoda z javy

    def getListOfFiles(dir: String):List[File] = {
        val d = new File(dir)
        if (d.exists && d.isDirectory) {
            d.listFiles.filter(_.isFile).toList
        } else {
            List[File]()
        }
    }

    val images = getListOfFiles("images")
    // val images = getSeqOfFiles("images")

    val luminometer = new Luminometer

    // images.foreach(luminometer.measureBrightness)
    Source(images).runWith(Sink.foreachAsync(1)(luminometer.measureBrightness))

    println("General Kenobi")
}