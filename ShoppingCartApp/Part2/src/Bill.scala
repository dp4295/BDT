package Part2.src

class Bill(deliveryCharge: Double, customerName: String, address: String) {

  // Calculate the total cost of checked-out items
  def calculateTotalCost(checkedOutItems: List[(String, Int, Double)]): Double = {
    checkedOutItems.map { case (_, _, cost) => cost }.sum
  }

  // Display the bill
  def displayBill(checkedOutItems: List[(String, Int, Double)], availableItems: List[Item]): Unit = {
    println("-----------------------Bill------------------------")
    println(String.format("%-8s %-12s %-12s %-12s", "Sr.No", "Item", "Quantity", "Cost/Item"))

    var index = 1
    checkedOutItems.foreach {
      case (itemName, quantity, totalCost) =>
        println(f"$index%-8d $itemName%-12s $quantity%-12d $totalCost%-12.2f") // Item details line
        index += 1
    }

    val totalItemsCost = calculateTotalCost(checkedOutItems)
    val totalBillAmount = totalItemsCost + deliveryCharge

    println(s"\nTotal items cost: $totalItemsCost")
    println(s"Total Bill Amount: Total items cost + Delivery Charge is: $totalBillAmount")
    println(s"Name: $customerName")
    println(s"Address: $address")
    println("Have a nice day!!\n")

    // Display remaining items in store
    println("---------Remaining Quantity In Store-------------")
    println(String.format("%-8s %-12s %-12s %-12s", "Sr.No", "Item", "Quantity", "Cost/Item"))
    index = 1
    availableItems.foreach { item =>
      println(f"$index%-8d ${item.getItemName}%-12s ${item.getTotalQuantity}%-12d ${item.getPerItemCost}%-12.2f")
      index += 1
    }
  }

}
