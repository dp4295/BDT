package Part2.src

import Util.InputValidator

object StockManagerApp extends App {

  var continueManaging = true

  while (continueManaging) {
    println("\n--- Stock Management Interface ---")
    println("1. View Stock")
    println("2. Update Stock Quantity")
    println("3. Add New Item to Inventory")
    println("4. Delete Item from Inventory")
    println("5. Exit")

    val choice = InputValidator.readIntWithRetry("Choose an option (1-4): ")

    choice match {
      case 1 => viewStock()
      case 2 => updateStock()
      case 3 => addNewItem()
      case 4 => deleteItem()
      case 5 =>
        println("Exiting Stock Management Interface.")
        continueManaging = false
      case _ => println("Invalid option. Please choose a valid option.")
    }
  }

  // Function to view current stock
  def viewStock(): Unit = {
    val availableItems = itemManager.getAvailableItemsFromDB()
    println("\n--- Current Stock ---")
    println(String.format("%-8s %-12s %-12s %-12s", "Sr.No", "Item", "Quantity", "Cost/Item"))
    println("_________________________________________________")
    var index = 1
    availableItems.foreach { item =>
      println(f"$index%-8d ${item.getItemName}%-12s ${item.getTotalQuantity}%-12d ${item.getPerItemCost}%-12.2f")
      index += 1
    }
  }

  // Function to update stock for an existing item
  def updateStock(): Unit = {
    val availableItems = itemManager.getAvailableItemsFromDB()

    // Display the available items for the manager to select which one to update
    println("\n--- Update Stock ---")
    viewStock()

    val itemId = InputValidator.readIntWithRetry("Enter the item ID to update stock: ")
    availableItems.find(_.getItemId == itemId) match {
      case Some(item) =>
        val newQuantity = InputValidator.readIntWithRetry(s"Enter the new quantity for ${item.getItemName}: ")
        val result = itemManager.updateItemInDB(itemId, newQuantity)
        println(result)
      case None =>
        println("Item not found.")
    }
  }

  // Function to add a new item to inventory
  def addNewItem(): Unit = {
    val itemName = scala.io.StdIn.readLine("Enter the name of the new item: ")
    val totalQuantity = InputValidator.readIntWithRetry("Enter the total quantity: ")
    val perItemCost = InputValidator.readDoubleWithRetry("Enter the cost per item: ")

    val newItem = new Item(0, itemName, totalQuantity, perItemCost) // 0 for itemId as it's auto-generated
    val result = itemManager.addNewItemToDB(newItem)
    println(result)
  }

  // Function to delete an item from inventory
  def deleteItem(): Unit = {
    viewStock() // Show current stock so that the manager can see item IDs
    val itemId = InputValidator.readIntWithRetry("Enter the item ID to delete: ")

    // Confirm deletion
    var confirm = InputValidator.readYesNoWithRetry(s"Are you sure you want to delete item with ID $itemId? (Y/N): ")
    if (confirm == "y") {
      val result = itemManager.deleteItemFromDB(itemId)
      println(result)
    } else {
      println(s"Deletion of item with ID $itemId cancelled.")
    }
  }

}
