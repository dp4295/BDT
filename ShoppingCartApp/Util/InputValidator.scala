package Util

object InputValidator {

  // Safely read an integer from user input
  def readIntWithRetry(prompt: String): Int = {
    var isValid = false
    var number = 0
    while (!isValid) {
      try {
        val input = scala.io.StdIn.readLine(prompt)
        number = input.toInt // Try to convert input to an integer
        isValid = true // If successful, mark as valid
      } catch {
        case _: NumberFormatException =>
          // Handle invalid input and prompt the user again
          println(s"Invalid input. Please enter a valid number.")
      }
    }
    number
  }

  // Method to read and validate 'y' or 'n' input (case-insensitive)
  def readYesNoWithRetry(prompt: String): String = {
    var isValid = false
    var result = ""
    while (!isValid) {
      val input = scala.io.StdIn.readLine(prompt).trim.toLowerCase
      input match {
        case "y" =>
          result = "y"
          isValid = true
        case "n" =>
          result = "n"
          isValid = true
        case _ =>
          println("Invalid input. Please enter 'y' for Yes or 'n' for No.")
      }
    }
    result
  }

}
