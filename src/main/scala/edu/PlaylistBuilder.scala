package edu

import com.datastax.spark.connector._
import com.typesafe.config.Config
import Config._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph
import spark.jobserver._
import org.apache.spark.storage.StorageLevel

object PlaylistBuilder extends SparkJob with NamedRddSupport{
    override def runJob(sc:SparkContext, jobConfig: Config): Any = {
      val recordRdd = sc.cassandraTable[Record](KEYSPACE, TABLE_RECORD)
      val hashRdd = sc.cassandraTable[Hash](KEYSPACE, TABLE_HASH)
      
      val minSimilarityB = sc.broadcast(MIN_SIMILARITY)
      val songIdsB = sc.broadcast(recordRdd.map(r => (r.id, r.name)).collectAsMap())
      
      //use Implicit Class to add methods to an object without modifying the source code of the object
      implicit class Crossable[X](xs: Traversable[X]) {
        def cross[Y](ys: Traversable[Y]) = for { x <- xs; y <- ys } yield (x, y)
      }
      
      // hashRdd = id: String, songs: List[Long]
      val songHashRdd = hashRdd.flatMap({case(hash) => 
        (hash.songs).map(song => ((hash, song), 1))
      })
      
      
      val songTfRdd = songHashRdd.map({case((hash, songId), count) => (songId, count)}).reduceByKey((x,y)=>x+y)
    
      // total hashes of any song
      val songTfB = sc.broadcast(songTfRdd.collectAsMap())
      
      // graph representation of all the similarities b/w songs
      // shape : val crossSongRdd: RDD[Edge[Double]]
      val crossSongRdd = songHashRdd.keys.groupByKey().values flatMap { songIds =>
        songIds cross songIds filter { case (from, to) =>
          from != to
        } map(_ -> 1)
      } reduceByKey(_+_) map { case ((from, to), count) =>
        val weight = count.toDouble / songTfB.value.getOrElse(from, 1)
        org.apache.spark.graphx.Edge(from, to, weight)
      } filter { edge =>
        edge.attr > minSimilarityB.value
      }
      
        
      val defaultStorageLevel=StorageLevel.MEMORY_ONLY

      val graph = Graph.fromEdges(crossSongRdd, 0L, defaultStorageLevel, defaultStorageLevel) 
      
      val prGraph = graph.pageRank(TOLERANCE, TELEPORT)
      
      val edges = prGraph.edges.map({ edge =>
      (edge.srcId, (edge.dstId, edge.attr))
      }).groupByKey().map({case (srcId, it) =>
        val dst = it.toList
        val dstIds = dst.map(_._1.toString)
        val weights = dst.map(_._2.toString)
        Edge(srcId, dstIds, weights)
      })
      
      val vertices = prGraph.vertices.mapPartitions({vertices => 
         val songIds = songIdsB.value
         vertices.map({case(vertix, priority) => Node(vertix, songIds.getOrElse(vertix, "UNKNOWN"), priority)})
      })
    }
    
    override def validate(sc:SparkContext, config: Config): SparkJobValidation = {
      SparkJobValid
    }
 
}