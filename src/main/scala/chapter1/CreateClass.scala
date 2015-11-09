package chapter1

/**
 * Created by A12975 on 11/7/15.
 */
class Person {
  val name = "jack"
  private var age = 0
  private[this] var nationality = "china"

  /**
   * 在本Person类中接收一个Person类型的参数,并访问该参数对象的字段时,
   * 就充分体现了private和private[this]的主要区别
   * @param p 一个Person类型的参数
   */
  def invite(p: Person) = {
    // private修饰时,在这种情况下是可以访问的
    val sumAge = age + p.age

    // 而用private[this]修饰时,在此种情况下会编译报错: Symbol nationality is inaccessible from this place
    val nationalities = s"$nationality + ${p.nationality}"
  }
}

object PersonTest extends App {
  val person = new Person
}
