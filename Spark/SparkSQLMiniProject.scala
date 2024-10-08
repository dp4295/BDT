package Spark.miniProject

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{count, countDistinct, desc, row_number, sum}

import java.util.Properties

// Case class for JDBC connection properties
case class jdbcConfig(url: String, user: String, password: String)

object jdbcConfig {
  // load JDBC config from the application.conf
  def load(): jdbcConfig = {
    val config = ConfigFactory.load()
    jdbcConfig(
      url = config.getString("db.url"),
      user = config.getString("db.user"),
      password = config.getString("db.password")
    )
  }
}

object SparkSQLMiniProject {

  def main(args: Array[String]): Unit = {

    // Initialize Spark Session
    val spark = SparkSession.builder
      .appName("SparkSQLMiniProject")
      .config("spark.master", "local")
      .getOrCreate()

    // Load Configuration
    val JdbcConfig = jdbcConfig.load()

    // Load table name from application.config
    val config = ConfigFactory.load()
    val membersTable = config.getString("db.tables.members")
    val menuTable = config.getString("db.tables.menu")
    val salesTable = config.getString("db.tables.sales")

    // Set up connection properties
    val connectionProperties = new Properties()
    connectionProperties.put("user", JdbcConfig.user)
    connectionProperties.put("password", JdbcConfig.password)

    // Read members table from the database
    val membersDF = spark.read
      .jdbc(JdbcConfig.url, membersTable, connectionProperties)
    membersDF.show()

    // Read menu table from the database
    val menuDF = spark.read
      .jdbc(JdbcConfig.url, menuTable, connectionProperties)
    menuDF.show()

    // Read Sales table from the database
    val salesDF = spark.read
      .jdbc(JdbcConfig.url, salesTable, connectionProperties)
    salesDF.show()

//    Query 1: Total amount each customer spent at the restaurant
    val totalSpentDF = salesDF
      .join(menuDF, salesDF("product_id") === menuDF("product_id"))
      .groupBy(salesDF("customer_id"))
      .agg(sum(menuDF("price")).as("total_amount"))
    totalSpentDF.show()

//     Query 2: Number of days each customer visited the restaurant
    val visitDaysDF = salesDF
      .groupBy("customer_id")
      .agg(countDistinct("order_date").as("visit_days"))
    visitDaysDF.show()

//    Query 3: First item from the menu purchased by each customer
    val windowSpec = Window.partitionBy(salesDF("customer_id")).orderBy("order_date")
    val firstItemDF = salesDF
      .join(menuDF, salesDF("product_id") === menuDF("product_id"))
      .withColumn("rn", row_number().over(windowSpec))
      .filter("rn = 1")
      .select("customer_id", "product_name", "order_date")
    firstItemDF.show()

//    Query 4: Most purchased item on the menu by all customers
    val mostPurchasedItemDF = salesDF
      .join(menuDF, salesDF("product_id") === menuDF("product_id"))
      .groupBy("product_name")
      .agg(count(salesDF("product_id")).as("purchase_count"))
      .orderBy(desc("purchase_count"))
      .limit(1)
    mostPurchasedItemDF.show()

//    Query 5: Most popular item for each customer
   val purchaseCountsDF = salesDF
  .groupBy("customer_id", "product_id")
  .agg(count("*").as("order_count"))

    val maxCountsDF = purchaseCountsDF
      .groupBy("customer_id")
      .agg(functions.max("order_count").as("max_order_count"))

    // Join the two DataFrames on customer_id and order_count = max_order_count
    val mostPopularItemDF = purchaseCountsDF
      .join(maxCountsDF,
        purchaseCountsDF("customer_id") === maxCountsDF("customer_id") &&
          purchaseCountsDF("order_count") === maxCountsDF("max_order_count"))
      .join(menuDF, purchaseCountsDF("product_id") === menuDF("product_id"))
      .select(purchaseCountsDF("customer_id"), menuDF("product_name"), purchaseCountsDF("order_count"))
    mostPopularItemDF.show()

  }

}
