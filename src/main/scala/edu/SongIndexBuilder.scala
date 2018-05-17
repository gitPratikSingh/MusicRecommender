package edu

import com.datastax.spark.connector._
import com.typesafe.config.Config
import src.art.Config._
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import spark.jobserver._

object SongIndexBuilder extends SparkJob{
  
  override def runJob(sc: SparkContext, conf: Config): Any = {
    val inputDir = conf.getString("input.dir")
    val SampleSizeB = sc.broadcast(SAMPLE_SIZE)
    val audioSongRdd = SongLibrary.read(inputDir, sc, MIN_TIME, MAX_TIME)
  }
  
  override def validate(sc: SparkContext, config: Config): SparkJobValidation = {

    if(!config.hasPath("input.dir")) {
      SparkJobInvalid("Missing parameter [input.dir]")
    } else {
      val hdfs = FileSystem.get(sc.hadoopConfiguration)
      val path = new Path(config.getString("input.dir"))
      val isDir = hdfs.isDirectory(path)
      val isValid = containsWav(hdfs, path)
      hdfs.close()
      if(isDir && isValid) {
        SparkJobValid
      } else {
        SparkJobInvalid("Input directory does not contains .wav files")
      }
    }

  }
 
  def containsWav(hdfs: FileSystem, path: Path) = {
    val it = hdfs.listFiles(path, false)
    var i = 0
    while(it.hasNext){
      if(it.next().getPath.getName.endsWith(".wav")){
        i += 1
      }
    }
    i > 0
  }
  
  def main(args: Array[String]):Unit = {
    val conf = new SparkConf().setAppName("appName").setMaster("local[1]")
    val sc = new SparkContext(conf)
    
    val inputDir = "Data"
    val audioSongRdd = SongLibrary.read(inputDir, sc, MIN_TIME, MAX_TIME)
    
    
    val SampleSizeB = sc.broadcast(SAMPLE_SIZE)
    
    //when you just want to transform the values and keep the keys as-is, it's recommended to use mapValues
    //if you applied any custom partitioning to your RDD (e.g. using partitionBy), using map would "forget" that paritioner (the result will revert to default partitioning) as the keys might have changed; mapValues, however, preserves any partitioner set on the RDD.
    
    val songRdd = audioSongRdd.keys.sortBy(Song => Song).zipWithIndex().mapValues(l => l+1)
    
    // There is no significant performance difference. collectAsMap simply collects RDD and creates a mutable HashMap on a driver
    val sMap = songRdd.collectAsMap()
    sMap.foreach(println(_))
    
    /* Broadcast this map 
     * (ChillingMusic.wav,1)
     * (mario.wav,3)
     * (KissesinParadise.wav,2)
     * */    
    
    val songIdsB = sc.broadcast(sMap)
    
    // convert songsRdd into RecordList
    val recordRdd = songRdd.map({case(song, id) => Record(id, song)}) 
    
    // convert audioSongRdd: RDD[(String, Audio)] into RDD[(Long, Audio)] and also partition the audios so that each song is present in one partition
    // mapPartitions() can be called for each partitions while map() and foreach() is called for each elements in an RDD
    // It runs one at a time on each partition or block of the Rdd, so function must be of type iterator<T>. It improves performance by reducing creation of object in map function.

    val audioRdd = audioSongRdd.mapPartitions({audios => 
      val songIds = songIdsB.value
      audios.map({case(song, audio) => (songIds.get(song).get, audio)})
    })
    
  /*    
   *    map(func)
   *    Return a new distributed dataset formed by passing each element of the source through a function func.
   *    
   *    flatMap(func)
   *    Similar to map, but each input item can be mapped to 0 or more output items (so func should return a Seq rather than a single item).
   *    
   *    The transformation function:
   *    map: One element in -> one element out.
   *    flatMap: One element in -> 0 or more elements out (a collection).
	*/  

    audioRdd.flatMap({case(songId, audio) => 
      audio.sampleByTime(SampleSizeB.value, true).map({case(sample) => (songId, sample)})  
    })
    
    print("audioSongRdd.count()" + audioSongRdd.count())
    
  }
}