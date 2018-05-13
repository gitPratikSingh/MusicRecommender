object Config {
  final val KEYSPACE = "music"
  final val TABLE_HASH = "hash"
  final val TABLE_RECORD = "record"
  final val TABLE_NODE = "nodes"
  final val TABLE_EDGE = "edges"
  final val MIN_TIME = 0
  final val MAX_TIME = 60000
  final val SAMPLE_SIZE = 50.0
  final val MIN_SIMILARITY = 0.0
  final val RDD_EDGE = "playlist:edges"
  final val RDD_NODE = "playlist:nodes"
  final val TOLERANCE = 0.001
  final val TELEPORT = 0.1
}
