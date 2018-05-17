package recommender

case class Record(id: Long, name: String)
case class Hash(id: String, songs: List[Long])
case class Node(id: Long, name: String, popularity: Double)
case class Edge(source: Long, targets: List[String], weights: List[String])