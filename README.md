# IntelligentMusicRecommendationSystem
Recommends music content based on raw audio signal

The steps I plan to followed to implement this app are as follows:

1. Use Spark to process audio files stored in HDFS
2. Use Fourier Transform for audio signal transformation
3. Use Cassandra as a caching layer between online and offline layers
4. Use Pagerank as unsupervised recommendation algorithm
5. Integrate Spark-Job server with Play framework to build an end-to-end prototype

I will commit the codes incrementally as I go-on implementing these steps


# Existing solutions: 

## Collaborative filtering:
Using this approach, we leverage big data by collecting more information 
about the behavior of people. Although an individual is by definition unique, their shopping behavior 
is usually not, and some similarities can always be found with others. The recommended items will 
be targeted for a particular individual, but they will be derived by combining the user's behavior 
with that of similar users. This is the famous quote from most retail websites: 
	"People who bought this also bought that..."


#### Problem with this approach: 	
Of course, this requires prior knowledge about the customer, their past purchases and you must also have 
enough information about other customers to compare against. Therefore, a major limiting factor is that 
items must have been viewed at least once in order to be shortlisted as a potential recommended item. 
In fact, we cannot recommend an item until it has been seen/bought at least once.

## Content-based filtering:
An alternative approach, rather than using similarities with other users, involves looking at the product 
itself and the type of products a customer has been interested in in the past. If you are interested in both 
classical music and speed metal, it is safe to assume that you would probably buy (at least consider) any 
new albums mixing up both classical rhythms with heavy metal riffs. Such a recommendation would be difficult 
to find in a collaborative filtering approach as no one in your neighborhood shares your musical taste.

#### Problem with this approach:
The downside is that the model can be more difficult to build and selecting the right features with 
no loss of information can be challenging.


# My approach:
Recommend songs to end-users, by building a system that would recommend songs, not based on what people like or 
dislike, nor on the song attributes (genre, artist), but rather on how the song really sounds and how you feel 
about it? Its about really understanding the music signals.

