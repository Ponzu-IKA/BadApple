package amaiice.badapple

import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread
import kotlin.math.roundToInt

// This is the main entry point of the application.
// It uses the `Printer` class from the `:utils` subproject.
class BadApple(private val charSet: CharSet) {

    companion object {
        const val WIDTH = 959
        const val HEIGHT = 719
        const val SIZE = 9
    }
    fun readImage(path: String): String {
        var imageString = ""
        val bufferedImage = ImageIO.read(File(path))
        for (y in 0..HEIGHT step SIZE) {
            for (x in 0..WIDTH step SIZE){
                val image = bufferedImage.getRGB(x,y)
                imageString += when {
                    -2000000 <= image -> charSet.white
                    -9000000 <= image-> charSet.gray
                    -15000000 <= image -> charSet.darkGray
                    else -> charSet.black
                }
            }
            imageString += charSet.lineBreak
        }
        return imageString
    }
}

fun func(file: List<String>) {
    val sound = AudioSystem.getAudioInputStream(File("badapple.wav"))
    val sLine = AudioSystem.getLine(DataLine.Info(SourceDataLine::class.java, sound.format)) as SourceDataLine

    sLine.open()
    thread {
        sLine.start()

        val data = ByteArray(sLine.bufferSize)
        var size = 0
        while(0 <= size ) {
            sLine.write(data, 0, size)
            size = sound.read(data)
        }
        sLine.drain()
        sLine.stop()
        sLine.close()
    }
    val framePerNanoSec = (1000_000_000/60).toLong()
    var prevFrame:Long = framePerNanoSec
    var playTime = System.nanoTime()
    for(i in file.indices) {
        val currentTime = System.nanoTime()
        replace(file[i], CharSet("\u001b[00;107m","\u001b[01;47m","\u001b[01;100m","\u001b[00;40m","\u001B[00m\n"))
        print("${(1.0 / (currentTime - prevFrame)*1000_000_000_0).roundToInt()/10.0} FPS/")
        print("nowFrame: $i/ ${file.size}\u001b[${file.size+1}A\u001b[G")
        playTime += framePerNanoSec
        TimeUnit.NANOSECONDS.sleep(framePerNanoSec-(currentTime - playTime))
        prevFrame = currentTime
    }
}

fun replace(str: String, charSet: CharSet) {
    //str.replace("0", charSet.white).replace("1", charSet.gray).replace("2", charSet.darkGray).replace("3", charSet.black).replace("\n", charSet.lineBreak) + charSet.lineBreak

    var enc = ""
    str.chunked(BadApple.WIDTH/BadApple.SIZE+1).forEach {
            enc += when(it[0]) {
                '0' -> charSet.white
                '1' -> charSet.gray
                '2' -> charSet.darkGray
                '3' -> charSet.black
                else -> "??"
            }

        for(i in it.indices) {
            enc += "  "

            if (i < it.length-1) {
                if(it[i] != it[i+1]) {
                    enc += when(it[i+1]) {
                        '0' -> charSet.white
                        '1' -> charSet.gray
                        '2' -> charSet.darkGray
                        '3' -> charSet.black
                        else -> ""
                    }
                }
            }
        }

        enc += charSet.lineBreak
    }


    print(enc)
}

fun main(args: Array<String>) {
    val fileIO = FlatFileIO()
    val badApple = BadApple(CharSet("0", "1", "2", "3", ""))

    if(args.isNotEmpty())
        if(args[0] == "--encode"){
            val imagePaths = File("movie").list()!!

            val letter = imagePaths.mapIndexed { index, path ->
                print("Processing...: ${(index.toDouble() / imagePaths.size * 1000).roundToInt() / 10.0}%\t$path\r")
                badApple.readImage("movie/${path}").replace("\n", "")
            }.joinToString("")
            println("\n\ndone.")
            println("LetterCompressing...")
            fileIO.compress(letter.toByteArray())
            println("done.")
        } else if (args[0] == "--test") {
            val width = BadApple.WIDTH/BadApple.SIZE+1
            println(width)
            println(badApple.readImage("test.png").chunked(width).joinToString("\n"))
            return
        }
    val file = fileIO.open().decodeToString()
    func(file.chunked((BadApple.WIDTH/BadApple.SIZE+1)*(BadApple.HEIGHT/BadApple.SIZE+1)))
}