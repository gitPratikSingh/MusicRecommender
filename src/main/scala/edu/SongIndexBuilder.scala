package edu

import com.datastax.spark.connector._
import com.typesafe.config.Config
import src.art.Config._
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import spark.jobserver._

object SongIndexBuilder{
  
  def main(args: Array[String]):Unit = {
    val conf = new SparkConf().setAppName("appName").setMaster("local[1]")
    new SparkContext(conf)
    print("hi")
  }
}