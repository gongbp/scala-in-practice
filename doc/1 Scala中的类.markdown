# 1 Scala中的类

标签（空格分隔）： 级别A1:初级程序设计者 深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------

## 1.1 简单类和无参方法

Scala中最简单的类，形式上和Java的很相似。如果是使用Scala Style的话，不会这样来使用一个类。

```
// 不推荐在Scala中这样使用一个类，这是Java Style
class Counter {
  private var value = 0          // 字段必须初始化
  def increment() { value += 1 } // 方法默认为public, 修改器使用括号（），Use () with mutator
  def current = value            //读取器不使用括号（），Don’t use () with accessor
  /**
   * 首先对两个术语进行说明：
   *
   *  1. 声明一个方法，就是有方法的名称，参数列表，返回类型等。例如：
   *  abstract class Hello {
   *    def get(id: Int): String // 方法声明
   *  }
   *
   *  2. 方法定义，也就是方法实现，实现了一个完整的方法。例如：
   *  class Hello {
   *    // 方法定义
   *    def get(id: Int): String = s"id = $id, hello world!"
   *  }
   *
   *  对无参方法的定义：
   *    上面定义了两个无参方法，increment和current。
   *    但是一个使用了括号，而另一个没有使用括号。
   *    
   *    原因是涉及到一个约定俗成的规则：
   *    对于改变对象状态的方法【改值器方法】就使用()，比如，increment()改变了对象的状态就使用了括号
   *    而对于不改变对象的方法【取值器方法】就使用无括号调用，比如，current只是获得值，没有改变对象的状态，就不使用括号。
   *
   *  对无参方法的调用：
   *    1 如果无参方法有括号，调用时，括号可写可不写
   *      myCounter.increment
   *      myCounter.increment()
   *    2 如果无参方法无括号，则必须使用无括号调用
   *      myCounter.current
   *      myCounter.current() // 编译错误
   */
}

```

在Scala中，类并不声明为public。Scala源文件可以包含多个类，所有这些类都是public的。

---

---

## 1.2 带getter和setter的属性

- java中的例子：

```
public class Person { // 定义一个带有字段age的Java类
  public int age; // 声明了一个字段(field)，因为没有初始化，所以称为声明，而不是定义。Java中并不推荐这样写
}
```

如果这样使用公有字段的话，任何人都可以随便改变age的值。
所以，我们更趋向于使用getter和setter方法。

```
public class Person { // java推荐写法，带getter和setter的属性age
  private int age; // 声明了一个属性(property)，
  public int getAge() {return age;}
  public void setAge(int age) {this.age = age;}
}

```
>注意称呼的改变，第一个类中age是字段，第二个类中age是属性。
带有getter和setter的字段就是属性。

这样做的好处是什么呢？单纯的看上面的实现，属性除了比字段冗余了两个方法之外，并没有体现其价值啊？
答案非也。通过对getter和setter的引入，我们加强了对字段的控制权。其体现在：
```
// 通过对getter和setter的引入，可以在这两个方法里面引入判断逻辑
public void setAge(int newAge) {
  if(newAge > age) age = newAge;
}
```
如果单纯使用字段，就不能有这样的控制。
提供属性的话，我们可以在需要的时候进行合理的控制和改进。

---

- Scala中的例子

**因为scala对每个字段都提供getter和setter方法。所以Scala中的字段就等同于Java中的属性**。
在这里，我们定义一个公有字段：
```
class Person {
  var age = 0 // 默认是public的
}
```
Scala生成面向JVM的类，其中有一个私有字段age以及相应的getter和setter方法。
因为字段age默认是public的，所有这两个方法也是public的。如果定义一个私有的age字段`private var age = 0`，则对应的getter和setter方法也是private的。

在Scala中，getter就是age， setter为age_= 。例如：

```
$ scalac Person.scala

$ javap -private Person
Warning: Binary file Person contains scalaclass.Person
Compiled from "Person.scala"
public class scalaclass.Person {
  private int age;
  public int age();
  public void age_$eq(int);
  public scalaclass.Person();
}

// 编译器创建了age和age_$eq方法。（=号被翻译成$eq，是因为JVM不允许在方法名中出现= 。）

object Person extends App {
  val p = new Person
  println(p.age) // 0，会调用p.age()
  p.age = 10     // 会自动调用setter方法，p.age_= 10
  println(p.age) // 10，会调用p.age()
}

```

可重新定义getter和setter方法:
```
class Person {
  private var privateAge = 0 // 定义私有字段并改名

  def age = privateAge       // 自定义getter
  def age_=(newAge: Int) = { // 自定义setter
    if (newAge > privateAge) privateAge = newAge
  }
}

object Run extends App {
  val fred = new Person
  println(fred.age) // 0 
    
  fred.age = 21
  println(fred.age) // 21 

  fred.age = 0
  println(fred.age) // 21，新的年龄比当前年龄大才会修改
}
```

>说明：
Scala对每个字段都提供getter和setter，但是你也可以通过下面的方式来控制这个过程：
- 如果字段是私有的，则getter和setter方法也是私有的
- 如果字段是val，则只有getter方法生成
- 如果你不需要生成getter和setter方法，则可以将字段声明为private[this]

---

---

## 1.3 只有getter的Scala字段（等同于Java中的属性）

对Scala类中只提供只读字段，也就是只有getter方法而没有setter方法。
该字段在对象构建完成之后就不再改变，则使用val定义。
```
class Message {
  val timeStamp = new java.util.Date // a read-only property with a getter but no setter
}

$ scalac Message.scala
$ javap -private Message.class
Compiled from "Message.scala"
public class scalaclass.Message {
  private final java.util.Date timeStamp;
  public java.util.Date timeStamp();
  public scalaclass.Message();
}
```
Scala编译后生成一个private的final字段和一个getter方法，没有setter。

>总结：在实现字段时，有如下四种选择：
- var foo: Scala会自动合成一个getter和一个setter
- val foo: Scala会自动生成一个getter
- 自定义foo和foo_=方法
- 自定义foo方法

---

---


## 1.4 关于对象私有字段:

在Java中的例子：
```
public class Counter {
    private int value = 0; // 定义一个私有字段，仅仅局限在本类中以各种方式访问
    public void increment() {
        value += 1; // 方式1： 在本类中直接访问访问
    }
    public boolean isLess(Counter other) {
        return value < other.value; // 方式2： 在本类中通过对象来访问
    }
}

class Test {
    public static void main(String[] args) {
       Counter c = new Counter();
        c.value; // 在Counter之外访问，编译错误
    }
}
```
在Scala中的例子：
```
class Counter2 {
  private var value = 0 // 通过private关键字定义一个类私有字段，其实private就是private[Counter2]的简写
  def increment() { value += 1 } // 在本类中直接访问
  def isLess(other: Counter2) = value < other.value // 可以访问另一个对象的私有字段
}

object Test extends App {
  val c2 = new Counter2
  c2.value // 在Counter2类之外访问，编译出错
}

```
通过上面的两个例子，private修饰符在Scala和Java中的功效是一样的。
之所以可以在当前类中访问other.value，是因为other也是一个Counter对象。所以可以访问另一个对象的私有字段。
如果期望禁止这一点，也就是只允许当前对象可访问，别的Counter对象也不能访问的话，可以使用private[this]来修饰字段:
```
class Counter {
  private[this] var value = 0 // 通过private[this]定义一个对象私有字段，只允许当前对象访问
  def increment() { value += 1 }
  def isLess(other: Counter) = value < other.value // compile error!!! 别的Counter对象也不能访问
}
```
对于`类私有的字段【private修饰的字段】`，Scala生成私有的getter和setter方法。
但对于`对象私有的字段【private[this]修饰的字段】`，Scala根本不会生成getter和setter方法。

---

---

## 1.5 Bean属性
这是Java Style的写法，不推荐使用，略过！

```
import scala.beans.BeanProperty

class Person(@BeanProperty var name:String)
这样将会生成四个方法：

name:String
name_=(newValue:String):Unit
getName():String
setName(newValue:String):Unit

```

---

---

## 1.6 辅助构造器

Scala的类可以有任意多个构造器，但是最重要的就是它的主构造器。

**其他辅助构造器的名称都为this，每一个辅助构造器必须以一个对先前已定义的其他辅助构造器或主构造器的调用开始**。

带有两个辅助构造器的类的例子：
```
// 类不带参数，既主构造器不带参数
class Person {
  private var name = ""
  private var age = 0

  def this(name: String) { // 一个辅助构造器 
      this() // 调用主构造器：就是调用this时，传递的参数跟类参数一致。这里均为空参数。
      this.name = name
  }
  def this(name: String, age: Int) { // 另一个辅助构造器
      this(name) // 调用前一个辅助构造器
      this.age = age
  }
}
```
上面的Person类没有显示定义主构造器，在Scala中会自动获得一个无参的主构造器。
接下来就可以有三种方式来实例化一个Person对象：
```
val p1 = new Person             // 主构造器
val p2 = new Person("Jack")     // 第一个辅助构造器
val p2 = new Person("Tony", 30) // 第二个辅助构造器
```

---

另一个例子：
```
// 类带有一个参数，即主构造器也带有一个参数
class Random(val self: java.util.Random) {

  // 第一个辅助构造器
  def this() = this(new java.util.Random()) // 调用主构造器：就是调用this时，传递的参数跟类参数一致。这里均为java.util.Random类型
  
  // 第二个辅助构造器
  def this(seed: Long) = this(new java.util.Random(seed)) // 调用主构造器
  
  // 第三个辅助构造器
  def this(seed: Int) = this(seed.toLong) // 调用第二个辅助构造器
}  

```
接下来用四种方式来实例化一个Random对象：
```
val r1 = new Random(new java.util.Random) // 主构造器
val r2 = new Random()       // 第一个辅助构造器     
val r3 = new Random(10000L) // 第二个辅助构造器
val r4 = new Random(100)    // 第三个辅助构造器

```

通过上面的两个例子的对比，请分清楚到底哪一个才是主构造器。

---

---

## 1.7 主构造器

在Scala中，每个类都有主构造器。主构造器并不以this方法定义，而是与类定义交织在一起。

1. 主构造器的参数直接放置在类名之后。
```
class Person(val name: String, val age: Int) {
  // 类的参数就可以当做主构造器的参数
}
// 另一种等价写法：使用case class，构造参数不需要显示指定val修饰，case class默认就是val修饰的，
// 但是var在case class中也需要显示指定
case class Person(name: String, age: Int) {
  // 类的参数就可以当做主构造器的参数
}
```
**主构造器的参数如果有val/var修饰时，会被编译成字段，其值被构造时传入的参数初始化。**
在本例中，name和age成为Person类的字段。如new Person("Fred", 42)这样的构造器调用将设置name和age字段。

与之等价的Java中的啰啰嗦嗦的代码如下：
```
public class Person {
  private String name;
  private int age;
  
  public Person(String name, int age) {
    this.name = name;
    this.age = age;
  }
  
  public String getName() {
    return this.name;
  }
  public int getAge() {
    return this.age;
  }
}

```
虽然上面的代码可以通过IDE自动生成，但是在阅读代码时，没有Scala那般简洁，干净！

2.主构造器会执行类体中定义的所有语句（属于主构造器的语句）。
```
class Person(val name: String, private val age: Int) {
  println("Just constructed another person") // 这是主构造器的一部分。
  def description = name + " is " + age + " years old"
}
```
上面这个例子中，每当实例化一个Person对象时，主构造器会构造name和age字段，并同时执行println方法。
当你需要在构造过程中配置某个字段时，这个特性特别有用。例如：
```
class MyProg {
  private val props = new Properties
  props.load(new FileReader("myprog.properties"))
  //上述语句是主构造器的一部分
}
```
> 说明：如果类名之后没有参数，则该类具备一个无参数主构造器。这样的主构造器仅仅是执行类体中属于该主构造器的语句。

通常可以通过在主构造器中使用默认参数来避免过多的使用辅助构造器。
```
class Person(val name: String = "", val age: Int = 0)
```

3.主构造器的参数可以有多种修饰符。
例如：
```
class Person(val name: String, private var age: Int, private[this] val address: String)
```
再次强调，主构造器中的参数有var/val修饰的，会被编译成字段。

如果这些参数不带有var/val修饰的话，就是普通的方法参数。但是，这样的参数取决于在类中如何被使用。

①如果不带val或var，且这些参数至少被一个方法所使用，它将被升格为字段。
```
class Person(name: String, age: Int) {
  def description = name + " is " + age + " years old"
}
```
上述代码声明并初始化不可变字段name和age，而这两个字段是对象私有的。效果等同于private[this] val。

②否则，该参数将不被保存为字段。它仅仅是一个可以被主构造器中的代码访问的普通参数。

通过①②我们可得，类参数在类体中使用，则类参数会升级为
`private[this] val`修饰的对象字段。否则它只是一个可以被主构造器中的代码访问的普通参数。

针对主构造器参数生成的字段和方法：

|主构造器参数  |                        生成的字段/方法|
|--------------|---------------------------------------|
|name: String  |                                       对象私有字段。如果没有方法使用name，则无该字段         |
|private val/var name: String|        类私有字段，私有getter和setter方法                     |
|val/var name: String|             类私有字段，公有getter和setter方法                     |
|@BeanProperty val/var name: String| 类私有字段，公有的Scala版和JavaBeans版的getter和setter方法|

4.主构造器私有化。

如果想让主构造器变成私有的，可以在类名之后参数列表之前添加private关键字修饰即可。
```
class Person private(val id: Int){}
```
这样一来用户就必须通过辅助构造器来构造Person对象了。

private主构造器的例子:
```
// 无参数的类
class Order1 {}

// 无参数的类，私有主构造器。第一种写法: 省略空参数列表的括号
class Order2 private {
  // 定义一个辅助构造器
  def this(orderId: Long) {
    this // 辅助构造器的第一句必须是调用主构造器，如果没有调用或者不是在第一句的位置调用，编译报错！
    println(orderId)
    // more code here ...
  }
}

// 无参数的类，私有主构造器，第二种写法: 显示写出空参数列表
class Order22 private() {
  // 定义一个辅助构造器
  def this(orderId: Long) {
    this // 或者this() 辅助构造器的第一句必须是调用主构造器，如果没有调用或者不是在第一句的位置调用，编译报错！
    println(orderId)
    // more code here ...
  }
}

// 含有参数的类
class Order3 private(orderId: Long, price: Double) {
  // 定义一个辅助构造器
  def this(orderId: Long) {
    this(orderId, 0) // 调用主构造器
    println(orderId)
    // more code here ...
  }
}

object PrivateConstructorTests extends App {
  //  val o = new Order  // this won't compile
  val o22 = new Order22(10L) // 调用辅助构造器
  
 //  val o3 = new Order3(1, 10.0) // 编译错误
  val o3 = new Order3(1) // 调用辅助构造器
}
```

---

---

## 1.8 样本类Case Class

上面介绍了Scala中关于类的各种细节，不难发现，很多都跟Java的类似。
如果真按照上面的搞法，归根结底，还是Java Style。Scala不是很推崇Java Style。
而在真正使用Scala中的Class时，几乎都是使用Scala推崇的immutable编程，这样的话，无一例外的都使用`case class`:
```
// 主构造器参数默认val修饰，所以生成的字段只有一个getter方法
// 并且该字段的值不能修改，不可变
scala> case class Person(name: String, age: Int)
defined class Person

scala> val p = Person("jack", 30)
p: Person = Person(jack,30)

scala> p.name
res3: String = jack

scala> p.age
res4: Int = 30

scala> p.name = "tony" // 字段值不可变
<console>:10: error: reassignment to val
       p.name = "tony"
              ^

scala> val p2 = p.copy("tony") // immutable方式，copy后生成一个新对象
p2: Person = Person(tony,30)

scala> p2.name
res5: String = tony

```

---

---

References:

[1]. 【Scala for the impatient chapter 5】