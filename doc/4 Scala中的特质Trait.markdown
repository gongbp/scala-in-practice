# 4 Scala中的特质Trait

标签（空格分隔）： 级别L1:初级类库设计者 深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------

本章要点如下：

- 类可以实现任意数量的特质Trait
- 特质可以要求实现他们的类具备特定的字段，方法或父类
- 特质可以提供方法和字段的具体实现
- with多个特质的时候，注意其顺序--其方法被先执行的特质排在更后面

## 4.1 为什么没有多重继承，Scala通过特质如何解决多重继承问题

多重继承会引发菱形继承问题，既，两个父类继承自同一个基类，则子类中会包含两份父类中的内容，不合并重复内容会引起一些歧义，而合并重复内容又会导致类成员的内存布局不能简单的从父类复制。

Scala中，在构造特质时，对其构造顺序进行限制来消除多重继承的问题。具体见下面篇幅的介绍。

## 4.2 当作接口使用的特质

当作接口使用的Trait，意味着这个Trait里面全是抽象方法:
```
trait Logger {
  def log(msg: String) // 不需要abstract修饰，未被实现的方法就是抽象的
}
```
通过子类来实现抽象方法：
```
class ConsoleLogger extends Logger { // 用extends，而不是implements
  def log(msg: String) { // 子类实现一个抽象方法，不需要override修饰。重写父类的具体方法才需要override。
    println(msg) 
  }
}
```

如果需要混入多个特质时，用with来添加额外的特质：
```
class ConsoleLogger2 extends Logger with Cloneable with Serializable {
  def log(msg: String) { println(msg) }
}
```
在这里我们使用了Java类库的Cloneable和Serializable接口，仅仅是为了展示语法的需要。
所有Java的接口都可以在Scala中作为特质使用。

>说明：在第一个特质前使用extends，而在所有其他特质前使用with看上去很奇怪。其实Scala并不是这样来解读的。在Scala中，Logger with Cloneable with Serializable 首先是一个整体，然后再由类来扩展。

---

---

## 4.3 带有具体实现的Trait

在Scala中，特质中的方法并不需要一定是抽象的。也可以是一个带有具体实现方法的特质：
```
trait ConsoleLogger {
  def log(msg: String) { println(msg) } // 一个具体实现的方法
}
```
使用上面的特质把日志信息打印出来。
```
class Account() {
  var balance: Double = 0
}

class SavingsAccount extends Account with ConsoleLogger {
  def withdraw(amount: Double) {
    if (amount > balance) log("Insufficient funds")
    else balance -= balance
  }
  //...
}
```
在Scala中，我们说ConsoleLogger提供的功能被混入到了SavingsAccount类中。

---

---

## 4.4 带有特质的对象【实例化一个对象的时候混入特质（一个或多个）】

```
// 定义一个账户
class Account() {
  var balance: Double = 0
}

// 一个空实现的Logger
trait Logged {
  def log(msg: String) {}
}

trait ConsoleLogger extends Logged {
  override def log(msg: String) { println(s"Sub trait ConsoleLogger: $msg") }
}

trait ConsoleLogger2 {
  def log(msg: String) { println(s"ConsoleLogger2: $msg") }
}

trait FileLogger {
  def logFile(msg: String) { println(msg) }
}

class SavingsAccount extends Account with Logged {
  def withdraw(amount: Double) {
    if (amount > balance) log("Insufficient funds")
    else log("OK")
  }
  //...
}

object Run extends App {
  /**
   * 因为类SavingsAccount混入了Logged，并使用了特质提供的log方法。
   * 在实例化SavingsAccount的对象时，可以混入Logged特质的子类型。
   * 那么在调用这个对象所具有的特质方法log时，将会执行子类型中的log的方法。
   */
  val acct = new SavingsAccount with ConsoleLogger
  acct.withdraw(10)

  /**
   * 在实例化SavingsAccount的对象时，混入了一个非Logged特质的子类型，
   * 如果这个非子类型跟Logged特质存在同名的log方法时，编译出错：
   * inherits conflicting members
   * 反之，如果这个子类型跟Logged没有冲突的字段和方法，则可以混入
   */
  val acct2 = new SavingsAccount with ConsoleLogger2 // 这里会编译出错：inherits conflicting members
  acct2.withdraw(10)
  
  /**
   * 另一个对象也可以混入不同的特质
   */
  val acct3 = new SavingsAccount with FileLogger
}
```
>对上面例子的说明：
在实例化SavingsAccount对象时，可以混入该对象所具有的特质Logged的子类型ConsoleLogger。
那么在调用这个对象所具有的特质方法log时，将会执行子类型ConsoleLogger的log方法。

---

---

## 4.5 叠加在一起的特质

可以为类或对象添加多个相互调用的特质，调用将会从最后一个特质开始。
这个功能对需要分阶段加工处理某个值的场景很有用。
```
class Account() {
  var balance: Double = 0
}

trait Logged {
  def log(msg: String) {}
}

class SavingsAccount extends Account with Logged {
  def withdraw(amount: Double) {
    if (amount > balance) log("Insufficient funds")
    else log("OK")
  }
  //...
}

// 单纯打印日志信息
trait ConsoleLogger extends Logged {
  override def log(msg: String) { println(msg) }
}

// 为日志信息添加时间戳
trait TimestampLogger extends Logged {
  override def log(msg: String) {
    super.log(new java.util.Date() + " " + msg)
  }
}

// 截断冗长的日志信息
trait ShortLogger extends Logged {
  val maxLength = 15
  override def log(msg: String) {
    super.log(
        if (msg.length <= maxLength) msg
        else msg.substring(0, maxLength - 3) + "...")
  }
}

object Run extends App {

  /**
   * acct1对象在调用log方法时，首先会调用ShortLogger中的log方法，
   * ShortLogger中的log方法对日志进行处理后，把处理后的结果信息
   * 通过super.log方式传递给混入顺序的上一层TimestampLogger进行处理。
   * 同样，TimestampLogger的log方法处理完成后，再通过super.log方式
   * 传递给混入顺序的上一层ConsoleLogger处理，最后打印出日志信息。
   * 那为什么没有使用Logger中的log方法而是到ConsoleLogger中的log方法就停止了呢？
   * 因为ConsoleLogger中的log方法并没有继续使用super去调用上一个层级Logger中的方法。
   * 通过这样的一种机制能很好的避免多重继承的问题。
   */
  val acct1 = new SavingsAccount with ConsoleLogger with TimestampLogger with ShortLogger
  acct1.log("test") // Sun Dec 01 16:01:17 CST 2013 test

  // 线性化顺序也就是super被解析的顺序
  // SavingAccount -> TimestampLogger -> ShortLogger -> ConsoleLogger -> Logger -> Account
 
  val acct2 = new SavingsAccount with ConsoleLogger with ShortLogger with TimestampLogger
  acct2.log("test") // Sun Dec 01 1...
}
```

---

---

## 4.6 特质构造顺序

和类一样，特质也可以有构造器，由字段的初始化和其他特质体中的语句构成。
```
trait FileLogger extends Logger {
  val out = new PrintWriter("app.log") // 特质构造器的一部分
  out.println(s"${new Date()}")        // 特质构造器的一部分
  
  def log(msg: String) {
    out.println(msg)
    out.flush()
  }
}
```
在实例化混入该特质的对象时，特质构造器部分的语句都会被执行。
以下面的例子说明：
```
class SavingsAccount extends Account with ConsoleLogger with ShortLogger with TimestampLogger{}
```
构造器以如下的顺序执行：

- 调用超类Account的构造器；
- 特质构造器在超类Account构造器之后、类SavingsAccount构造器之前执行；
- 特质由左到右被构造；每个特质当中，父特质先被构造；
  首先，Logger（第一个Trait的父Trait），
  然后，ConsoleLogger（第一个Trait），
  第三，ShortLogger（第二个Trait，注意到它的父Trait已经被构造）
  第四，TimestampLogger（第三个Trait，注意到它的父Trait已经被构造）
- 如果多个特质共有一个父特质，父特质不会被重复构造
- 所有特质被构造完毕，子类SavingsAccount被构造。

```
class Account() {
  var balance: Double = 0
}

trait Logged {
  def log(msg: String) {} // 这是一个空实现的方法，而并非抽象方法
}

class SavingsAccount extends Account with Logged {
  def withdraw(amount: Double) {
    if (amount > balance) log("Insufficient funds")
    else log("OK")
  }
  //...
}

// 单纯打印日志信息
trait ConsoleLogger extends Logged {
  override def log(msg: String) { println(msg) }
}

// 为日志信息添加时间戳
trait TimestampLogger extends Logged {
  override def log(msg: String) {
    super.log(new java.util.Date() + " " + msg)
  }
}

// 截断冗长的日志信息
trait ShortLogger extends Logged {
  val maxLength = 15
  override def log(msg: String) {
    super.log(
        if (msg.length <= maxLength) msg
        else msg.substring(0, maxLength - 3) + "...")
  }
}

class SavingsAccount extends Account with ConsoleLogger with ShortLogger with TimestampLogger{}

Account（超类）
Logger（第一个Trait的父Trait）
ConsoleLogger（第一个Trait）
ShortLogger（第二个Trait，注意到它的父Trait已经被构造）
TimestampLogger（第三个Trait，注意到它的父Trait已经被构造）
SavingAccount（子类）

// 构造器顺序
Account -> Logger -> ConsoleLogger -> ShortLogger -> TimestampLogger -> SavingAccount
```
---

线性化：
如果C extends C1 with C2 … with Cn，则lin( C ) = C ⪼ lin(Cn) ⪼ … ⪼ lin(C2) ⪼ lin(C1)，在这里⪼的意思是“串接并去掉重复项，右侧胜出”。

例如：
```
lin(SavingAccount) 
= SavingAccount ⪼ lin(TimestampLogger) ⪼ lim(ShortLogger) ⪼ lim(ConsoleLogger) ⪼ lim(Account)
= SavingAccount ⪼ (TimestampLogger ⪼ Logger) ⪼ (ShortLogger ⪼ Logger) ⪼ (ConsoleLogger ⪼ Logger) ⪼ lim(Account)
= SavingAccount ⪼ TimestampLogger ⪼ ShortLogger ⪼ ConsoleLogger ⪼ Logger ⪼ Account

// 线性化顺序
SavingAccount -> TimestampLogger -> ShortLogger -> ConsoleLogger -> Logger -> Account
```
线性化给出了在特质中super被解析的顺序。

例如：

- 在TimestampLogger中调用super会执行ShortLogger中的方法，
- 而在ShortLogger中调用会执行ConsoleLogger中的方法。

> **构造器的顺序是类的线性化的反向**。 

---

---

## 4.7 在特质中重写抽象方法（在特质中实现抽象方法）

我们实现一个提供抽象方法的特质：
```
trait Logger {
  def log(msg: String) // 抽象方法
}  

```
再通过一个具体的时间戳特质来扩展上面的Logger特质，即实现其抽象方法log。

```
// 此种实现编译出错，因为super.log调用了一个抽象方法，编译器将super.log调用标记为错误。
trait TimestampLogger extends Logger {
  override def log(msg: String) { // 重写抽象方法
    super.log(new java.util.Date() + " " + msg) // super.log定义了吗？
  }
}
```
根据正常的继承规则，这个调用肯定是错误的---Logger.log方法没用实现。但实际上，我们没法知道哪个log方法最终被调用---这取决于特质被混入的顺序。

所以，这种情况TimestampLogger依旧被当作是抽象的---它需要混入一个具体的log方法。
因此，需要用**abstract override**修饰TimestampLogger中的log方法:
```
trait TimestampLogger extends Logger {
  abstract override def log(msg: String) { // 重写抽象方法
    super.log(new java.util.Date() + " " + msg) 
  }
}
```
非抽象类和特质扩展TimestampLogger时，要求需要实现log方法。
```
class Account extends TimestampLogger // 编译错误，需要实现log方法
```

---

---

## 4.8 当做富接口使用的特质
特质可以包含大量工具方法，而这些工具方法可以依赖一些抽象方法来实现。
>例如：scala中的Iterator特质就利用抽象的next和hasNext方法定义了很多方法。

丰富的Logger特质，提供大量的工具方法：
```
class Account() {
  var balance: Double = 0
}

// Scala中经常使用抽象方法和具体方法相结合的方式
trait Logger {
  def log(msg: String) // 定义一个抽象方法
  // 其他的工具方法都依赖于上面的抽象方法log
  def trace(msg: String) = log(s"[TRACE]: $msg") // 把抽象方法和具体方法结合在一起的例子
  def debug(msg: String) = log(s"[DEBUG]: $msg")
  def info(msg: String)  = log(s"[INFO]:  $msg")
  def warn(msg: String)  = log(s"[WARN]:  $msg")
  def error(msg: String) = log(s"[ERROR]: $msg")
}

class SavingsAccount extends Account with Logger {
  // 任意使用Logger中的其他工具方法，比如error方法
  def withdraw(amount: Double) {
    if (amount > balance) error("Insufficient funds")
    else log("OK")
  }
  // 子类在使用这个特质时，需要实现抽象的的log方法。
  override def log(msg: String) = println(msg)
}
```

---

---

## 4.9 特质中的具体字段
特质中的字段可以是具体的，也可以是抽象的。如果给出了初始值，字段就是具体的。
```
trait ShortLogger extends Logged {
  val maxLength = 15 // 一个具体的字段（给出了初始值）
}

```

混入了该特质（ShortLogger）的类会自动获得特质的具体字段（maxLength）。
通常，对于特质中的每一个具体字段，使用该特质的类都会获得一个字段与之对应。
但这些字段不是被继承的，而是被简单加入到了子类当中。这个很细微的区别非常重要，通过实际例子来展示说明：
```
class Account {
  var balance = 0.0 // 父类中的普通字段
}

class SavingsAccount extends Account with ShortLogger {
  
  var interest = 0.0 // 子类中的普通字段
  
  def withdraw(amount: Double) {
    if(amount > balance) log("Insufficient funds")
    else ...
  }
}

```
SavingsAccount类按照正常的方式继承了这个字段。SavingsAccount对象由所有超类的字段以及任何子类中定义的字段构成。则SavingsAccount对象的字段构成如下图：

|---------|
  balance    <-- 从父类继承下来的字段
|---------|
 interest    <-- SavingsAccount类中的普通字段
 maxLength   <-- 来自特质中的字段被放置在子类中
|---------|

在JVM中，一个类只能扩展一个父类，因此来自特质的字段不能以相同的方式继承。由于这个限制，maxLength被直接加到了SavingsAccount类中，跟interest字段并排在一起。

>从特质中通过混入的方式获得的具体字段都自动成为该类自己的字段，这种字段等同于在类中自己定义的普通字段。

---

---

## 4.10 特质中的抽象字段
特质中没有被初始化的字段就是抽象字段，抽象字段必须在子类中被具体化（实现）。
带有抽象字段的特质的例子：
```
trait ShortLogger extends Logged {

  var maxLength: Int // 一个抽象的字段（没有被初始化）
  
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
  var maxLength = 20 // 具体化特质ShortLogger中的抽象字段maxLength
}

```

---

---

## 4.11 初始化特质中的字段

每个特质只有一个无参数的构造器。
>特质与类唯一的差别：
- 特质没有带参数的构造器，而是只有一个无参构造器

由于特质Trait只有一个无参构造器，那么在需要向特质指定参数的情况下就无法实现：
比如，以一个文件日志生成器Trait为例来说明：
```
// 向特质FileLogger传入一个指定的参数，用于指定日志文件
val acct = new SavingsAccount with FileLogger("myapp.log") // 错误：特质不能使用构造器参数
```

对于这个局限其解决方法是，可以考虑使用抽象字段来存放文件名：
```
trait FileLogger extends Logger {
  val filename: String // 用抽象字段来存放文件名，抽象字段可以用val/var修饰
  val out = new PrintStream(filename)
  def log(msg: String) { out.println(msg); out.flush() }
}
```
然后再直接使用特质FileLogger：
```
// 错误的使用方式,混入了FileLogger的SavingsAccount类实例
val acct = new SavingsAccount with FileLogger {
  val filename = "myapp.log"
}
```
但是这样却是行不通的。问题来自于构造顺序。FileLogger的构造器会先于子类构造器执行，这里的子类是混入了FileLogger的SavingsAccount类实例。在构造FileLogger时，就会抛出一个空指针异常，子类的构造器根本就不会执行。

这个问题的解决方法之一是使用**提前定义**这个语法:
```
// 一个扩展了SavingsAccount且混入了FileLogger的匿名类实例
val acct = new { // new之后的提前定义块
  val filename = "myapp.log"
} with SavingsAccount with FileLogger
```
这段“恶心”代码解决了直接使用时的构造顺序的问题。提前定义发生在常规的构造序列之前。
在FileLogger被构造时，filename已经被初始化了。

如果你需要在类中做同样的事情，代码如下：
```
class SavingsAccount extends { // extends之后是提前定义块
  val filename = "myapp.log"
} with Account with FileLogger {
  ... // SavingsAccount的实现
}
```

另外一个方法是使用**懒值**，因为懒值在真正的初次使用时才被初始化：
```
trait FileLogger extends Logger {
  val filename: String
  lazy val out = new PrintStream(filename)
  def log(msg: String) { out.println(msg) } // 真正使用log方法时，out才被初始化，而这个时候filename已经早就被初始化完了。
}
```
如此一来，out字段不会再抛出空指针异常。在使用out字段时，filename也已经初始化了。

**但是使用懒值不高效，因为懒值在每次使用前都会检查是否已经初始化。**

---

---

## 4.12 扩展一个类的特质（Trait extends Class）

通常特质可以扩展另一特质，由特质组成的继承层级也比较常见。
但是不太常见的一种用法也是存在的，那就是通过一个特质去扩展一个类。这个类会自动成为所有混入该特质的类的超类。例如：

```
trait LoggedException extends Exception with Logged {
  // 定义一个log方法来记录异常信息
  def log() { 
    log(getMessage()) // 调用从Exception超类继承下来的getMessage()方法。
  }
}
```

创建一个混入该特质LoggedException的类UnhappyException：
```
class UnhappyException extends LoggedException {
  override def getMessage = "arggh!"
}
```
特质的超类Exception自动成为了混入了LoggedException特质的UnhappyException的超类。

Scala并不允许多继承。
那么这样一来，如果UnhappyException原先已经扩展了一个类了该如何处理？
只要已经扩展的类是特质超类的一个子类就可以。
```
// UnhappyException扩展的类IOException是特质LoggedException的超类Exception的一个子类
class UnhappyException extends IOException with LoggedException  // OK
```

如果类扩展自一个不相关的类，那么就不可能混入这个特质了。

```
// UnhappyFrame扩展的类JFrame不是特质LoggedException的超类Exception的子类，错误！
class UnhappyFrame extends JFrame with LoggedException  // Error!!!
```
我们无发同时将JFrame和Exception作为父类。

---

---

## 4.13 特质中的自身类型(self type)【级别L2: 资深类库设计者】

当特质扩展一个类时，编译器能够确保的一件事就是所有混入该特质的类都认这个特质扩展类为超类。
通过例子再次解释上面的含义：
```
// 定义3个类
class Parent
class Son extends Parent
class D

// 一个特质扩展一个类
trait Logger extends Parent

// 下面来使用这个Logger特质，分两种情况
// 第一种情况：一个单纯的类
class A extends Logger // 这种方式，显然Parent类成为了类A的父类

// 第二种情况：还扩展了别的类的类
class B extends Son with Logger // 类B扩展了别的类Son，但是这里有个限制就是Son必须是Parent的子类。我们这里满足

// 如果是另外的情况：
class C extends D with Logger // 错误，扩展的别的类D并不是Parent类的子类，禁止！

// 综上所述：在使用一个扩展了类的特质时，或受到这个特质扩展的类的限制。
// 特质除了使用扩展一个类的方式之外，就是在特质中使用自身类型self type。

```

**自身类型**:
如果特质以 this: 类型type =>开始定义,那么这个特质就只能被混入type指定的类型的子类。
```
trait LoggedException extends Logged {
  this: Exception =>
  def log() { log(getMessage()) }
}
```
这里的特质LoggedException并不扩展Exception类，而是自身类型为Exception类型，意味着该特质只能被混入Exception的子类。这样指定了自身类型之后，可以调用自身类型的任何方法（这里调用了Exception类的getMessage方法）。因为我们知道this必定是一个Exception。

如果你把这个特质混入一个不符合自身类型要求的类，就会报错：
```
val f = new JFrame with LoggedException // 错误：LoggedException的自身类型为Exception，而JFrame并不是Exception的子类。

```

> 总结：带有自身类型的特质和带有超类型的特质很相似。两种情况都能确保混入该特质的类能够使用某个特定类型的特性。
在某些情况下，带有自身类型的特质比带有超类型的特质更灵活。
自身类型可以解决特质间的循环依赖问题（两个彼此需要的特质会产生循环依赖：TODO）。

自身类型还可以处理结构类型（structural type）——这种类型只给出了类必须拥有的方法，而不是类的名称。
```
trait LoggedException extends Logged {
  this: {def getMessage(): String} =>
  def log() { log(getMessage()) }
}
```

这个特质可以被混入任何拥有getmessage方法的类。

---

---

## 4.14 背后发生了什么
Scala将特质翻译为JVM的类和接口。

- 只有抽象方法的特质被简单的变成一个Java接口。
```
trait Logger { // Scala 特质
  def log(msg: String)
}
```
直接被翻译成：
```
public interface Logger { // 生成Java接口
  void log(String msg);
}

```

- 带有具体方法的特质，Scala会创建出一个伴生类，伴生类里的静态方法存放特质的具体方法
```
trait ConsoleLogger extends Logger {
  def log(msg: String) {
    println(msg)
  }
}

```
被翻译成：
``` 
public interface ConsoleLogger extends Logger{ // 生成Java接口
  void log (String msg);
}

public class ConsoleLogger$class { // 生成Java伴生类
  public static void log(ConsoleLogger self, String msg) {
    println(msg);
  }
}

```


---

---

References:

[1]. 【Scala for the impatient chapter 10】