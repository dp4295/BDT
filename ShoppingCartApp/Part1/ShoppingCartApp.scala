package Part1

import Util.InputValidator
import Util.InputValidator.readYesNoWithRetry

import scala.io.StdIn.readLine

object ShoppingCartApp extends App{


  itemManager.addNewItem(new Item(1, "Biscuits", 5, 20.5))
  itemManager.addNewItem(new Item(2, "Cereals", 10, 90.0))
  itemManager.addNewItem(new Item(3, "Chicken", 20, 100.0))


  var continueShopping = true

  while (continueShopping) {

    displayMenu()

    val itemId = InputValidator.readIntWithRetry("What do you want to purchase? ")


    itemManager.getItems.find(_.getItemId == itemId) match {
      case Some(item) =>
        val availableQuantity = item.getTotalQuantity
        val quantity = InputValidator.readIntWithRetry(s"How many ${item.getItemName} packets do you want to purchase? Available stock: $availableQuantity ")
        if (quantity <= 0) {
          println("Invalid")
        } else if (quantity > availableQuantity) {
          println(s"Sorry, only $availableQuantity items are available.")
        } else {
          println(itemManager.checkOut(itemId, quantity)) // Process the checkout
        }

      case None =>
        println("Item not found.")
    }

    val continue = readYesNoWithRetry("Do you want to continue shopping? Y/N ")

    continueShopping = continue == "y"
  }

  // Gather user details and calculate delivery charge
  val customerName = readLine("Enter your name: ")
  val address = readLine("Enter your address: ")
  val distanceFromStore = readLine("Enter the distance from store (5/10/15/30): ").toInt

  val (deliveryCharge, distanceRange) = distanceFromStore match {
    case d if d <= 15 => (50, "up to 15 km")
    case d if d > 15 && d <= 30 => (100, "between 15 and 30 km")
    case _ =>
      println("No delivery available for distances greater than 30 km.")
      System.exit(1) // Exit the application if distance is greater than 30 km
      (0, "") // This line never reached but need to return to default case
  }

  println(s"Delivery charge: $deliveryCharge Rs will be levied for distance $distanceRange\n")

  // Create a Bill object and display the final bill
  val bill = new Bill(deliveryCharge, customerName, address)
  bill.displayBill(itemManager.getCheckedOutItems, itemManager.getAvailableItems)


  // Helper function to display menu
  def displayMenu(): Unit = {
    println("_________________________________________________")
    println(String.format("%-8s %-12s %-12s %-12s", "Sr.No", "Item", "Quantity", "Cost/Item"))
    println("_________________________________________________")

    itemManager.getItems.foreach { item =>
      println(f"${item.getItemId}%-8d ${item.getItemName}%-12s ${item.getTotalQuantity}%-12d ${item.getPerItemCost}%-12.2f")
    }
  }
}
