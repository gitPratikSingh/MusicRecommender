import java.io.{BufferedInputStream, ByteArrayOutputStream, FileInputStream, InputStream}
import javax.sound.sampled.AudioSystem
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.{DftNormalization, FastFourierTransformer, TransformType}
import co.theasi.plotly._
import co.theasi.plotly.writer.Server


class Audio(val data: Array[Byte], val byteFreq: Int, val sampleRate: Float, val minTime: Long, val id: Int = 0) {

  def timeDomain(): Array[(Double, Int)] = {
    data.zipWithIndex.map{ case(b, idx) => (minTime + idx * 1000L / byteFreq.toDouble, b.toInt) }
  }

  def paddingToPowerOf2(data: Array[Byte]): Array[Byte] = {
    val n = math.ceil(math.log(data.length) / math.log(2))
    val optimal = math.pow(2, n).toInt
    val padding = Array.fill[Byte](optimal - data.length)(0)
    data ++ padding
  }

  def fft():Array[Complex]={
    val array = paddingToPowerOf2(data)
    val transformer = new FastFourierTransformer(DftNormalization.STANDARD)
    transformer.transform(array.map(_.toDouble), TransformType.FORWARD)
  }

  def duration: Double = (data.length + 1) * 1000L / byteFreq.toDouble

  override def toString = {
    s"data: ${data.length}, byteFreq: $byteFreq, sampleRate: $sampleRate, minTime: $minTime, duration: $duration, id: $id"
  }
}

object LoadSongs {

  def readFile(song:String): Audio = {
    val is = new FileInputStream(song)
    processSong(is, 0, 10)
  }

  def processSong(stream: InputStream, minTime: Long, maxTime: Long): Audio = {

    require(minTime >= 0)
    require(minTime < maxTime)

    val bufferedIn = new BufferedInputStream(stream)
    val out = new ByteArrayOutputStream
    val audioInputStream = AudioSystem.getAudioInputStream(bufferedIn)
    val format = audioInputStream.getFormat
    val sampleRate = format.getSampleRate
    val sizeTmp = Math.rint((format.getFrameRate * format.getFrameSize) / format.getFrameRate).toInt
    val size = (sizeTmp + format.getFrameSize) - (sizeTmp % format.getFrameSize)
    val byteFreq: Int = format.getFrameSize * format.getFrameRate.toInt

    println("Channels " + format.getChannels)
    println("Encoding " + format.getEncoding)
    println("FrameRate " + format.getFrameRate)
    println("FrameSize " + format.getFrameSize)
    println("SampleRate " + format.getSampleRate)
    println("SampleSizeInBits " + format.getSampleSizeInBits)
    println("byteFreq " + byteFreq)

    val buffer: Array[Byte] = new Array[Byte](size)
    val maxLength = if (maxTime == Long.MaxValue) Long.MaxValue else byteFreq * maxTime / 1000
    val minLength = byteFreq * minTime / 1000

    var available = true
    var totalRead = 0
    while (available) {
      val c = audioInputStream.read(buffer, 0, size)
      totalRead += c
      if (c > -1 && totalRead >= minLength && totalRead < maxLength) {
        out.write(buffer, 0, c)
      } else {
        if (totalRead >= minLength) {
          available = false
        }
      }
    }

    audioInputStream.close()
    out.close()

    new Audio(out.toByteArray, byteFreq, sampleRate, minTime)

  }



  def main(args:Array[String]): Unit ={

    val audioObject = readFile("src/Data/mario.wav")
    print(audioObject)
    print(audioObject.timeDomain().length)
    audioObject.timeDomain().filter(x => x._2>0).foreach(print)

    //plot()

  }

  def plot() = {
    implicit val server = new Server {
      val credentials = writer.Credentials("psingh22", "HblmXXPa72Kid2WN09wJ")
      val url = "https://api.plot.ly/v2/"
    }

    val p = Plot().withScatter(Vector(1, 2), Vector(3, 4))
    draw(p, "custom-credentials")
  }
}