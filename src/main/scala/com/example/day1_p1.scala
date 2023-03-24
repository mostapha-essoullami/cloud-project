package com.example

import akka.actor._

class KeyValueStore_1_1 extends Actor {
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

object KeyValueStore_1_1App extends App {
  val system = ActorSystem("KeyValueStore_1_1_1_1System")
  val KeyValueStore_1_1 =
    system.actorOf(Props[KeyValueStore_1_1], name = "KeyValueStore_1_1")

  // Examples of using the system
  KeyValueStore_1_1 ! Store("key1", "value1")
  KeyValueStore_1_1 ! Store("key2", "value2")
  KeyValueStore_1_1 ! Lookup("key1")
  KeyValueStore_1_1 ! Lookup("key3")
  KeyValueStore_1_1 ! Delete("key2")

  system.terminate()
}
