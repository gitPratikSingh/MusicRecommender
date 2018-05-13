
import java.io.{DataInputStream, File, FileInputStream}

import org.apache.spark.SparkContext

object SongLibrary {
  def read(library: String, sc: SparkContext, minTime: Long = 0, maxTime: Long = 20000) = {
    sc binaryFiles library filter { case (file, stream) =>
      file.endsWith(".wav")
    } map { case (file, stream) =>
      val fileName = new File(file).getName
      val audio = Song.processSong(stream.open(), minTime, maxTime)
      (fileName, audio)
    }
  }
  

  def readFile(song: String, minTime: Long = 0, maxTime: Long = Long.MaxValue) = {
    val is = new DataInputStream(new FileInputStream(song))
    Song.processSong(is, minTime, maxTime)
  }
}
