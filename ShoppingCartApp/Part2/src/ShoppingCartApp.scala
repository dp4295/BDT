package Part2.src

import Util.InputValidator
import Util.InputValidator.readYesNoWithRetry

object ShoppingCartApp extends App {

  var continueShopping = true

  while (continueShopping) {

    displayMenu()

    val itemId = InputValidator.readIntWithRetry("What do you want to purchase? ")

    val availableItems = itemManager.getAvailableItemsFromDB()

    availableItems.find(_.getItemId == itemId) match {
      case Some(item) =>
        val availableQuantity = item.getTotalQuantity
        val quantity = InputValidator.readIntWithRetry(s"How many ${item.getItemName} packets do you want to purchase? Available stock: $availableQuantity ")
        if (quantity <= 0) {
          println("Invalid")
        } else if (quantity > availableQuantity) {
          println(s"Sorry, only $availableQuantity items are available.")
        } else {
          println(itemManager.checkOutFromDB(itemId, quantity)) // Database checkout
        }

      case None =>
        println("Item not found.")
    }

    val continue = readYesNoWithRetry("Do you want to continue shopping? Y/N ")
    continueShopping = continue == "y"
  }

  // Gather user details and calculate delivery charge
  val customerName = scala.io.StdIn.readLine("Enter your name: ")
  val address = scala.io.StdIn.readLine("Enter your address: ")
  val distanceFromStore = scala.io.StdIn.readLine("Enter the distance from store (5/10/15/30): ").toInt

  val (deliveryCharge, distanceRange) = distanceFromStore match {
    case d if d <= 15 => (50, "up to 15 km")
    case d if d > 15 && d <= 30 => (100, "between 15 and 30 km")
    case _ =>
      println("No delivery available for distances greater than 30 km.")
      System.exit(1)
      (0, "")
  }

  println(s"Delivery charge: $deliveryCharge Rs will be levied for distance $distanceRange\n")

  // Fetch checked-out items and available items from the database
  val checkedOutItems = itemManager.getCheckedOutItemsFromDB()
  val availableItems = itemManager.getAvailableItemsFromDB()

  // Create a Bill object and display the final bill
  val bill = new Bill(deliveryCharge, customerName, address)
  bill.displayBill(checkedOutItems, availableItems)

  // Helper function to display menu
  def displayMenu(): Unit = {
    println("_________________________________________________")
    println(String.format("%-8s %-12s %-12s %-12s", "Sr.No", "Item", "Quantity", "Cost/Item"))
    println("_________________________________________________")

    itemManager.getAvailableItemsFromDB().foreach { item =>
      println(f"${item.getItemId}%-8d ${item.getItemName}%-12s ${item.getTotalQuantity}%-12d ${item.getPerItemCost}%-12.2f")
    }
  }


}
