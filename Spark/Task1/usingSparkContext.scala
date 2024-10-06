import org.apache.spark.{SparkConf, SparkContext}

object Task extends App {

  // Set up SparkContext
  val conf = new SparkConf().setAppName("HighestTemp").setMaster("local[2]")
  val sc = new SparkContext(conf)

  // Read the input data
  val data = sc.textFile("src/data/source/task1.txt")

  // Skip the first two lines (The Header and Comment line)
  // Use zipWithIndex to assign line numbers, then filter out the first two lines (index 0 and 1)
  val filteredRdd = data.zipWithIndex().filter {
    case (line, index) => index > 1
  }.map(_._1)  // Remove the index, keep only the lines

  // Parse the lines to extract (SensorId, Temp)
  val parsedData = filteredRdd.map(line => {
    val parts = line.split(",")
    val sensorId = parts(0)
    val temp = parts(2).toDouble
    (sensorId, temp)  // Return a tuple (sensorId, temp)
  })

  // Result 1: Find the highest Temperature overall
  val highestTempOverall = parsedData.map(_._2).max()
  println(s"Highest Temperature Overall:  $highestTempOverall")

  // Result 2: Find the highest temperature per sensor
  val maxTempPerSensor = parsedData.reduceByKey((temp1, temp2) => Math.max(temp1, temp2))
  maxTempPerSensor.collect().foreach { case (sensorId, maxTemp) =>
    println(s"Sensor: $sensorId, Max Temperature: $maxTemp")
  }

  // Result 3: Count the number of Temperature reading greater than 50 for each sensor
  val tempGreaterThan50Count = parsedData
    .filter { case (_, temp) => temp > 50 } // Filter temperature greater than 50
    .map { case (sensorId, _) => (sensorId, 1) } // Map each sensor to (sensorId, 1)
    .reduceByKey(_+_) // Sum the counts for each sensor

  // Print the count of temperature reading greater than 50 for each sensor
  tempGreaterThan50Count.collect().foreach { case (sensorId, count) =>
    println(s"Sensor: $sensorId, Count of Temp > 50: $count")
  }

  // Stop the SparkContext
  sc.stop()
}

/**
 Input file
# Task 1: Highest Temp
 SensorId,Date,Temp
s1,2016-01-01,20.5
s2,2016-01-01,30.1
s1,2016-01-02,60.2
s2,2016-01-02,20.4
s1,2016-01-03,55.5
s2,2016-01-03,52.5
**/
