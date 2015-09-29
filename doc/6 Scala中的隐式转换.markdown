# 6 Scala中的隐式转换

标签（空格分隔）： 深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------

## 6.1 隐式转换背景介绍

在进入本篇之前，我们先回答三个问题。
>第一个：什么是隐式转换？`隐式转换的概念`

    答:隐式转换：就是是把隐式常量/变量，隐式函数/方法及隐式类自动应用的过程。
    
    - 隐式常量/变量：主要被自动应用于含有隐式参数的函数/方法
    
    - 含有隐式参数的函数/方法：隐式参数的值来源于作用域内隐式常量/变量，或者调用函数/方法的隐式参数。
    
    - 隐式函数/方法：隐式函数/方法一般用于将值从一种类型转换为另一种类型，一般转换后的类型提供了更加丰富的功能。
    
    - 隐式类：隐式类在原理上其实就是在创建类的同时构建了一个与主构造器同名的隐式函数。注意：隐式类不能定义成为顶级对象。

- 隐式常量/变量的例子：
```
object ImplicitVariable extends App {
  
  implicit var a = 10 // 隐式变量
  
  def plus(implicit b: Int) = b + 10 // 含有隐式参数的方法
  
  println(plus) // 20
}
```

- 隐式方法的例子（隐式方法的名称是任意的）：

```
object ImplicitFunction extends App {

  /** add a ? method for String type */
  case class RichString(content: String) {
    def ? = StringUtils.isNotBlank(content) match {
      case true => content
      case false => "default value"
    }
  }

  /** 
   * This is a implicit function conversion for simplified syntax support 
   * 隐式方法
   */
  implicit def StringToGetParam(s: String): RichString = RichString(s)

  println("".?) // default value
}
```
>补充：这里对隐式函数和隐式方法进行区分，直接上例子：
```
// 隐式方法（Method）
implicit def StringToGetParam(s: String): RichString = RichString(s)

// 隐式函数（Function），各种写法，一种效果：
implicit val StringToGetParam = (s: String) => RichString(s)
implicit def StringToGetParam = (s: String) => RichString(s)
implicit def StringToGetParam: String => RichString = s => RichString(s)
```

- 隐式类的例子：

```
object Test extends App { 
  implicit class Calc(i: Int) { 
    def show { println("input = " + i) } 
  } 
  5.show  // input = 5
} 
```

------------------------------------------------------------

>第二个：为什么要使用隐式转换？`隐式转换的功能及可以解决的问题`

    答：使用隐式转换可以丰富现有类的功能；利用隐式转换可以提供优雅的类库，对类库的使用者隐藏掉繁琐的细节。
    
------------------------------------------------------------  

>第三个：怎么使用隐式转换？`隐式转换的使用方法`

    答：隐式转换一般通过三种方式来实现：
    - 隐式常量/变量
    - 隐式函数/方法
    - 隐式类
    具体实现方式请看下面内容。
    
------------------------------------------------------------

------------------------------------------------------------

## 6.2 利用隐式转换丰富现有类库的功能
### 6.2.1 通过隐式方法实现

你是否曾希望某个类有某个方法，而这个类的作者却没有提供？举例来说，如果java.io.File类能有个read方法来读取文件，这该多好：
```
val contents = new File("README").read
```
在Scala中，你可以定义一个经过丰富的File类型，提供你想要的功能：
```
class RichFile(val from: File) { 
  def read = Source.fromFile(from.getPath).mkString 
}
```
然后，再提供一个隐式转换（通过隐式方法实现）来将原来的类型转换到这个新的类型，
既File => RichFile:
```
implicit def file2RichFile(from: File) = new RichFile(from)
```
这样一来，你就可以在File对象上调用read方法了。它被隐式地转换成了一个RichFile。

------------------------------------------------------------

### 6.2.2 通过隐式类实现

由于隐式类不能定义成为顶级对象，所以需要为隐式类定义一个命名空间：

```
object Outer extends App {
  implicit class RichFile(file: File) {
    def read = Source.fromFile(file.getPath).mkString
  }
  new File("README").read
}
```

------------------------------------------------------------

------------------------------------------------------------

## 6.3 引入隐式转换（将需要的隐式转换引入到执行上下文）

Scala在隐式转换时，寻找隐式转换函数/方法的方式：

>- 位于源或目标类型的伴生对象中的隐式函数/方法，两者选其一 
``` 
class Info // 源类型

object Info { // 源类型的伴生对象，位置1
  implicit def info2Rich(info: Info): RichInfo = new RichInfo(info)
}

class RichInfo(info: Info) { // 目标类型
  def show {
    println("RichInfo ...")
  }
}

object RichInfo { // 目标类型的伴生对象，位置2
  // implicit def info2Rich(info: Info): RichInfo = new RichInfo(info)
}

object Test extends App {
  val info = new Info
  info.show
}
```
>- 位于当前作用域可以以单个标识符指代的隐式函数/方法
```
class Info  
class RichInfo(info: Info) { 
  def show { 
    println("RichInfo ...") 
  } 
} 
object Test extends App { 
  // 当前作用域可以以单个标识符指代的隐式函数
  // 也就是当前只有一个从Infor => RichInfo的转换
  implicit def info2Rich(info: Info) = new RichInfo(info) 
  val info = new Info 
  info.show 
} 
```
输出：

    RichInfo ... 
    
info对象本身没有show方法，在info上下文中，定义了一个隐式方法，scala编译器会自动调用此方法将info转换为RichInfo，然后就可以调用show方法了。

>补充：在REPL中，输入`:implicits`以查看所有除Predef外被引入的隐式成员，或者输入`:implicits -v`以查看全部引入的隐式成员。

------------------------------------------------------------

------------------------------------------------------------


## 6.4 隐式参数
```
object Test extends App { 
  def testImplicit(implicit name: String) { 
    println(name) 
  } 

  implicit val name = "test implicit..." 
  testImplicit 
  testImplicit("Test ...") 
} 
```
输出：

    test implicit... 
    Test ...  

函数或方法可以带有一个标记为implicit的参数列表。这种情况下，编译器将会查找缺省值，提供给该函数或方法。

------------------------------------------------------------

以下是另一个简单的示例：
```
case class Delimiters(left: String, right: String)

def quote(what: String)(implicit delims: Delimiters) = 
  delims.left + what + delims.right 
``` 
你可以用一个显示的Delimiters对象来调用quote方法，就像这样：
```
quote("TonyGong")(Delimiters("<<", ">>")) // 将返回<<TonyGong>>
```
如果略去隐式参数列表调用quote方法，编译器会查找一个类型为Delimiters的隐式值。这必须是一个被声明为implicit的值。编译器将会在如下两个地方查找这样的一个对象：

 - 在当前作用域所有可以用单个标识符指代的满足类型要求的val和def
 - 与所要求类型相关联的类型的伴生对象

在我们的示例中，我们可以提供一个对象用于提供隐式值：
```
object ImplicitsObject {
 implicit val quoteDelimiters = Delimiters("<<", ">>")
}
```
从ImplicitsObject对象中引入隐式值，并调用quote方法：
```
import ImplicitsObject._
quote("TonyGong")
```
如此一来，定界符（<<和>>）就被隐式地提供给了quote函数。

------------------------------------------------------------

------------------------------------------------------------

## 6.5 利用隐式参数进行隐式转换

隐式的函数参数也可以被用作隐式转换。为了明白它为什么重要，首先考虑如下这个泛型函数：
```
def smaller[T](a: T, b: T) = if(a < b) a else b // not working
```
编译失败，因为编译器并不知道a和b属于一个带有< 操作符的类型。
我们可以提供一个转换函数来达到目的：
```
def smaller[T](a: T, b: T)(implicit order: T => Ordered[T]) = if(order(a) < b) a else b 
```
注意order是一个带有单个参数的函数，被打上了implicit标签，并且有一个以单个标识符出现的名称。因此，它不仅是一个隐式参数，它还是一个隐式转换。所以，我们可以在函数体中略去对order的显示调用：
```
def smaller[T](a: T, b: T)(implicit order: T => Ordered[T])
 = if(a < b) a else b // 如果a没有带<操作符的话，会调用order(a) < b
```
------------------------------------------------------------

------------------------------------------------------------

## 6.6 上下文界定（Context Bounds）

类型参数可以有一个形式为`T:M`的上下文界定，其中M是另一个`泛型类型`。它要求作用域中存在一个类型为`M[T]`的隐式值。例如：
```
class Pair[T: Ordering]
```
要求存在一个类型为`Ordering[T]`的隐式值。该隐式值可以被用在该类的方法当中，考虑如下示例：
```
class Pair[T: Ordering](val first: T, val second: T) { 
  def smaller(implicit ord: Ordering[T]) = 
     if(ord.compare(first, second) < 0) first else second
}
```
如果我们new一个Pair(40, 2),  编译器将推断出我们需要一个Pair[Int]。由于Predef作用域中有一个类型为Ordering[Int]的隐式值，因此Int满足上下文界定。这个Ordering[Int]就成为该类的一个字段，被传入需要该值的方法当中。
如果你愿意，你也可以用Predef类的implicity方法获取该值：
```
class Pair[T: Ordering](val first: T, val second: T) { 
  def smaller = 
    if(implicitly[Ordering[T]].compare(first, second) < 0) first else second
}
```

------------------------------------------------------------

------------------------------------------------------------

## 6.7 类型约束（Type Constraints）
### 6.7.1 基本概念
类型约束用于限定类型，现在有两种关系可供使用：

    A =:= B  // 校验 A类型是否等于B类型  
    A <:< B  // 校验 A类型是否是B的子类型  
    
2.10之前还有一个`A <%< B`类似于view bound，表示A可以当作B，即A隐式转换成B也满足。但在2.10里已经废弃这种写法。

这个看上去很像操作符的`=:=` 和 `<:<`，实际是一个类，它在Predef里定义：

```
sealed abstract class =:=[From, To] extends (From => To) with Serializable

sealed abstract class <:<[-From, +To] extends (From => To) with Serializable
```

它定义了两个类型参数，所以可以使用中缀写法：`From <:< To`。

------------------------------------------------------------

### 6.7.2 如何使用

要使用这些类型约束，做法是提供一个隐式参数。比如：
```
def firstLast[A, C](it: C)(implicit ev: C <:< Iterable[A]) =   (it.head, it.last)
```


类型约束用在特定方法(specialized methods)的场景，所谓特定，是指方法只针对特定的类型参数才可以运行:
```
def test[T](i:T)(implicit ev: T <:< java.io.Serializable) {
  print("OK") 
}
```
```
scala> test("hi") // OK
scala> test(2)
<console>:9: error: Cannot prove that Int <:< java.io.Serializable.
```
上面定义的test方法，在方法的第二个参数使用了一个隐式参数ev，它的类型是:`T<:<java.io.Serializable`，表示只有参数类型T是java.io.Serializable的子类型，才符合类型要求。

或许你会奇怪上面test方法调用”hi”时，隐式参数ev是从哪儿传入的？
当前并没有定义这个隐式参数。这个隐式参数也是由Predef里的隐式方法产生的。
```
private[this] final val singleton_<:< = new <:<[Any,Any] {
  def apply(x: Any): Any = x 
}

implicit def conforms[A]: A <:< A = singleton_<:<.asInstanceOf[A <:< A]
```

当调用test("hi")，编译器推断出T是String，于是开始寻找 `String <:< java.io.Serializable`类型的隐式参数。
但是在上下文中找不到，于是通过conforms隐式方法来产生一个，conforms方法只有一个类型参数，它产生的结果是`<:<[String,String]`，也既是`String <:< String`类型的对象。
但因为`<:<[-From,+To]`第一个类型参数是逆变的，第二个类型参数是协变的，所以`<:<[String,String]`符合`<:<[String,java.io.Serializable]`的子类，满足要求。

而调用test(2)时，因为隐式方法产生的<:<[Int,Int]不符合<:<[Int,java.io.Serializable]子类型，抛出了异常。
可见这块编译器是利用函数类型的多态机制来实现类型检测的。

------------------------------------------------------------

### 6.7.3 补充说明

另外，对于Type类型，在判断之间的关系时也有类似的写法，不过这里是Type类型的方法：
```
scala> import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe._

scala> typeOf[List[_]] =:= typeOf[List[AnyRef]]
res4: Boolean = false

scala> typeOf[List[Int]] <:< typeOf[Iterable[Int]]
res1: Boolean = true
```
上面的是方法调用：typ1.=:=(typ2)，虽然效果都是证明类型关系，但不要混淆。

------------------------------------------------------------

### 6.7.4 <:与<:<的差异
```
object A{
  def test[T <: java.io.Serializable](i:T) {}
  test(1) // 编译时报错

  def test2[T](i:T)(implicit ev: T <:< java.io.Serializable) {}
  test2(1) // 同样编译时报错
}
```
两者的效果似乎一样，应该怎么选择？
[stackoverflow的解释如下:](http://stackoverflow.com/questions/19829770/whats-different-between-and-in-scala)

```
def foo[A, B <: A](a: A, b: B) = (a,b)

scala> foo(1, List(1,2,3))
res1: (Any, List[Int]) = (1,List(1, 2, 3))
```

传入第一个参数是Int类型，第二个参数是List[Int]，显然这不符合 B <: A 的约束，编译器在做类型推导的时候，为了满足这个约束，会继续向上寻找父类型来匹配是否满足，于是在第一个参数被推导为Any类型的情况下，List[Int] 符合Any的子类型。

```
def bar[A,B](a: A, b: B)(implicit ev: B <:< A) = (a,b)

scala> bar(1,List(1,2,3))
<console>:9: error: Cannot prove that List[Int] <:< Int.
```

通过隐式参数ev来证明类型时，类型推断过程不会像上面那样再向上寻找可能满足的情况，而直接报错。

确实，在用 `<:` 声明类型约束的时候，不如用`<:<`更严格。除了上面的类型推导之外，还存在隐式转换的情况下：
```
scala> def foo[B, A<:B] (a:A,b:B) = print("OK")

scala> class A; class B;

scala> implicit def a2b(a:A) = new B

scala> foo(new A, new B)  //存在A到B的隐式转换，也可满足
OK

scala> def bar[A,B](a:A,b:B)(implicit ev: A<:<B) = print("OK")

scala> bar(new A, new B)  //隐式转换并不管用
<console>:17: error: Cannot prove that A <:< B.
```
>另外`<:`和`<:<`的位置：
  `<:` 用在类型参数的位置`def foo[B, A<:B] (a:A,b:B)`
  `<:<` 用在隐式参数类型的位置 `def bar[A,B](a:A,b:B)(implicit ev: A<:<B)`
  

---

---

References:

[1]. 【Scala for the impatient chapter 21】










