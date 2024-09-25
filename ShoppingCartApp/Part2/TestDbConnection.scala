package Part2

object TestDbConnection extends App {

  try {
    val connection = MySQLConnection.getConnection
    println("Connection successful!")
    connection.close()
  } catch {
    case e: Exception => e.printStackTrace()
  }
}
