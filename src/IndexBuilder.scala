import com.datastax.spark.connector._
import com.typesafe.config.Config
import Config._
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.SparkContext
import spark.jobserver._


object IndexBuilder extends SparkJob {

  override def runJob(sc: SparkContext, conf: Config): Any = {}

  override def validate(sc: SparkContext, config: Config): SparkJobValidation = {}

}
