package com.example

case class Store(key: String, value: String)
case class Lookup(key: String)
case class Delete(key: String)
case object ReadInput
