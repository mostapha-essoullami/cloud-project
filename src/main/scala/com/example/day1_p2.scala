package com.example
import akka.actor._

class KeyValueStore_1_2 extends Actor {
  val hashTable = scala.collection.mutable.HashMap[String, String]()

  def receive = {
    case Store(key, value) =>
      hashTable.put(key, value)
      println(s"Stored key '$key' with value '$value'")
    case Lookup(key) =>
      hashTable.get(key) match {
        case Some(value) => println(s"Found key '$key' with value '$value'")
        case None        => println(s"Key '$key' not found")
      }
    case Delete(key) =>
      hashTable.remove(key) match {
        case Some(value) => println(s"Deleted key '$key' with value '$value'")
        case None        => println(s"Key '$key' not found")
      }
  }
}

class ConsoleReader_1_2(KeyValueStore_1_2: ActorRef) extends Actor {
  import scala.io.StdIn.readLine

  def receive = { case ReadInput =>
    println("choose store, lookup or delete:")
    val input = readLine()
    input.split("\\s+") match {
      case Array("store", key, value) => KeyValueStore_1_2 ! Store(key, value)
      case Array("lookup", key)       => KeyValueStore_1_2 ! Lookup(key)
      case Array("delete", key)       => KeyValueStore_1_2 ! Delete(key)
      case _                          => println("Invalid command")
    }
    self ! ReadInput
  }
}

object KeyValueStore_1_2App extends App {
  val system = ActorSystem("KeyValueStore_1_2System")
  val KeyValueStore_1_2 =
    system.actorOf(Props[KeyValueStore_1_2], name = "KeyValueStore_1_2")
  val consoleReader = system.actorOf(
    Props(new ConsoleReader_1_2(KeyValueStore_1_2)),
    name = "consoleReader"
  )

  // Start the console reader
  consoleReader ! ReadInput

}
