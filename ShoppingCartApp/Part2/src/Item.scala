package Part2.src

class Item {

  private var itemId: Int = 0
  private var itemName: String = ""
  private var totalQuantity: Int = 0
  private var perItemCost: Double = 0.00

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
