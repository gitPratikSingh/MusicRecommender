package edu

import com.typesafe.config.Config
import Config._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph
import spark.jobserver._

object PersonalizedPlaylistBuilder extends SparkJob with NamedRddSupport {
   /*	
   * 	Named Objects are a way to easily share RDDs, DataFrames or other objects among jobs. Using this facility, 
   * 	computed objects can be cached with a given name and later on retrieved
   * 
   * 	Without up-front information about a user's preferences, we can seek to collect our own information 
   * 	whenever a user plays a song, and hence personalize their playlist at runtime. To do this, I  
   * 	assume that the user was enjoying the previous song that they listened to.
   * */
  
  override def runJob(sc: SparkContext, conf: Config): Any = {
    val id = conf.getLong("song.id")

    val edges = this.namedRdds.get[Edge](RDD_EDGE).get
    val nodes = this.namedRdds.get[Node](RDD_NODE).get

    val songIdsB = sc.broadcast(nodes.map(n => (n.id, n.name)).collectAsMap())
    val edgeRDD = edges.flatMap({e =>
      e.targets.zip(e.weights).map({case (target, weight) =>
        org.apache.spark.graphx.Edge(e.source, target.toLong, weight.toDouble)
      })
    })
    
    
    val graph = Graph.fromEdges(edgeRDD, 0L)
    graph.cache()
    
    /*
     * PageRank and personalized PageRank are identical in the way that they compute scores 
     * (using the weight of incoming/outgoing edges), but the personalized version only allows 
     * users to teleport to the provided ID.
     * */

    val prGraph = graph.personalizedPageRank(id, TOLERANCE, TELEPORT)
    
    prGraph.vertices.mapPartitions({ it =>
      val songIds = songIdsB.value
      it map { case (vId, pr) =>
        (vId, songIds.getOrElse(vId, "UNKNOWN"), pr)
      }
    }).sortBy(_._3, ascending = false).map(v => List(v._1, v._3, v._2).mkString(",")).collect()

    
  }
  
  override def validate(sc: SparkContext, config: Config): SparkJobValidation = {
    if(!config.hasPath("song.id")) return SparkJobInvalid("Missing parameter [song.id]")
    if(this.namedRdds.get[Edge](RDD_EDGE).isEmpty) return SparkJobInvalid("Missing RDD [edges]")
    if(this.namedRdds.get[Edge](RDD_NODE).isEmpty) return SparkJobInvalid("Missing RDD [nodes]")
    SparkJobValid
  }

  
}