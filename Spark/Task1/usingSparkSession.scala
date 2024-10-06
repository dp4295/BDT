package Spark

import org.apache.spark.sql.{SparkSession}

object usingSparkSession extends App{

  // Set up SparkSession
  val spark = SparkSession.builder()
    .appName("HighestTemperature")
    .master("local[*]")  // Set to 'local[*]' for local testing; remove for cluster usage
    .getOrCreate()

  // Read the text file as an RDD
  val rdd = spark.sparkContext.textFile("src/data/source/task1.txt")

  // Skip the first two lines manually and split the remaining lines by comma
  val filteredRdd = rdd.zipWithIndex().filter {
    case (_, index) => index > 1  // Skip the first two lines (header/comment lines)
  }.map(_._1)  // Extract only the lines without the index

  // Parse the lines to extract (SensorId, Date, Temp)
  val structuredRdd = filteredRdd.map { line =>
    val parts = line.split(",")
    val sensorId = parts(0)
    val date = parts(1)
    val temp = parts(2).toDouble
    (sensorId, date, temp)  // Return a tuple (SensorId, Date, Temp)
  }

  // Task 1: Find the highest temperature overall
  val highestTempOverall = structuredRdd.map(_._3).max()
  println(s"Highest Temperature Overall: $highestTempOverall")


  // Convert the RDD to a DataFrame for easier processing
  import spark.implicits._
  val df = structuredRdd.toDF("SensorId", "Date", "Temp")

  // Task 2: Find the highest temperature for each sensor
  val highestTempPerSensor = structuredRdd.map {
    case (sensorId, _, temp) => (sensorId, temp)
  }.reduceByKey((temp1, temp2) => Math.max(temp1, temp2))

  // Print the highest temperature for each sensor
  highestTempPerSensor.collect().foreach {
    case (sensorId, maxTemp) =>
      println(s"Sensor: $sensorId, Max Temperature: $maxTemp")
  }


  // Task 3: Count of temperature readings greater than 50 for each sensor
  val tempGreaterThan50Count = structuredRdd
    .filter { case (_, _, temp) => temp > 50 }  // Filter temperatures greater than 50
    .map { case (sensorId, _, _) => (sensorId, 1) }  // Map each sensor to (sensorId, 1)
    .reduceByKey(_ + _)  // Sum the counts for each sensor

  // Print the count of temperature readings greater than 50 for each sensor
  tempGreaterThan50Count.collect().foreach {
    case (sensorId, count) =>
      println(s"Sensor: $sensorId, Count of Temp > 50: $count")
  }

  // Stop the SparkSession
  spark.stop()

}
