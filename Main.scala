import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.rdd.RDD

object TwitterStreamSimulator {
  def main(args: Array[String]): Unit = {
    // MongoDB local connection URI
    val mongoUri = "mongodb://localhost:27017" // Local MongoDB URI

    // Create Spark Session and configure MongoDB connection
    val spark = SparkSession.builder()
      .appName("TwitterStreamSimulator")
      .master("local[*]")
      .config("spark.mongodb.write.connection.uri", mongoUri) // Set the local MongoDB URI
      .config("spark.mongodb.write.database", "twitterDB")    // Specify the database name
      .config("spark.mongodb.write.collection", "tweets")     // Specify the collection name
      .getOrCreate()

    // Create StreamingContext with a batch interval of 10 seconds
    val ssc = new StreamingContext(spark.sparkContext, Seconds(10))

    // Path to the dataset
    val filePath = "twitter_training.csv"

    // Read the entire dataset as a DataFrame
    val fullData = spark.read
      .option("header", "true")
      .csv(filePath)

    // Drop all columns except 'Tweet_ID' and 'Text'
    val filteredData = fullData.select("tweet")

    val rows = filteredData.collect() // Convert to an array for simulating chunks
    val chunkSize = 1000               // Number of rows per batch
    var offset = 0                    // Offset to track the chunk position

    // Create a queue to hold simulated RDDs
    val rddQueue = new scala.collection.mutable.Queue[RDD[String]]()

    // Add simulated chunks to the queue in a separate thread
    new Thread(() => {
      while (offset < rows.length) {
        // Extract the current chunk of rows
        val chunk = rows.slice(offset, offset + chunkSize)
        offset += chunkSize

        // Convert the chunk into an RDD and add it to the queue
        val chunkRdd = ssc.sparkContext.parallelize(chunk.map(_.toString))
        rddQueue.enqueue(chunkRdd)

        // Wait for the next batch interval (10 seconds)
        Thread.sleep(10000)
      }
    }).start()

    // Create a DStream from the queue
    val simulatedStream = ssc.queueStream(rddQueue)

    // Process the simulated stream
    simulatedStream.foreachRDD { rdd =>
      if (!rdd.isEmpty()) {
        import spark.implicits._
        
        val df = rdd.map(row => Map("tweets" -> row)).toDF()

        df.write
          .format("mongodb")
          .mode("append")
          .save()
      }
    }

    // Start Streaming
    ssc.start()
    ssc.awaitTermination()
  }
}
