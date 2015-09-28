package chapter4.traits.example1

/**
 * 4.9 特质中的具体字段
 *
 * author: gong_baiping
 * date: 15/09/28 18:29
 * version: 0.1 (Scala 2.11.6, Play 2.4.2)
 * copyright: TonyGong, Inc.
 */
trait Logged {
  def log(info: String) = {
    println(info)
  }
}

trait ShortLogger extends Logged {
  val maxLength = 15 // 一个具体的字段（给出了初始值）
//  var balance = 1.0  // 跟Account类中的字段重复
}

class Account {
  var balance = 0.0 // 父类中的普通字段
}
class SavingsAccount extends Account with ShortLogger {
  var interest = 0.0 // 子类中的普通字段
  def withdraw(amount: Double) {
    if(amount > balance) log("Insufficient funds")
    else balance * interest
  }
}

object TraitWithSpecificFields extends App {

  // compile error!
  // 从父类Account中继承来的字段balance和Trait中添加进来的具体字段balance冲突
  val savingsAccount = new SavingsAccount
  println(savingsAccount.balance)
}