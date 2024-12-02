scalaVersion := "2.13.15"


organization := "ch.epfl.scala"
version := "1.0"

resolvers += "Apache Spark Repository" at "https://repository.apache.org/content/repositories/snapshots/"


libraryDependencies += "org.apache.spark" %% "spark-core" % "3.3.0"  // Spark Core for Scala 2.13
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.3.0"  // Spark SQL
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.3.0"// Spark Streaming
libraryDependencies += "org.mongodb.spark" %% "mongo-spark-connector" % "10.1.1"  // MongoDB Spark Connector
libraryDependencies +="org.apache.spark" %% "spark-mllib" % "3.4.0" // Replace with your Spark version
libraryDependencies += "org.scalameta" %% "scalameta" % "4.7.8"






name := "TwitterStreamSimulator"






