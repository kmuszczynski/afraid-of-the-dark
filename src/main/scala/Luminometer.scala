import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.global.opencv_core._
import org.bytedeco.opencv.opencv_core._
import org.bytedeco.opencv.global.opencv_imgcodecs._
import java.io.File
import scala.math.pow
import scala.concurrent.Future

//pixelBrightnessThreshold - which pixels are deemed 'bright'
//percentThreshold - how much pixels need to be 'bright' for image to be considered 'bright enough'
//values obtained through trail and error
//
//it works, kinda; classification is correct for all pictures with the exception of bright/k.jpg - the blue one
//in calculation of 'brightness' blue's weight is very low, so entirely blue picture is considered almost as 'dark' as entirely black one
//hypothesis: it was selected intentionally for that very reason

class Luminometer(private var _threshold: Int = 50, private var _percent: Int = 80) {

    //pola, settery, gettery
    def pixelBrightnessThreshold: Int = _threshold

    def pixelBrightnessThreshold_=(thresh: Int): Unit = {
        _threshold = scala.math.max(0, thresh)
    }

    def percentThreshold: Int = _percent

    def percentThreshold_=(p: Int): Unit = {
        _percent = scala.math.max(0, p)
    }

    //bardzo łopatologiczne rozbicie ścieżki do pliku na elementy
    //jest tragicznie zrobione, nie tak powinno się to robić
    //ALE DZIAŁA, więc well

        def measureBrightness(f: File): Future[Unit] = {
        val s: String = f.getPath
        val sciezka = s.split("\\\\")
        val folder = sciezka.head
        val reszta = sciezka.tail.head
        val resztaSplit = reszta.split("\\.")
        val nazwa = resztaSplit.head
        val rozszerzenie = resztaSplit.tail.head
        val outputFolder = "output"

        val image: Mat = imread(s)
        val srcI = image.createIndexer().asInstanceOf[UByteIndexer]
        var jasne = 0;
        var ciemne = 0;

        //zawężenie obszaru badania obrazu do środkowego 80% x 80%

        val brg = new Array[Int](3)
        val yod = (0.1 * image.rows).toInt
        val ydo = (0.9 * image.rows).toInt
        val xod = (0.1 * image.cols).toInt
        val xdo = (0.9 * image.cols).toInt

        //przejdź po każdym pixelu w obrazku
        for (y <- yod until ydo) {
            for (x <- xod until xdo) {
                //begin_stolen from https://github.com/bytedeco/javacv-examples
                srcI.get(y, x, brg) //weź kolor
                val c = ColorRGB.fromBGR(brg)
                //end_stolen
                //if (dośćJasny) jasne++ else ciemne++
                if (howBright(c) > pixelBrightnessThreshold) jasne+=1 else ciemne+=1
            }
        }
        //To raczej oczywiste
        val procentJasnych = 100 - ((jasne / (jasne + ciemne).asInstanceOf[Double]) * 100)
        println(s"Jasne: $jasne   Ciemne: $ciemne")
        println(s"Procent jasnych: $procentJasnych")

        val isBright = if (procentJasnych < percentThreshold) "bright" else "dark"
        
        val nowaSciezka = outputFolder + "\\" + nazwa + "_" + procentJasnych.toInt + "_" + isBright + "." + rozszerzenie
        println(nowaSciezka)
        //val bool jest potrzebny bo
        //ta metoda ma nic nie zwracać, żeby mogła być Future[Unit] (Unit znaczy, że nic nie zwraca)
        //imwrite() zwraca boolean, a składnia jest taka, że gdyby nie było 'val bool'
        //to cała metoda by zwracała ten boolean
        val bool = imwrite(nowaSciezka,image)
        //to jest taki myk do programowania równoległego
        //apka nie czeka na wykonanie tej metody, więc za to musi być jakieś info czy w ogóle udało się ją wykonać
        //To całe jest STRASZNIE prostym przykładem, bardzo uproszczonym, dużo założeń
        //nie jest możliwe, żeby coś poszło nie tak, ale w normalnej apce jak najbardziej to pomocne
        if (bool) Future.successful() else Future.failed(new Exception())
    }

    //what proceeds was also stolen
    //https://stackoverflow.com/a/56678483 <- from here to be precise
    //I did not come up with all this by myself
    //though I do generally grasp the idea, now that I've read it

    //jakieś tam kalkulacje na temat tego jak 'jasny dla ludzkiego oka' jest pixel
    //w skrócie, trzeba cofnąć kodowanie gamma
    //potem zrobić średnią ważoną z kolorów;
    //niebieski jest lekki co jest faktycznym problemem, bo
    //zdjęcie człowieka ubranego na niebiesko na niebieskim tle jest przez algorytm uważane za bardzo ciemne
    //mimo, że jest dobrze oświetlone i widać co przedstawia

    private def howBright(color: ColorRGB): Double = {
        val r = lightness( luminance(color) )
        // println(s"final estimate: $r")

        return r
    }

    private def decodeGamma(d: Double): Double = {
        // println(s"Attempt decodeGamme($d)")
        if (d <= 0.04045) {
            return d / 12.92
        } else {
            return pow(((d+0.055)/1.055),2.4)
        }
    }

    private def lightness(d: Double): Double = {
        // println(s"Attempt lightness($d)")
        if (d <= (216/24389.0)) {
            return d * (23489/27.0)
        } else {
            return pow(d,(1/3.0)) * 116 - 16
        }
    }

    private def luminance(color: ColorRGB): Double = {
        // println("Attempt luminance")
        val red = color.red / 255.0
        val green = color.green / 255.0
        val blue = color.blue / 255.0
        // println(s"Colours: $red $green $blue")
        
        return 0.2126 * decodeGamma(red) + 0.7152 * decodeGamma(green) + 0.072 * decodeGamma(blue)
    }

}