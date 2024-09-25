package Part2

import java.sql.{Connection, DriverManager}

object MySQLConnection {

  // Update the MySQL connection details
  val url = "jdbc:mysql://localhost:3306/shoppingcartdb"   // JDBC URL
  val username = "root"                                    // Your MySQL username
  val password = "{password}"                              // Your MySQL password (replace with actual password)

  // Load the MySQL driver (optional for newer versions)
  Class.forName("com.mysql.cj.jdbc.Driver")

  // Function to connect to the database
  def getConnection: Connection = {
    DriverManager.getConnection(url,username, password)
  }

}
