package Part2.src

import Part2.MySQLConnection

import java.sql.{Connection, PreparedStatement, ResultSet, SQLException}
import scala.collection.mutable.ListBuffer

object itemManager {

  // Get available items from the database
  def getAvailableItemsFromDB(): List[Item] = {
    var connection: Connection = null
    var resultSet: ResultSet = null
    val availableItems = ListBuffer[Item]()

    try {
      connection = MySQLConnection.getConnection
      val query = "SELECT id, itemName, totalQuantity, perItemCost FROM items"
      val statement = connection.createStatement()
      resultSet = statement.executeQuery(query)

      while (resultSet.next()) {
        val item = new Item(
          resultSet.getInt("id"),
          resultSet.getString("itemName"),
          resultSet.getInt("totalQuantity"),
          resultSet.getDouble("perItemCost")
        )
        availableItems += item
      }
    } catch {
      case e: SQLException => e.printStackTrace()
    } finally {
      if (resultSet != null) resultSet.close()
      if (connection != null) connection.close()
    }

    availableItems.toList
  }

  // Get checked out items from the database
  def getCheckedOutItemsFromDB(): List[(String, Int, Double)] = {
    var connection: Connection = null
    var resultSet: ResultSet = null
    val checkedOutItems = ListBuffer[(String, Int, Double)]()

    try {
      connection = MySQLConnection.getConnection
      val query = "SELECT itemName, quantity, perItemCost FROM checked_out_items" // Assuming you store them in a separate table
      val statement = connection.createStatement()
      resultSet = statement.executeQuery(query)

      while (resultSet.next()) {
        val itemName = resultSet.getString("itemName")
        val quantity = resultSet.getInt("quantity")
        val perItemCost = resultSet.getDouble("perItemCost")

        // Adding the tuple (itemName, quantity, totalCost) to ListBuffer
        checkedOutItems += ((itemName, quantity, perItemCost * quantity))
      }
    } catch {
      case e: SQLException => e.printStackTrace()
    } finally {
      if (resultSet != null) resultSet.close()
      if (connection != null) connection.close()
    }

    checkedOutItems.toList
  }

  def insertCheckedOutItemToDB(itemName: String, quantity: Int, perItemCost: Double): Unit = {
    var connection: Connection = null
    var statement: PreparedStatement = null

    try {
      connection = MySQLConnection.getConnection
      val query = "INSERT INTO checked_out_items (itemName, quantity, perItemCost) VALUES (?, ?, ?)"
      statement = connection.prepareStatement(query)
      statement.setString(1, itemName)
      statement.setInt(2, quantity)
      statement.setDouble(3, perItemCost)

      statement.executeUpdate()
    } catch {
      case e: SQLException => e.printStackTrace()
    } finally {
      if (statement != null) statement.close()
      if (connection != null) connection.close()
    }
  }



  def addNewItemToDB(item: Item): String = {
    var connection: Connection = null
    var statement: PreparedStatement = null

    try {
      connection = MySQLConnection.getConnection
      val query = "INSERT INTO items (itemName, totalQuantity, perItemCost) VALUES (?, ?, ?)"
      statement = connection.prepareStatement(query)
      statement.setString(1, item.getItemName)
      statement.setInt(2, item.getTotalQuantity)
      statement.setDouble(3, item.getPerItemCost)

      val rowsInserted = statement.executeUpdate()
      if (rowsInserted > 0) {
        s"Item '${item.getItemName}' added successfully."
      } else {
        s"Failed to add item '${item.getItemName}'."
      }
    } catch {
      case e: SQLException => e.printStackTrace()
        s"Error adding item: ${e.getMessage}"
    } finally {
      if (statement != null) statement.close()
      if (connection != null) connection.close()
    }
  }

  // Update quantity of an existing item
  def updateItemInDB(itemId: Int, newQuantity: Int): String = {
    var connection: Connection = null
    var statement: PreparedStatement = null

    try {
      connection = MySQLConnection.getConnection
      val query = "UPDATE items SET totalQuantity = ? WHERE id = ?"
      statement = connection.prepareStatement(query)
      statement.setInt(1, newQuantity)
      statement.setInt(2, itemId)

      val rowsUpdated = statement.executeUpdate()
      if (rowsUpdated > 0) {
        s"Item with ID $itemId updated successfully."
      } else {
        s"No item found with ID $itemId."
      }
    } catch {
      case e: SQLException => e.printStackTrace()
        s"Error updating item: ${e.getMessage}"
    } finally {
      if (statement != null) statement.close()
      if (connection != null) connection.close()
    }
  }

  // Delete an item from the database
  def deleteItemFromDB(itemId: Int): String = {
    var connection: Connection = null
    var statement: PreparedStatement = null

    try {
      connection = MySQLConnection.getConnection
      val query = "DELETE FROM items WHERE id = ?"
      statement = connection.prepareStatement(query)
      statement.setInt(1, itemId)

      val rowsDeleted = statement.executeUpdate()
      if (rowsDeleted > 0) {
        s"Item with ID $itemId deleted successfully."
      } else {
        s"No item found with ID $itemId."
      }
    } catch {
      case e: SQLException => e.printStackTrace()
        s"Error deleting item: ${e.getMessage}"
    } finally {
      if (statement != null) statement.close()
      if (connection != null) connection.close()
    }
  }

  def checkOutFromDB(itemId: Int, quantity: Int): String = {
    var connection: Connection = null
    var statement: PreparedStatement = null
    var itemName: String = ""
    var perItemCost: Double = 0.0

    try {
      connection = MySQLConnection.getConnection

      // Retrieve the item details before updating
      val selectQuery = "SELECT itemName, perItemCost FROM items WHERE id = ?"
      statement = connection.prepareStatement(selectQuery)
      statement.setInt(1, itemId)
      val resultSet = statement.executeQuery()

      if (resultSet.next()) {
        itemName = resultSet.getString("itemName")
        perItemCost = resultSet.getDouble("perItemCost")
      } else {
        return "Item not found."
      }

      // Now update the quantity in the database
      val updateQuery = "UPDATE items SET totalQuantity = totalQuantity - ? WHERE id = ? AND totalQuantity >= ?"
      statement = connection.prepareStatement(updateQuery)
      statement.setInt(1, quantity)
      statement.setInt(2, itemId)
      statement.setInt(3, quantity)

      val rowsUpdated = statement.executeUpdate()
      if (rowsUpdated > 0) {
        // If quantity is updated successfully, insert checked out item into checked_out_items table
        insertCheckedOutItemToDB(itemName, quantity, perItemCost)
        s"Successfully checked out $quantity items of $itemName."
      } else {
        s"Not enough items available or item not found."
      }

    } catch {
      case e: SQLException => e.printStackTrace()
        s"Error checking out item: ${e.getMessage}"
    } finally {
      if (statement != null) statement.close()
      if (connection != null) connection.close()
    }
  }







}
