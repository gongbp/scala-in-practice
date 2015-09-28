package chapter4.traits.example3

import java.io.PrintStream

import chapter4.traits.example1.Account

/**
 * Add comments.
 *
 * author: gong_baiping
 * date: 15/09/28 19:09
 * version: 0.1 (Scala 2.11.6, Play 2.4.2)
 * copyright: TonyGong, Inc.
 */
trait Logger

trait FileLogger extends Logger {
  val filename: String                // 用抽象字段来存放文件名，抽象字段可以用val/var修饰，特质构造器的一部分
  val out = new PrintStream(filename) // 特质构造器的一部分，在构造本特质时，会执行这两句
  def log(msg: String) {
    out.println(msg)
    out.flush()
  }
}

/**
 * 编译错误，需要初始化FileLogger中的抽象字段filename
 */
/*class SavingsAccount extends Account with FileLogger {
   // SavingsAccount的实现
}*/
class SavingsAccount extends { // extends之后是提前定义块
  val filename = "myapp.log"
} with Account with FileLogger {
   // SavingsAccount的实现
}

object TraitWithPreDefinition extends App {

}

