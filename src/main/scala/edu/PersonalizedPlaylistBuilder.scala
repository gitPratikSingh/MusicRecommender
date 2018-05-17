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
   * */
  
  override def runJob(sc: SparkContext, conf: Config): Any = {
    val id = conf.getLong("song.id")
    val edges = this.namedRdds.get[Edge](RDD_EDGE).get
    val nodes = this.namedRdds.get[Node](RDD_NODE).get
    
    val songIdsB = sc.broadcast(nodes.map(n => (n.id, n.name)).collectAsMap())
    
  }
  
  override def validate(sc: SparkContext, config: Config): SparkJobValidation = {
    if(!config.hasPath("song.id")) return SparkJobInvalid("Missing parameter [song.id]")
    if(this.namedRdds.get[Edge](RDD_EDGE).isEmpty) return SparkJobInvalid("Missing RDD [edges]")
    if(this.namedRdds.get[Edge](RDD_NODE).isEmpty) return SparkJobInvalid("Missing RDD [nodes]")
    SparkJobValid
  }

  
}