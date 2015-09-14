# 2 Scala中的对象

标签（空格分隔）： 级别A1:初级程序设计者 深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------

本篇通过一个问题引出下面的内容。
何时使用Scala的object语法？也就是何时使用object关键字定义一个对象？
答案就是：你需要某个类的单个实例时（单例对象），或者为一些值或方法提供一个namespace。就可以使用该语法。


## 2.1 用对象作为单例或存放Util方法

Scala没有静态方法或静态字段，可以把这些方法或字段放在一个单例对象中。例如：
```
// 单例对象(Singletons)
object Accounts {
  private var lastNumber = 0
  def newUniqueNumber() = { lastNumber += 1; lastNumber }
}
```
然后通过`对象.方法名`即可进行调用。

单例对象的用途：
- 作为存放工具方法或常量的地方
- 高效的共享单个不可变实例
- 需要用单个实例来协调某个服务时

---

---

## 2.2 类可以拥有一个同名的伴生对象

在Java中，通常需要既有实例方法又有静态方法的类。在Scala中，你可以通过类和与类同名的伴生对象来达到同样的目的。
```
class Account {
  val id = Account.newUniqueNumber()
  private var balance = 0.0
  def deposit(amount: Double) { balance += amount }
  //...
}

object Account { // The companion object
  private var lastNumber = 0
  private def newUniqueNumber() = { lastNumber += 1; lastNumber }
}

```
类和它的伴生对象可以相互访问私有成员。类的伴生对象可以被访问，但并不在作用域当中。比如，Account类必须通过Account.newUniqueNumber()而不是直接用newUniqueNumber()来调用伴生对象的方法。
另外，它们必须存在于同一个文件中。

---

---

## 2.3 对象可以扩展类Class或特质Trait 

一个object可以扩展类以及一个或多个Trait，其结果是一个扩展了指定类以及Trait的对象，同时拥有在对象定义中给出的所有特性。

一个有用的场景就是使用共享对象，就是一个对象提供若干方法，在任何地方都可以引入这个对象，并使用其提供的方法。

例如：
```
abstract class UndoableAction(val description: String) {
  def undo(): Unit
  def redo(): Unit
}
```

默认情况下可以是什么都不做。对于这个行为我们只需要一个实例即可。

```
object DoNothingAction extends UndoableAction("Do nothing") {
  override def undo() {}
  override def redo() {}
}

```

DoNothingAction对象可以被所有需要这个缺省行为的地方共用。

```
object Run extends App {
  val actions = Map("open" -> DoNothingAction, "save" -> DoNothingAction /*, ...*/ )
}

```

---

---

## 2.4 对象的apply方法通常用来构造伴生类的新实例

我们通常会定义和使用对象的apply方法。apply方法被定义在伴生对象中，当做工厂方法来生产伴生类的对象。当遇到如下形式的表达式时，apply方法就会被调用：

```
Object(参数1, ..., 参数N)
```
举例来说，Array对象定义了apply方法，让我们可以用下面这样的表达式来创建数组：
```
Array("hello", "world")
```

为什么不使用构造器呢？对于嵌套表达式而言，省去new关键字会方便很多，例如：
```
Array(Array(1, 7), Array(2, 9))
```
>注意: Array(100)和new Array(100)的区别。
Array(100)调用的是apply(100)，输出一个元素（整数100）的Array[Int]；
new Array(100)调用的是构造器this(100)，结果是Array[Nothing]，包含了100个null元素。

一个使用apply方法的具体例子：
```
// 对伴生类的主构造方法进行私有化，对外不可见，不能通过new的方式实例化Account对象
class Account private (val id: Int, initialBalance: Double) {
  private var balance = initialBalance
}

// 由于伴生对象和伴生类之间可以互相访问私有成员，所以在伴生对象中是可以通过new的方式实例化Account对象的（也就是可以访问私有化的主构造方法）
osbject Account { 
  // 工厂方法生产伴生类的对象
  def apply(initialBalance: Double) =
    new Account(0, initialBalance)
}

object Run extends App {
  val acct = Account(1000.0) // 等价于Account.apply(1000.0)
}

```

---

---


## 2.5 不显示定义main方法，可以使用扩展App特质的对象

扩展App特质的对象称为应用程序对象。每个Scala程序都必须从一个对象的main方法开始，这个方法的类型为`Array[String] => Unit`，表示接收一个字符串数组类型的参数，返回空值Unit。

```
object Hello {
  def main(args: Array[String]) {
    println("Hello, World!")
  }
}
```
除了每次都提供自己的main方法外，也可以通过一个对象扩展App特质，然后将程序代码放入构造器方法体内：

```
object Hello extends App {
  println("Hello, World!")
}
```
如果你需要命令行参数，则可以通过args属性得到：
```
object Hello extends App {
  if (args.length > 0) 
    println(s"Hello, ${args(0)}")
  else 
    println("Hello, World!")
}
```

如果你在调用该程序时设置了`scala.time`选项的话，程序退出时会显示执行的时间。
```
$ scalac Hello.scala
$ scala -Dscala.time Hello Jack
Hello, Jack

```
App特质扩展自另一个特质DelayedInit，编译器对该特质有特殊处理。所有带有该特质的类，其初始化方法都会被挪到delayedInit方法中。App特质的main方法捕获到命令行参数，调用delayedInit方法，并且还可以根据需要打印出执行时间。

>说明，较早版本有一个Application的特质来达到同样的目的。那个特质是在静态初始化方法中执行程序动作，并不被即时编译器优化

---

---

## 2.6 扩展Enumeration对象来实现枚举

Scala中并没有枚举类型。但是标准库提供了一个Enumeration的类，可以用于枚举场合。
定义一个扩展Enumeration类的对象并以Value方法调用初始化枚举中的所有可选值。例如：
```
object TrafficLightColor extends Enumeration {
  val Red, Yellow, Green = Value
}
```
在这里定义了三个字段： Red, Yellow和Green,然后用Value调用将它们初始化。这是如下代码的简写：
```
val Red = Value
val Yellow = Value
val Green = Value

```
每次调用Value方法都返回内部类的新实例，该内部类也叫做Value。
也就是每一个字段都是Value的一个实例对象。
或者，你也可以向Value方法传入ID，名称，或两个参数都传:
```
val Red = Value(0, "Stop") // ID默认为从0开始
val Yellow = Value(10) // 名称为“Yellow”
val Green = Value("Go") // ID为11

```
>说明：枚举中的字段有两个值，一个ID，一个名称。
ID的值从0开始，如果不指定，则ID在前一个枚举值的基础上加一。
名称不指定，缺省名称为字段名。

定义完成后，你就可以用TrafficLightColor.Red，TrafficLightColor.Yellow等来引用枚举值。枚举值的类型是TrafficLightColor.Value而不是TrafficLightColor--后者是拥有这些值的对象。有人推荐增加一个类型别名:
```
object TrafficLightColor extends Enumeration {
  type TrafficLightColor = Value
  val Red, Yellow, Green = Value
}

```
现在枚举的类型就变成了TrafficLightColor.TrafficLightColor,但仅当你使用import时这样做才有意义。
例如：
```
import TrafficLightColor._
def doWhat(color: TrafficLightColor) = {
  if (color == Red) "stop"
  else if (color == Yellow) "hurry up"
  else "go"
}

```
枚举的ID可以通过id方法返回，比如： Red.id
对TrafficLightColor.values调用输出所有枚举值的集：
```
for(c <- TrafficLightColor.values) println(c.id + ":" + c)

```
最后，你可以通过枚举的ID或名称来进行定位，以下两段代码都输出TrafficLightColor.Red对象：
```
TrafficLightColor(0) // 将调用Enumeration.apply(0)
TrafficLightColor.withName("Red")
```

---

---

References:

[1]. 【Scala for the impatient chapter 6】