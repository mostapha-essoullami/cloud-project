package com.example

import akka.actor._

import java.io._
import scala.io._

class CashStore(fileStore: ActorRef) extends Actor {
  val hashTable = scala.collection.mutable.HashMap[String, String]()

  def receive = {
    case Store(key, value) =>
      if (hashTable.size > 1000) {
        val (k, _) = hashTable.head
        hashTable.remove(k)
      }
      hashTable.put(key, value)
      println(s"Stored key '$key' with value '$value'")
      fileStore ! Store(key, value)
    case Lookup(key) =>
      hashTable.get(key) match {
        case Some(value) => println(s"Found key '$key' with value '$value'")
        case None        => fileStore ! Lookup(key)
      }
    case Delete(key) =>
      hashTable.remove(key) match {
        case Some(value) => println(s"Deleted key '$key' with value '$value'")
        case None        => println(s"Key '$key' not found")
      }
      fileStore ! Delete(key)
  }
}

class FileStore extends Actor {
  val file = "store.txt"
  private val deletedValue = "deleted"

  def receive = {
    case Store(key, value) =>
      Thread.sleep(50)
      val writer = new FileWriter(file, true)
      writer.write(s"$key,$value\n")
      writer.close()
      println(s"Stored key '$key' with value '$value'")
    case Lookup(key) =>
      Thread.sleep(50)
      val lines = Source.fromFile(file).getLines().toList.reverse
      val value = lines
        .find(_.startsWith(s"$key,"))
        .map(_.substring(key.length + 1))
        .filter(_ != deletedValue)
      value match {
        case Some(v) => println(s"Found key '$key' with value '$v'")
        case None    => println(s"Key '$key' not found")
      }

    case Delete(key) =>
      Thread.sleep(50)
      val writer = new FileWriter(file, true)
      writer.write(s"$key,$deletedValue\n")
      writer.close()
      println(s"Deleted key '$key'")
  }

}

import scala.util.Random
import akka.actor.ActorRef

class RandomClient(keyValueStore: ActorRef) {

  // Populate the storage
  for (i <- 1 to 500) {
    keyValueStore ! Store(i.toString, s"value_$i")
  }

  // Make a random request (store, delete, or lookup) to the storage service
  for (i <- 1 to 1000) {
    val randomKey = Random.nextInt(1000) + 1
    val randomValue = s"value_${Random.nextInt(1000) + 1}"
    val randomAction = Random.nextInt(3)
    randomAction match {
      case 0 => keyValueStore ! Store(randomKey.toString, s"value_$randomValue")
      case 1 => keyValueStore ! Delete(randomKey.toString)
      case 2 => keyValueStore ! Lookup(randomKey.toString)
    }
  }
}

object KeyValueStoreApp extends App {
  val system = ActorSystem("KeyValueStoreSystem")
  val fileStore = system.actorOf(Props[FileStore], name = "fileStore")
  val keyValueStore =
    system.actorOf(Props(new CashStore(fileStore)), name = "keyValueStore")

  // Start the random operations
  val t1 = System.currentTimeMillis

  val randomClientWithoutCache = new RandomClient(fileStore)

  println(
    s"\n\n\nsystem without cache took ${System.currentTimeMillis - t1} millis\n\n\n"
  )
  Thread.sleep(1000)

  val t2 = System.currentTimeMillis

  val randomClientWithCache = new RandomClient(keyValueStore)

  println(
    s"\n\n\nsystem with the cache took ${System.currentTimeMillis - t2} millis\n\n\n"
  )

}
