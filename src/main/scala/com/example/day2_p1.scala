package com.example

import java.io._
import scala.io._

class KeyValueStore_2_1(file: File) {
  private val deletedValue = "deleted"

  def store(key: String, value: String): Unit = {
    val writer = new FileWriter(file, true)
    writer.write(s"$key,$value\n")
    writer.close()
    println(s"Stored key '$key' with value '$value'")
  }

  def lookup(key: String): Unit = {
    val lines = Source.fromFile(file).getLines().toList.reverse
    val value = lines
      .find(_.startsWith(s"$key,"))
      .map(_.substring(key.length + 1))
      .filter(_ != deletedValue)
    value match {
      case Some(v) => println(s"Found key '$key' with value '$v'")
      case None    => println(s"Key '$key' not found")
    }
  }

  def delete(key: String): Unit = {
    val writer = new FileWriter(file, true)
    writer.write(s"$key,$deletedValue\n")
    writer.close()
    println(s"Deleted key '$key'")
  }
}

class ConsoleReader_2_1(KeyValueStore_2_1: KeyValueStore_2_1) {
  import scala.io.StdIn.readLine

  def run(): Unit = {
    while (true) {
      val input = readLine()
      input.split("\\s+") match {
        case Array("store", key, value) => KeyValueStore_2_1.store(key, value)
        case Array("lookup", key)       => KeyValueStore_2_1.lookup(key)
        case Array("delete", key)       => KeyValueStore_2_1.delete(key)
        case _                          => println("Invalid command")
      }
    }
  }
}

object KeyValueStore_2_1App extends App {
  val file = new File("store.txt")
  val KeyValueStore_2_1 = new KeyValueStore_2_1(file)
  val consoleReader = new ConsoleReader_2_1(KeyValueStore_2_1)

  // Start the console reader
  consoleReader.run()
}
