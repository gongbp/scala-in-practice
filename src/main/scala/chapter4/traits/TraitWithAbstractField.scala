package chapter4.traits.example2

/**
 * 4.10 特质中的抽象字段
 *
 * author: gong_baiping
 * date: 15/09/28 18:50
 * version: 0.1 (Scala 2.11.6, Play 2.4.2)
 * copyright: TonyGong, Inc.
 */

trait Logged {
  def log(info: String) = {
    println(info)
  }
}

class Account {
  var balance = 0.0 // 父类中的普通字段
}

trait ShortLogger extends Logged {

  val maxLength: Int // 一个抽象的val字段（没有被初始化）
  var name: String   // 一个抽象的var字段（没有被初始化）

  // 方法的实现中使用了上面的抽象字段
  override def log(msg: String) {
    super.log(
      if(msg.length <= maxLength) msg
      else msg.take(maxLength - 3) + "..."
    )
  }
}

// 在具体类中使用ShortLogger特质时，必须要实现其抽象字段maxLength
class SavingsAccount extends Account with ShortLogger {
  val maxLength = 20 // 具体化特质ShortLogger中的抽象字段maxLength
  var name = "abstract var variable"
}

object TraitWithAbstractField extends App {
  val savingsAccount = new SavingsAccount
  println(savingsAccount.maxLength)
}
