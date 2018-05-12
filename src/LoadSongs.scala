import java.io.{BufferedInputStream, ByteArrayOutputStream, FileInputStream}

import javax.sound.sampled.AudioSystem
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.{DftNormalization, FastFourierTransformer, TransformType}

class Audio(val data: Array[Byte], val byteFreq: Int, val sampleRate: Float, val minTime: Long, val id: Int) {

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

  def readFile(song:String): Unit = {
    val is = new FileInputStream(song)
    processSong(is)
  }

  def processSong(stream: FileInputStream): Unit = {
    val bufferedIn = new BufferedInputStream(stream)
    val out = new ByteArrayOutputStream
    val audioInputStream = AudioSystem.getAudioInputStream(bufferedIn)
    val format = audioInputStream.getFormat

    println(format.toString)
    // PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian

    val tmpSize = Math.rint((format.getFrameRate * format.getFrameSize)/format.getFrameSize).toInt
    val size = (tmpSize + format.getFrameSize) - (tmpSize % format.getFrameSize)
    val buffer = new Array[Byte](size)

    var available = true
    var readCount = 0

    while(available){
      val c = audioInputStream.read(buffer, 0, size)

      if(c > -1){
        readCount += c
        out.write(buffer, 0, c)
      }else{
        available = false
      }
    }

    audioInputStream.close()
    out.close()
    out.toByteArray

  }

  def main(args:Array[String]): Unit ={

    val audio = readFile("src/Data/mario.wav")
  }

  def test={

    val trace1 = Scatter(
      Seq(1, 2, 3, 4),
      Seq(10, 15, 13, 17)
    )

    val trace2 = Scatter(
      Seq(1, 2, 3, 4),
      Seq(16, 5, 11, 9)
    )

    val data = Seq(trace1, trace2)

    Plotly.plot("div-id", data)
  }
}