# 5 Scala中的类型参数

标签（空格分隔）： 级别L2:资深类库设计者 深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------

Scala中的类型参数其表现形式，主要通过`泛型类`，`泛型Trait`和`泛型函数/方法`。


## 5.1 类型参数，参数类型，参数化的类型区别

以Array[T]为例：

T就是`类型参数`，或者称为`参数类型`。`类型参数（type parameter）`和`参数类型(parameter type)`是一个意思，都是指T。

Array[T]这个整体就称为`参数化的类型（parameterized type）`。
    
------------------------------------------------------------

------------------------------------------------------------


## 5.2 泛型类（带有一个或多个类型参数的类）

　　`类class`，`特质trait`以及`函数/方法`可以带类型参数。在Scala中，用方括号来定义类型参数，例如：
```
class Pair[T, S](val first: T, val second: S)
```
　　以上将定义一个带有两个类型参数T和S的类。这个类被称为参数化的类型Pair[T, S]。
　　
　　在类的定义中，你可以用类型参数来定义`变量/常量`的类型，`方法参数`的类型以及`返回值`的类型。

　　带有一个或多个类型参数的类是泛型的。如果你把类型参数T，S替换成实际的类型，将得到一个具体的类，比如Pair[Int, String]。

　　Scala具有类型推断能力，下面的代码是等价的：
```
val p = new Pair[Int, String](10, "String")

// 等价于
val p = new Pair(10, "String") // Scala从构造方法的参数自动推断出这是一个Pair[Int, String]的类型
```

------------------------------------------------------------

------------------------------------------------------------

## 5.3 泛型函数（带有一个或多个类型参数的函数）

　　函数和方法也可以带类型参数。和泛型类一样，你需要把类型参数放在函数名之后。以下是一个简单的示例：
```
// 返回数组的中间元素
def getMiddle[T](a: Array[T]) = a(a.length / 2)
```
接着调用getMiddle方法：
```
getMiddle(Array("hello", "world", "!")) // Scala自动推断出getMiddle[String]

getMiddle[String](Array("hello", "world", "!")) // 手动指定类型参数
```

------------------------------------------------------------

>小结：类型参数（参数类型）出现的位置在类名，特质名，方法名之后：
```
class Pair[T] // 出现在类名之后：方括号中的T称为类型参数，而Pair[T]则被称为参数化的类型
trait Pair[T] // 出现在特质名之后：方括号中的T称为类型参数，而Pair[T]则被称为参数化的类型
def getMiddle[T] // 出现在方法名之后：方括号中的T称为类型参数，此种情况无参数化的类型
```

------------------------------------------------------------

------------------------------------------------------------


## 5.4 对类型参数的限定（对类型参数范围的指定）

　　对类型参数的限定就是对类型参数范围的指定，使这个类型参数有一个更明确的限定范围，从而在这个类型参数未被具体化的时候，也能够在一定程度上知道该类型参数所代表的类型的某些行为。主要通过下面三种方式来进行限定：

`上界(upper bounds)  A <: B`
`下界 (lower bounds) B >: A`
`上下文界定(context bounds) A : B`

### 5.4.1 上界（upper bounds）的使用场景

　　为类型参数指定上界进行限定后，就表明这个上界是这个类型参数代表的类型的父类，从而就可以确定这个类型参数代表的类型肯定有上界指定的类型中的行为。
　　例如：T <: Comparable[T] 说明，类型T肯定包含有Comparable中的方法。因为T是Comparable[T]的子类。
　　
　　直接通过例子来说明为什么需要使用`上界upper bounds`。
　　考虑这样一个Pair类型，它要求它的两个参数类型相同：
```
class Pair[T](val first: T, val second: T) {
  def smaller = 
    if (first < second) first else second // 编译错误
}
```
　　类型为T的值并不一定有`<`操作符或被叫作为`<`的方法。
　　为了解决T类型的值可以进行大小比较，我们可以为T类型添加一个`上界upper bounds` T<:Comparable[T]。也就是T必须为Comparable[T]的子类型。
　　上界Comparable[T]这个父类中提供了两个值进行大小比较的方法，作为这个上界的子类T，必然也就拥有了这个上界父类中的方法，所以可以进行比较。示例代码如下：
```
class Pair[T <: Comparable[T]](val first: T, val second: T) {
  def smaller = 
    if(first.compareTo(second) < 0) first else second // 编译成功
}
```
　　这样一来，我们在实例化Pair[T]的时候，一定要保证T的实际类型必须是Comparable[T]的子类型。

　　例如：
　　String是Comparable[String]的子类型，所以可以实例化Pair[String]。
　　File **不**是Comparable[File]的子类型，所以**不**可以实例化Pair[File]。
　　如果你尝试实例化new Pair(4, 2)，则会编译出错，因为Scala通过类型推断，自动推断出T=Int，界定T <: Comparable[T]无法满足。因为Int <: Comparable[Int]不满足，Int不是Comparable[Int]的子类。

------------------------------------------------------------

### 5.4.2 下界（lower bounds）的使用场景

　　下界的限定就是类型参数的类型必须等于下界或者是下界的父类。
　　例如：
　　T >: List[Int]，要求T必须等于下界List[Int]或者是List[Int]的父类。当T为Traverable[Int]时，那么使用T类型的地方，任何Traverable的子类都可以使用，比如Set[Int]，尽管不是List[Int]的父类，但是也可以使用。

　　举例来说，假定我们要定义一个方法，用另一个值替换对偶的第一个参数。我们的对偶是不可变得，因此我们需要返回一个新的对偶。以下是我们的首次尝试：
```
// 父类
class Person(name: String){
  override def toString = s"hello, $name"
}
// 子类之一
class Student(name: String) extends Person(name) {
  override def toString = s"hello, $name"
}
// 子类之二
class Teacher(name: String) extends Person(name) {
  override def toString = s"hello, $name"
}
// 对偶类
class Pair[T](val first: T, val second: T) {
  def replaceFirst(newFirst: T) = new Pair(newFirst, second)
}
// test类
object Pair extends App {

  // 确定T为Student类型
  val s: Pair[Student] = new Pair(new Student("张三"), new Student("李四"))
  
  // 那么replaceFirst方法的参数类型也必须为Student类型
  val s1: Pair[Student] = s.replaceFirst(new Student("王五"))
  
  // replaceFirst方法的参数类型为Student的父类，编译错误，类型不匹配
  val p: Pair[Person] = s.replaceFirst(new Person("王五")) 
  
  // replaceFirst方法的参数类型与Student类有共同父类，编译错误，类型不匹配
  val p1: Pair[Person] = s.replaceFirst(new Teacher("王麻子"))
}
```

　　上面的Person类既然是Student类的父类，我们当然希望replaceFirst方法也可以应用于Student类的父类Person。改进如下：

```
// 对偶类
class Pair[T](val first: T, val second: T) {
  // 为了清晰起见，给返回的对偶也明确写上类型参数
  def replaceFirst[U >: T](newFirst: U) = new Pair[U](newFirst, second)
  
  /**
  // 你也可以对返回类型不明确指定类型参数，通过Scala自动类型推断
  def replaceFirst[U >: T](newFirst: U) = new Pair(newFirst, second)
  */
}
// test类
object Pair extends App {
  val s: Pair[Student] = new Pair(new Student("张三"), new Student("李四")) // Student类型
  
  val s1: Pair[Student] = s.replaceFirst(new Student("王五")) // Student类型
  
  val p: Pair[Person] = s.replaceFirst(new Person("王五")) // 返回Student的父类型Person
  
  val p1: Pair[Person] = s.replaceFirst(new Teacher("王麻子")) // 返回Student的父类型Person
}
```

　　通过这个改进，带来的额外的好处就是，Teacher类跟Student类没有任何关系，但是他们都有一个公共的父类Person。所以通过下界的限定，def replaceFirst[U >: T]不但可以接收Person类型，还可以接收Person的其他子类型参数。

>注意: 如果不指定下界，那么这个replaceFirst方法返回的类型就是`newFirst: U`和`val second: T`的公共父类型。
```
class Pair[T](val first: T, val second: T) {
  // 返回的类型为U和T的公共父类型
  def replaceFirst[U](newFirst: U) = new Pair(newFirst, second)
}
```

------------------------------------------------------------

### 5.4.3 上下文界定（context bounds）

　　类型参数可以有一个形式为`T:M`的上下文界定，其中M是另一个`泛型类型`。它要求作用域中存在一个类型为`M[T]`的隐式值。

```
// 上下文界定
class Pair[T : Ordering]
```
　　上述定义要求必须存在一个类型为Ordeing[T]的隐式值。该隐式值会成为Pair类的一个字段，可以被用在该Pair类的带有隐式参数的方法中。这个带有隐式参数的方法会自动应用作用域内的类型为Ordeing[T]隐式值。以下是一个示例：
```
class Pair[T : Ordering](val first: T, val second: T) {
  // 1. 编译后，Pair里面会有一个隐式的Ordering[T]的字段
  // 2. 定义一个带有隐式参数的方法，调用方法时，会自动获取Pair类的隐式字段Ordering[T]
  def smaller(implicit ord: Ordering[T]) = 
    if (ord.compare(first, second) < 0 ) first else second 
} 
```

　　如果我们new一个Pair(40, 2),  编译器将推断出我们需要一个Pair[Int]。由于Predef作用域中有一个类型为Ordering[Int]的隐式值，因此Int满足上下文界定。这个Ordering[Int]就成为该类的一个字段，被传入需要该值的方法（一个带有隐式参数的方法）当中。
如果你愿意，你也可以用Predef类的implicity方法获取该值：
```
class Pair[T: Ordering](val first: T, val second: T) { 
  def smaller = 
    if(implicitly[Ordering[T]].compare(first, second) < 0) first else second
}
```

>说明：为什么要引人上下文限定？

　　上下文界定（Context Bound），是Scala为隐式参数引入的一种语法糖，使得隐式转换的编码更加简洁。
　　通过下面的例子说明：
　　首先引入一个泛型函数max，用于获取a和b的最大值。
```
def max[T](a: T, b: T) = if (a > b) a else b  
```
　　因为T是未知类型，只有在运行时才会确定真正的类型，因此调用a > b是不正确的，因为T不确定，也就不确定T是否有`>`这个方法实现。
 
　　通过引入类型隐式转换可以解决上面的问题，因为Ordering类型是可比较的，因此定义一个类型的隐式转换，将T转换为Ordering[T]，所以只要执行上下文有这个隐式转换，就可以进行比较：
```
// 实现一个带有隐式参数的max方法，在调用这个Max方法时，引入一个Ordering[T]的隐式值
def max[T](a: T, b: T)(implicit t: Ordering[T]) = 
  if (t.compare(a, b) < 0) b else a
``` 
 
　　上面的方法实现，对使用者来说，不够友好，最好把隐式参数的部分隐藏起来，于是通过Context Bound来省略隐式参数:

```
// 1 推荐的做法，implicitly是在Predef.scala里定义的，它是一个特殊的方法，编译器会记录当前上下文里的隐式值，而这个方法则可以获得某种类型的隐式值。
def max[T : Ordering](a: T, b: T) = 
    if (implicitly(Ordering[T]).compare(a, b) < 0) b else a

// 2 在内部定义函数并声明隐式参数
def max[T : Ordering](a: T, b: T) = {
    def compare(implicit ev1: Ordering[T]) = ev1.compare(a, b)
    if(compare > 0) a else b
}
```

------------------------------------------------------------

------------------------------------------------------------
## 5.5 ClassTag上下文限定（ClassTag context bounds）（最常用）

### 5.5.1 背景介绍（为什么要使用ClassTag）

　　Java中的泛型基本上都是在编译器这个层次来实现的。在生成的Java字节码中是不包含泛型中的类型信息的。使用泛型的时候加上的类型参数，会在编译的时候去掉，这个过程就称为类型擦除。泛型擦除是为了兼容jdk1.5之前的jvm，在这之前是不支持泛型的。
　　泛型在编写和编译的时候，不能确定具体的类型，但是虚拟机运行的时候必须要具体的类型，所以ClassTag会帮助我们存储这个运行时的类型，并通过反射把这个运行时的类型传递给虚拟机。

### 5.5.2 ClassTag

　　scala在2.10里用用ClassTag替代了ClassManifest，原因是在路径依赖类型中，ClassManifest存在问题（ClassManifest已过期）。
　　ClassTag[T]保存了被泛型擦除后的原始类型T,提供给运行时使用。
```
scala> import scala.reflect.ClassTag
import scala.reflect.ClassTag

scala> def mkArray[T : ClassTag](elems: T*) = Array[T](elems: _*)
mkArray: [T](elems: T*)(implicit evidence$1: scala.reflect.ClassTag[T])Array[T]

scala> mkArray(1, 2)
res0: Array[Int] = Array(1, 2)

scala> mkArray("Jim", "Lucy")
res1: Array[String] = Array(Jim, Lucy)

```


------------------------------------------------------------

------------------------------------------------------------

## 5.6 TypeTag上下文限定（TypeTag context bounds）
　  scala在2.10里用TypeTag替代了Manifest，原因是在路径依赖类型中，Manifest存在问题（Manifest已过期）。实例代码如下：

```
scala> class Foo{class Bar}
defined class Foo

scala> val f1 = new Foo;val b1 = new f1.Bar
f1: Foo = Foo@48c4245d
b1: f1.Bar = Foo$Bar@3df978b9

scala> val f2 = new Foo;val b2 = new f2.Bar
f2: Foo = Foo@5ac7aa18
b2: f2.Bar = Foo$Bar@4cdd2c73

// 使用Manifest的例子
scala> def m(f: Foo)(b: f.Bar)(implicit ev: Manifest[f.Bar]) = ev
m: (f: Foo)(b: f.Bar)(implicit ev: Manifest[f.Bar])Manifest[f.Bar]

scala> val ev1 = m(f1)(b1)
ev1: Manifest[f1.Bar] = Foo@48c4245d.type#Foo$Bar

scala> val ev2 = m(f2)(b2)
ev2: Manifest[f2.Bar] = Foo@5ac7aa18.type#Foo$Bar

scala> ev1 == ev2 // // they should be different, thus the result is wrong
res3: Boolean = true

// 使用TypeTag的例子
scala> import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe._

scala> def m2(f: Foo)(b: f.Bar)(implicit ev: TypeTag[f.Bar]) = ev
m2: (f: Foo)(b: f.Bar)(implicit ev: reflect.runtime.universe.TypeTag[f.Bar])reflect.runtime.universe.TypeTag[f.Bar]

scala> val ev3 = m2(f1)(b1)
ev3: reflect.runtime.universe.TypeTag[f1.Bar] = TypeTag[f1.Bar]

scala> val ev4 = m2(f2)(b2)
ev4: reflect.runtime.universe.TypeTag[f2.Bar] = TypeTag[f2.Bar]

scala> ev3 == ev4 // This is right!
res4: Boolean = false
```

TypeTag保存所有具体的类型
```
import scala.reflect.runtime.universe._

def paramInfo[T](x: T)(implicit tag: TypeTag[T]): Unit = {
  val targs = tag.tpe match { case TypeRef(_, _, args) => args }
  println(s"type of $x has type arguments $targs")
}

scala> paramInfo(42)
type of 42 has type arguments List()

scala> paramInfo(List(1, 2))
type of List(1, 2) has type arguments List(Int)
```

------------------------------------------------------------

------------------------------------------------------------

## 5.7 多重限定
　  Scala 多重界定分为以下几种：
```
// 1. 只能有一个上界
T <: A with B  类型参数T不能同时有多个上界或下界，不过可以有一个类型混入了多个Trait，T是A或B的子类 

// 2. 只能有一个下界 
T >: A with B  A或B是T的子类，一般不用

// 3. 可以同时有上界和下界 
T >: A <: B  必须先是下界，再跟着上界，顺序不能颠倒，A下是界，B是上界  ，A是B的子类

// 4. 可以同时有多个上下文界定 
T: A : B   上下文界定，T必须同时满足存在A[T]和B[T]的隐试转换值

// 5. 可以同时有多个视图界定，2.10后过期 
T <% A <% B      视图界定   T既可以转换成B也可以转换成A 
```

　  类型不可以有多个上界或下界，如果想有多个上界或下界，这样语法是不正确的。
spark中常用的就是`<:`

------------------------------------------------------------

------------------------------------------------------------

## 5.8 类型约束（对类型参数的约束）【级别L3: 专家类库设计者】

　  类型约束总共有三种关系可供使用:
```
T =:= U
T <:< U
T <%< U 2.10之后已废弃
```
　  这些约束将会测试T是否等于U，是否为U的子类型，能否被视图(隐式)转换为U。`类型约束`对比`类型界定`的例子：
```
// 类型界定
class Pair[T <: Comparable[T]](val first : T, val second : T){
  def smaller = if (first.compareTo(second) < 0) first else second
}

// 类型约束，更严格
class Pair[T](val first : T, val second : T)(implicit ev: T <:< Comparable[T] ) {
  def smaller = if (first.compareTo(second) < 0) first else second
}
```
　  这个例子并没有看出类型约束相比于类型变量界定有和优势。其实类型约束比类型界定更严格。如果上面的例子有从T到Comparable[T]的隐式转换，那么类型界定可以，而类型约束却禁止。

　  下面再举出类型约束的两个用途。
　  第一，类型约束让你可以在泛型类中定义只能在特定条件下使用的方法:
```
class Pair[T](val first : T, val second : T) {
  def smaller(implicit ev : T <:< Comparable[T] ) = if (first.compareTo(second) < 0) first else second
}
```
　  构造Pair时，如果T的类型不是Comparable[T]的子类，则调用smaller方法时报错。
　  例如：构造Pair[File]，File不是Comparable[T]的子类。那么调用smaller的时候才会报错。

　  一个更明显的例子是Option类的orNull方法，你可以构建任何类型的Option[T]，但是Option里面的orNull方法有类型约束：
```
@inline final def orNull[A1 >: A](implicit ev: Null <:< A1): A1 = this getOrElse ev(null)
```
　  如果T的类型为值类型（AnyVal），那么调用orNull方法就会报错。因为Null是引用类型，它要求T必须是引用类型。

　  见Scala的继承图：
![](http://thumbsnap.com/i/MXFillwv.png)

　  例如：
```
val m = Map("a"->1,"b"->2)
val mOpt = m.get("c") //  Option[Int]
val mOrNull = mOpt.orNull // Compile error!
```
　  在和Java代码打交道时，orNull方法很有用，因为Java中通常用null表示缺少某值。不过这种做法并不适用于值类型，比如Int。因为orNull的实现带有约束Null <:< A，你仍然可以实例化Option[Int]，只有不要调用orNull。

　  第二，类型约束的另一个用途是改进类型的推断:
```
def firstLast[A, C <: Iterable[A]](it: C) = (it.head, it.last)
  
firstLast(List(1,2,3)) // Compile error
```
　  这里编译错误，是因为推断出的类型参数[Nothing, List[Int]]不符合[A, C <: Iterable[A]]。为什么是Nothing？类型推断器单凭List(1,2,3)无法判断出A是什么，因为它是在同一个步骤中匹配到A和C的。要解决这个问题，首先匹配C，然后再匹配A:
```
def firstLast[A, C](it: C)(implicit ev : C <:< Iterable[A]) = (it.head, it.last)
  
firstLast(List(1,2,3)) // 正确
```

------------------------------------------------------------

------------------------------------------------------------

## 5.9 类型约束补充（Type Constraints）

### 5.9.1 基本概念
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

### 5.9.2 如何使用

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

### 5.9.3 补充说明

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

### 5.9.4 <:与<:<的差异
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

## 5.10 类型的型变
　  类型的型变包括协变和逆变，对于一个带类型参数的类型，比如 List[T]，如果对A及其子类型B，满足 List[B]也符合 List[A]的子类型，那么就称为covariance(协变)，如果 List[A]是 List[B]的子类型，即与原来的父子关系正相反，则称为contravariance(逆变)

协变：

 _____               _____________ 
|     |             |             |
|  A  |             |  List[ A ]  |
|_____|             |_____________|
   ^                       ^ 
   |                       | 
 _____               _____________ 
|     |             |             |
|  B  |             |  List[ B ]  |
|_____|             |_____________|  
逆变：

 _____               _____________ 
|     |             |             |
|  A  |             |  List[ B ]  |
|_____|             |_____________|
   ^                       ^ 
   |                       | 
 _____               _____________ 
|     |             |             |
|  B  |             |  List[ A ]  |
|_____|             |_____________|  

　  如果一个类型支持协变或逆变，则称这个类型为variance(翻译为可变的或变型)，否则称为invariant(不可变的)

　  在Java里，泛型类型都是invariant，比如 List<String> 并不是 List<Object> 的子类型。Java并不支持声明点变型(declaration-site variance，即在定义一个类型时声明它为可变型，也称definition-site)，而scala支持，可以在定义类型时声明(用加号表示为协变，减号表示逆变)，如:
```
trait List[+T] // 在类型定义时(declaration-site)声明为协变 
```
　  这样会把List[String]作为List[Any]的子类型。
不过Java支持使用点变型(use-site variance)，所谓“使用点“，也就是在声明变量时:
```
List<? extends Object> list = new ArrayList<String>();
```
　  scala为了兼容java泛型通配符的形式，引入存在类型(existential type，后边再讲)时，也支持了使用点变型(use-site variance)
```
scala> val a : List[_ <: Any] = List[String]("A")
a: List[_] = List(A)    
```
　  要注意variance并不会被继承，父类声明为variance，子类如果想要保持，仍需要声明:
```
scala> trait A[+T]

scala> class C[T] extends A[T]  // C是invariant的

scala> class X; class Y extends X;

scala> val t:C[X] = new C[Y]
<console>:11: error: type mismatch; 
 found   : C[Y]
 required: C[X]
Note: Y <: X, but class C is invariant in type T.
You may wish to define T as +T instead. (SLS 4.5)
```
　  必须也对C声明为协变的才行：
```
scala> class C[+T] extends A[T]

scala> val t:C[X] = new C[Y]
t: C[X] = C@6a079142
```
　  假定一个函数def makePair(p : Pair[Person])，如果Student是Person的子类，是否可以传递Pair[Student]作为形参？理论上是不可以的，因为即使Student是Person的子类，但Pair[Student]和Pair[Person]之间没有任何关系。可以定义:
```
class Pair[+T](val first : T, val second : T)
```
　  +号意味着该类型是与T协变的，也就是说它与T按同样的方向型变。由于Student是Person的子类，那么Pair[Student]就是Pair[Person]的子类了。也可以有另一方向的协变，考虑Friend[T],表示希望与类型T的人成为朋友的人:
```
trait Friend[-T] {
  def befriend(someone : T)
}

object Run extends App {
  class Person extends Friend[Person]
  class Student extends Person
  
  def makeFriendWith(s:Student, f:Friend[Student]) {
    f.befriend(s)
  }
  
  val jeff = new Student
  val kean = new Person
  
  makeFriendWith(jeff, kean) // OK
}
```
　  注意类型变化的方向和子类型方向是相反的。Student是Person的子类型，但Friend[Student]是Friend[Person]的超类型。在这种情况下，需要将类型参数声明为逆协变。

　  通常而言，对于某个对象消费的值适用逆变，而对于它产出的值则适用协变。如果一个对象同时消费和产出某值，则类型应该保持不变。在scala中数组是不支持型变的。又如下面这个例子会报错:
```
class Pair[+T](var fisrt: T, var second :T) // Error - covariant type T occurs in contravariant position in type T of value fisrt_=
```
　  说first_=(value:T)协变的类型T出现在了逆变点。考虑另一个例子。
```
class Pair[+T](val fisrt: T, val second :T) {
  def replaceFirst(newFirst : T) = new Pair[T](newFirst, second) // covariant type T occurs in contravariant position in type T of value newFirst
}
```
　  编译器拒绝上述代码，因为类型T出现在了逆变点。但是这个方法不可能会破坏原本的对偶——它返回一个新的对偶。解决方法是给方法加上另一个类型参数:
```
class Pair[+T](val fisrt: T, val second :T) {
  def replaceFirst[R >: T](newFirst : R) = new Pair[R](newFirst, second)
}
```

---

---

## 5.11 类型的型变应用实战
> 引入型变的目的？

　  使用协变和逆变是为了使程序更灵活。
　  这里稍微对比一下Java和Scala的使用。
　  Java中List不支持协变，String是Object的子类，而List<String>跟List<Object>之间没有任何关系。所以你不能把List<String>类型的值赋给List<Object>类型的变量。在这种情况下使用时，特别不灵活。
　  Scala中的List是支持协变的，所以String是AnyRef的子类，那么List[String]也是List[AnyRef]的子类。根据里氏替换原则，父类出现的位置可以用子类替换。所以可以把List[String]类型的值赋给List[AnyRef]类型的变量。使用起来更加方便。

### 5.11.1 类型的协变应用实战

　  使用型变中的协变实现一个容器Maybe，这个容器有两个子类型，要么有一个元素的Just，要么是不含元素的Nil。实例代码如下：
```
sealed abstract class Maybe[+A] {
  def isEmpty: Boolean
  def get: A
}
final case class Just[A](value: A) extends Maybe[A] {
  def isEmpty = false
  def get = value
}
case object Nil extends Maybe[Nothing] {
  def isEmpty = true
  // When you see a method returning Nothing, that means that method won’t return successfully.
  def get: Nothing = throw new NoSuchElementException("Nil.get")
}
```
协变允许子类去继承并且在协变的位置（比如，返回值的位置就是协变的位置）也允许可以返回比父类更窄的类型。
在这里，Nil对象就是Maybe的一个子类，并使用scala的Nothing作为类型参数。因为Nil对象中的get方法抛出一个异常，异常的返回类型就是Nothing，又加上Maybe的类型参数A是协变的，因此允许子类返回一个更窄的类型，而Scala的类型继承图中Nothing也是最底层的，是所有类型的子类型。
```
Covariance allows subclasses to override and use narrower types than their superclass in covariant positions such as the return value. Here the Nil object is a subclass of Maybe with scala.Nothing as a type parameter. The reason you’re using scala.Nothing here is that the get method in the Nil object throws an exception. Because the A type of Maybe is covariant, you can return a narrower type from its subclasses. There’s no narrower type than Nothing in Scala because it’s at the bottom of the hierarchy.
```

### 5.11.1 类型的逆变应用实战（暂略）


>总结：
协变：就是让子类的参数类型能够比父类的参数类型更窄。
逆变：

---

---

## 5.12 对象不能使用泛型

　  举一个给对象添加类型参数的例子。比如一个元素类型为T的列表，这个列表要么是一个空列表，要么是一个头部类型为T，尾部类型为List[T]的节点：
```
abstract class List[+T] {
  def isEmpty: Boolean
  def head : T
  def tail : List[T]
}

class Node[T] (val head : T, val tail : List[T]) extends List[T] {
  def isEmpty = false
}
// 需改进，无状态的类最好用对象代替
class Empty[T] extends List[T] {
  def isEmpty = true
  def head = throw new UnsupportedOperationException
  def tail = throw new UnsupportedOperationException
}
```
　  将Empty定义成类看上去很傻。因为它没有状态。但是你无法简单地将它变成对象。
```
//object Empty[T] extends List[T] // Error
```

　  你不能将参数化的类型添加到对象。本例中的解决方法时继承List[Nothing]:
```
object Empty extends List[Nothing] {
  def isEmpty = true
  def head = throw new UnsupportedOperationException
  def tail = throw new UnsupportedOperationException
}
```
　  Nothing类型是所有类型的子类型。因此，当我们构造如下单元素列表时：
```
val lst = new Node(42, Empty)
```
　  Scala类型推断出T为Int，因为T是协变的，Nothing是Int的子类，从而List[Nothing]也是List[Int]的子类。Node的第二个参数类型具体化为List[Int]，当然可以接收List[Nothing]类型的参数。

----

----

## 5.13 类型通配符（暂略）



----

----

[1]: 参考书籍：【Scala for the impatient chapter17】【Scala in depth】 
[2]: http://hongjiang.info/scala-type-contraints-and-specialized-methods/
[3]: http://blog.csdn.net/wsscy2004/article/details/38440247
[4]: http://hongjiang.info/scala-type-system-context-bounds/
[5]: http://zhangjunhd.github.io/2014/01/01/scala-note17-type-parameters.html
[6]:http://hongjiang.info/scala-covariance-and-contravariance/