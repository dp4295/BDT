package Spark.assignment2

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.functions.{col, regexp_extract, when}

/*
Assignment 
cleaning
1. check for outlier in length and width column
2. seperate product_number into two columns storeid and productid using _ as seperator.
3. seperate year from product_name in year column.


Transformation
1. Add a new column called product_size that categorizes products based on length.
if length is less than 1000 then small
else if 1000 to 2000 then Medium
else if 2000 to 3000 then Large
else Large
2. create pivot based on product_category and product_size and count the products
3. Using a window function to rank products within each category based on their length and display second long product.
 */
object assignment2 extends App{

  // Initialize SparkSession
  val spark = SparkSession.builder()
    .appName("Assignment2")
    .master("local[*]") // Use local mode for testing
    .getOrCreate()

  // Load data from CSV file
  val df = spark.read.option("header", "true").option("inferSchema", "true").csv("{path}/Amzon/1row/products.csv")

  // Display original DataFrame
  df.show(false)

  // Data Cleaning
  // 1. Check for outliers in length and width
  val dfOutliers = df.filter(col("length") < 0 || col("width") < 0 || col("length") > 10000 || col("width") > 500)
  println("Outliers in length and width columns:")
  dfOutliers.show()

  // 2. Split product_number into storeid and productid
  val dfWithProductNumber = df.withColumn("storeid", functions.split(col("product_number"), "_").getItem(0))
    .withColumn("productid", functions.split(col("product_number"), "_").getItem(1))

  // 3 Extract year from product_name
  val dfWithYear = dfWithProductNumber.withColumn("year", regexp_extract(col("product_name"), "^(\\d{4})", 1))

  // Display after cleaning
  dfWithYear.show(false)


  // Transformation
  // 1. Add a new column 'product_size' based on length
  val dfWithProductSize = dfWithYear.withColumn("product_size", when(col("length") < 1000, "Small")
    .when(col("length").between(1000, 2000), "Medium")
    .when(col("length").between(2000, 3000), "Large")
    .otherwise("Extra Large"))

  // Display after transformation
  dfWithProductSize.show(false)


  // 2 Create pivot based on product_category and product_size and count the products
  val pivotDF = dfWithProductSize.groupBy("product_category")
    .pivot("product_size")
    .count()

  // Display pivot table
  pivotDF.show(false)

  // 3 Use a window function to rank products within each category based on their length and display the second longest product
  val windowSpec = Window.partitionBy("product_category").orderBy(col("length").desc)

  val dfWithRank = dfWithProductSize.withColumn("rank", rank().over(windowSpec))
    .filter(col("rank") === 2)  // Filter for second longest product within each category

  // Display second longest product for each category
  dfWithRank.show(false)

  // Stop SparkSession
  spark.stop()
}
