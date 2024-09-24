package Part1

import scala.collection.mutable


class Item () {

  private var itemId: Int = 0
  private var itemName: String = ""
  private var totalQuantity: Int = 0
  private var perItemCost: Double = 00.00

  def this(itemId: Int, itemName: String, totalQuantity: Int, perItemCost: Double) = {
    this()
    this.itemId = itemId
    this.itemName = itemName
    this.totalQuantity = totalQuantity
    this.perItemCost = perItemCost
  }


  // Getter
  def getItemId: Int = itemId
  def getItemName: String = itemName
  def getTotalQuantity: Int = totalQuantity
  def getPerItemCost: Double = perItemCost

  // Setter
  def setItemId(_itemId: Int): Unit = {
    itemId = _itemId
  }
  def setItemName(_itemName: String): Unit = {
    itemName = _itemName
  }
  def setTotalQuantity(_totalQuantity: Int): Unit = {
    totalQuantity = _totalQuantity
  }

  def setPerItemCost(_perItemCost: Double): Unit = {
    perItemCost = _perItemCost
  }

}

// Crete a Singleton Object, available during the application run time
object itemManager {

  private var itemSet: mutable.LinkedHashSet[Item] = mutable.LinkedHashSet()
  private var checkedOutItems: mutable.LinkedHashSet[(String, Int, Double)] = mutable.LinkedHashSet()

  /**
   * Add unique items. Compared with itemName for uniqueness.
   * @param item
   * @return Item added to the stock or Item already been added.
   */
  def addNewItem(item: Item): String = {
    itemSet.find(_.getItemName == item.getItemName) match {
      case Some(_) =>
        s"Item '${item.getItemName}' already exist in the stock. Use `updateStock` to modify stock "

      case None =>
        itemSet = itemSet + item
        s"Added item '${item.getItemName}' to the stock"
    }
  }

  def getItems: mutable.LinkedHashSet[Item] = itemSet

   def checkOut(itemId: Int, buyQuantity: Int): String = {
    itemSet.find(_.getItemId == itemId) match {
      case Some(item) =>
        if (item.getTotalQuantity >= buyQuantity) {
          // Record the checkout item
          checkedOutItems += ((item.getItemName, buyQuantity, item.getPerItemCost * buyQuantity))
          decrementQuantity(item.getItemName, buyQuantity)
          s"Successfully checked out $buyQuantity of '${item.getItemName}'."
        } else {
          "Not Enough Items"
        }
      case None =>
        "Item not found."
    }
  }

  private def incrementQuantity(itemName: String, amount: Int): Unit = {
    itemSet = itemSet.map {
      case item if item.getItemName == itemName =>
        item.setTotalQuantity(item.getTotalQuantity + amount)
        item
      case item => item
    }
  }

  private def removeItem(itemName: String): Unit = {
    itemSet = itemSet.filterNot(_.getItemName == itemName) // Remove by name
  }

  private def decrementQuantity(itemName: String, amount: Int): Unit = {
    itemSet = itemSet.map {
      case item if item.getItemName == itemName =>
        val newQuantity = item.getTotalQuantity - amount
        item.setTotalQuantity(if (newQuantity < 0) 0 else newQuantity) // Prevent negative quantities
        item
      case item => item
    }
  }

  // Method to return checked out items
  def getCheckedOutItems: mutable.LinkedHashSet[(String, Int, Double)] = checkedOutItems

  // Method to return available items in store
  def getAvailableItems: mutable.LinkedHashSet[Item] = itemSet

}

