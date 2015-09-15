# 12 Scala中的常用Monad类型实战

标签（空格分隔）： 级别L2:资深类库设计者 深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------

## 12.1 类型 Option（串行中的无异常处理的场景时使用）

引入Option的目的到底解决什么问题，为什么用它来处理缺失值要比其他方法好，这是本节要说明的问题。

### 12.1.1 基本概念

Java 开发者一般都遇到过 NullPointerException（其他语言也有类似的东西）， 通常这是由于调用了某个对象的方法，但是该对象却为null ，这并不是开发者所希望发生的，代码也只能用丑陋的`try{}catch`去捕捉这种异常。

值 null 通常被滥用来表示一个缺失的值。 

Scala 试图通过摆脱 null 来解决这个问题，并提供自己的类型来表示一个值是可选的（有值或无值）， 这就是 Option[A] 特质。

Option[A] 是一个类型为 A 的可选值的容器： 如果值存在， Option[A] 就是一个 Some[A] ，如果不存在， Option[A] 就是对象 None 。

在类型层面上指出一个值是否存在，使用你的代码的开发者（也包括你自己）就会被编译器强制去处理这种可能性， 而不能依赖值存在的偶然性。

Option 是强制的！不要使用 null 来表示一个值是缺失的。


----------


### 12.1.2 创建 Option

通常，你可以直接实例化 Some 样本类来创建一个 Option 。

```
val greeting: Option[String] = Some("Hello world")

// 或者
val greeting = Some("Hello world") // 类型自动推断出为Option[String]
```

或者，在知道值缺失的情况下，直接使用 None 对象：

```
val greeting: Option[String] = None
```

然而，在实际工作中，你不可避免的要去操作一些 Java 库， 或者是其他将 null 作为缺失值的JVM 语言的代码。 为此， Option 伴生对象提供了一个工厂方法（apply），可以根据给定的参数创建相应的 Option ：

```
val absentGreeting: Option[String] = Option(null) // absentGreeting will be None
// 等价于显示调用伴生对象的工厂方法
val absentGreeting: Option[String] = Option.apply(null)

val presentGreeting: Option[String] = Option("Hello!") // presentGreeting will be Some("Hello!")
```

**综上所述，明确知道值的就用Some，明确不知道值的就用None，不确定的就用Option**。

----------

### 12.1.3 Option的正确使用

在使用Option时，主要有三种不同的使用场景：

1 对Option的数据只处理Some的情况，而None的情况不需要处理。
（**推荐使用方式是**，把Option当做集合的方式使用，具体可以用for，foreach，map，flatMap等）
例如：
```
// 获得一个User对象
val user1: Option[User] = UserRepository.findById(1)
 
// 如果user不为空，就打印他的first name  
if (user1.isDefined) {
  println(user1.get.firstName) // will print "John"
} // 显然，为空就不做任何处理

// 上面的这种使用方式极不推荐，啰嗦的Java Style
// 推荐的方式是当做一个只包含一个元素的集合来处理
user1.foreach { u => println(u.firstName) } // 如果为空不做任何事，否则打印出user的first name

```

2 对Option的数据，Some和None的情况都要处理，但是**处理他们的逻辑是一样的**。
（这种情况其实就是获得Some里面的值，None的情况指定默认值。**推荐使用方式是**，使用getOrElse方法）
例如：
```
val age: Option[Int] = Option(30) // 这里指定是有值，如果这个age从别的地方获得，可能为None
// 但是获得的值进行处理的逻辑一样
age.getOrElse(30) // 真实年龄值和默认值都是为了给使用年龄的地方提供一个整数供使用

```

3 对Option的数据，Some和None的情况都要处理，但是**处理他们的逻辑不一样**。
（**推荐使用方式是**，模式匹配）
例如：
```
UserRepository.findById(1) match { // 从UserRepository中获得一个User
  case Some(user) => 
    // 如果User存在，那么就对该User进行字段的更新
    val updateUser = user.copy(firstName = "Jack")
    UserRepository.update(updateUser)
    // 再返回更新后的user
    updateUser
  
  case None =>
    // 如果不存在，就Insert一个新的User
    val newUser = User(3, "Tony", "G", 30, Some("male"))
    UserRepository.insert(newUser)
    // 再返回新增的User
    newUser
}  

```

----------

下面具体介绍Option的各种细节：

### 12.1.4 使用 Option

目前为止，所有的这些都很简洁，不过该怎么使用 Option 呢？是时候开始举些无聊的例子了。

想象一下，你正在为某个创业公司工作，要做的第一件事情就是实现一个用户的存储库， 要求能够通过唯一的用户 ID 来查找他们。 有时候请求会带来无效的 ID，这种情况，查找方法就需要返回 Option[User] 类型的数据。 一个假想的实现可能是：
```
  case class User(
    id: Int,
    firstName: String,
    lastName: String,
    age: Int,
    gender: Option[String]
  )

  object UserRepository {
    private val users = Map(1 -> User(1, "John", "Doe", 32, Some("male")),
                            2 -> User(2, "Johanna", "Doe", 30, None))
                            
    def findById(id: Int): Option[User] = users.get(id)
    
    def findAll = users.values
  }
```  
现在，假设从 UserRepository 接收到一个 Option[User] 实例，并需要拿它做点什么，该怎么办呢？

一个办法就是通过 isDefined 方法来检查它是否有值。 如果有，你就可以用 get 方法来获取该值（**不推荐这种使用方式**）：
```
  val user1 = UserRepository.findById(1)
  
  if (user1.isDefined) {
    println(user1.get.firstName) // will print "John"
  } 
```

这和 Guava 库 中的 Optional 使用方法类似。 不过这种使用方式太过笨重，更重要的是，使用 get 之前， 你可能会忘记用 isDefined 做检查，这会导致运行期出现异常。 这样一来，相对于 null ，使用 Option 并没有什么优势。

你应该尽可能远离这种访问方式！


----------


### 12.1.5 提供一个默认值（用getOrElse获取Option类型里面的值）

很多时候，在值不存在时，需要进行回退，或者提供一个默认值。 Scala 为 Option 提供了 getOrElse 方法，以应对这种情况：
```
  val user = User(2, "Johanna", "Doe", 30, None)
  
  println("Gender: " + user.gender.getOrElse("not specified")) // will print "not specified"
```
  
请注意，作为 getOrElse 参数的默认值是一个 传名参数（by name的方式） ，这意味着，只有当这个 Option 确实是 None 时，传名参数才会被求值。 因此，没必要担心创建默认值的代价，它只有在需要时才会发生。


----------


### 12.1.6 模式匹配（对Option类型的Some和None分别有不同的逻辑处理）

Some 是一个样本类（case class），可以出现在模式匹配表达式或者其他允许模式出现的地方。 上面的例子可以用模式匹配来重写：
```
  val user = User(2, "Johanna", "Doe", 30, None)
  
  user.gender match {
    case Some(gender) => println("Gender: " + gender)
    case None => println("Gender: not specified")
  }
```
或者，你想删除重复的 println 语句，并重点突出模式匹配表达式的使用：
```
  val user = User(2, "Johanna", "Doe", 30, None)
  
  val gender = user.gender match {
    case Some(gender) => gender
    case None => "not specified"
  }
  
  println("Gender: " + gender)
```
对于获取Option里面的值，你可能已经发现用模式匹配处理 Option 实例是非常啰嗦的，这也是它**非惯用法**的原因。 所以，即使你很喜欢模式匹配，也尽量用其他方法吧。

不过在 Option 上使用模式确实是有一个相当优雅的方式， 在下面的 for 语句一节中，你就会学到。

但是对于Option类型的Some和None分别有不同的逻辑处理时，**推荐使用模式匹配**。例如：
```
UserRepository.findById(1) match { // 从UserRepository中获得一个User
  case Some(user) => 
    // 如果User存在，那么就对该User进行字段的更新
    val updateUser = user.copy(firstName = "Jack")
    UserRepository.update(updateUser)
    // 再返回更新后的user
    updateUser
  
  case None =>
    // 如果不存在，就Insert一个新的User
    val newUser = User(3, "Tony", "G", 30, Some("male"))
    UserRepository.insert(newUser)
    // 再返回新增的User
    newUser
}  

```


----------


### 12.1.7 作为集合的 Option

到目前为止，你还没有看见过优雅使用 Option 的方式吧。下面这个就是了。

前文我提到过， Option 是类型 A 的容器，更确切地说，你可以把它看作是某种集合， 这个特殊的集合要么只包含一个元素，要么就什么元素都没有。

虽然在类型层次上， Option 并不是 Scala 的集合类型， 但，凡是你觉得 Scala 集合好用的方法， Option 也有， 你甚至可以将其转换成一个集合，比如说 List 。

那么这又能让你做什么呢？

#### 12.1.7.1 执行一个副作用

如果想在 Option 值存在的时候执行某个副作用，foreach 方法就派上用场了：
```
 UserRepository.findById(2).foreach(user => println(user.firstName)) // prints "Johanna"
```
如果这个 Option 是一个 Some ，传递给 foreach 的函数就会被调用一次，且只有一次； 如果是 None ，那它就不会被调用。


----------


#### 12.1.7.2 执行映射

Option 表现的像集合，最棒的一点是， 你可以用它来进行函数式编程，就像处理列表、集合那样。

正如你可以将 List[A] 映射到 List[B] 一样，你也可以映射 Option[A] 到 Option[B]： 如果 Option[A] 实例是 Some[A] 类型，那映射结果就是 Some[B] 类型；否则，就是 None 。

如果将 Option 和 List 做对比 ，那 None 就相当于一个空列表： 当你映射一个空的 List[A] ，会得到一个空的 List[B] ， 而映射一个是 None 的 Option[A] 时，得到的 Option[B] 也是 None 。

让我们得到一个可能不存在的用户的年龄：
```
val age = UserRepository.findById(1).map(_.age) // age is Some(32)
```


----------


#### 12.1.7.3 Option 与 flatMap

也可以在 gender 上做 map 操作：
```
val gender = UserRepository.findById(1).map(_.gender) // gender is an Option[Option[String]]
```
所生成的 gender 类型是 Option[Option[String]] 。这是为什么呢？

这样想：你有一个装有 User 的 Option 容器，在容器里面，你将 User 映射到 Option[String] （ User 类上的属性 gender 是 Option[String] 类型的）。 得到的必然是嵌套的 Option。

既然可以 flatMap 一个 List[List[A]] 到 List[B] ， 也可以 flatMap 一个 Option[Option[A]] 到 Option[B] ，这没有任何问题： Option 提供了 flatMap 方法。
```
val gender1 = UserRepository.findById(1).flatMap(_.gender) // gender is Some("male")

val gender2 = UserRepository.findById(2).flatMap(_.gender) // gender is None

val gender3 = UserRepository.findById(3).flatMap(_.gender) // gender is None
```
现在结果就变成了 Option[String] 类型， 如果 user 和 gender 都有值，那结果就会是 Some 类型，反之，就得到一个 None 。

要理解这是什么原理，让我们看看当 flatMap 一个 List[List[A] 时，会发生什么？ （要记得， Option 就像一个集合，比如列表）
```
val names: List[List[String]] =
 List(List("John", "Johanna", "Daniel"), List(), List("Doe", "Westheide"))
 
names.map(_.map(_.toUpperCase))
// results in List(List("JOHN", "JOHANNA", "DANIEL"), List(), List("DOE", "WESTHEIDE"))

names.flatMap(_.map(_.toUpperCase))
// results in List("JOHN", "JOHANNA", "DANIEL", "DOE", "WESTHEIDE")
```
如果我们使用 flatMap ，内部列表中的所有元素会被转换成一个扁平的字符串列表。 显然，如果内部列表是空的，则不会有任何东西留下。

现在回到 Option 类型，如果映射一个由 Option 组成的列表呢？
```
val names: List[Option[String]] = List(Some("Johanna"), None, Some("Daniel"))

names.map(_.map(_.toUpperCase)) // List(Some("JOHANNA"), None, Some("DANIEL"))

names.flatMap(xs => xs.map(_.toUpperCase)) // List("JOHANNA", "DANIEL")
```
如果只是 map ，那结果类型还是 List[Option[String]] 。 而使用 flatMap 时，内部集合的元素就会被放到一个扁平的列表里： 任何一个 Some[String] 里的元素都会被解包，放入结果集中； 而原列表中的 None 值由于不包含任何元素，就直接被过滤出去了。

记住这一点，然后再去看看 faltMap 在 Option 身上做了什么。


----------


#### 12.1.7.4 过滤 Option

也可以像过滤列表那样过滤 Option： 如果选项包含有值，而且传递给 filter 的谓词函数返回真， filter 会返回 Some 实例。 否则（即选项没有值，或者谓词函数返回假值），返回值为 None 。
```
UserRepository.findById(1).filter(_.age > 30) // None, because age is <= 30

UserRepository.findById(2).filter(_.age > 30) // Some(user), because age is > 30

UserRepository.findById(3).filter(_.age > 30) // None, because user is already None
```


----------


### 12.1.8 for 语句

现在，你已经知道 Option 可以被当作集合来看待，并且有 map 、 flatMap 、 filter 这样的方法。 可能你也在想 Option 是否能够用在 for 语句中，答案是肯定的。 而且，用 for 语句来处理 Option 是可读性最好的方式，尤其是当你有多个 map 、flatMap 、filter 调用的时候。 如果只是一个简单的 map 调用，那 for 语句可能有点繁琐。

假如我们想得到一个用户的性别，可以这样使用 for 语句：
```
for {
  user <- UserRepository.findById(1)
  gender <- user.gender
} yield gender // results in Some("male")
```
可能你已经知道，这样的 for 语句等同于嵌套的 flatMap 调用。 如果 UserRepository.findById 返回 None，或者 gender 是 None ， 那这个 for 语句的结果就是 None 。 不过这个例子里， gender 含有值，所以返回结果是 Some 类型的。

如果我们想返回所有用户的性别（当然，如果用户设置了性别），可以遍历用户，yield 其性别：
```
for {
  user <- UserRepository.findAll
  gender <- user.gender
} yield gender
// result in List("male")
```


----------


#### 12.1.8.1 在生成器左侧使用

for 语句中生成器的左侧也是一个模式。 这意味着也可以在 for 语句中使用包含选项的模式。

重写之前的例子：
```
 for {
   User(_, _, _, _, Some(gender)) <- UserRepository.findAll
 } yield gender
```
在生成器左侧使用 Some 模式就可以在结果集中排除掉值为 None 的元素。


----------


#### 12.1.8.2 链接 Option

Option 还可以被链接使用，这有点像偏函数的链接： 在 Option 实例上调用 orElse 方法，并将另一个 Option 实例作为传名参数传递给它。 如果一个 Option 是 None ， orElse 方法会返回传名参数的值，否则，就直接返回这个 Option。

一个很好的使用案例是资源查找：对多个不同的地方按优先级进行搜索。 下面的例子中，我们首先搜索 config 文件夹，并调用 orElse 方法，以传递备用目录：
```
case class Resource(content: String)

val resourceFromConfigDir: Option[Resource] = None

val resourceFromClasspath: Option[Resource] = Some(Resource("I was found on the classpath"))

val resource = resourceFromConfigDir orElse resourceFromClasspath
```
如果想链接多个选项，而不仅仅是两个，使用 orElse 会非常合适。 不过，如果只是想在值缺失的情况下提供一个默认值，那还是使用 getOrElse 吧。

>总结
在这一节里，你学到了有关 Option 的所有知识， 这有利于你理解别人的代码，也有利于你写出更可读，更函数式的代码。
这一节最重要的一点是：列表、集合、映射、Option，以及之后你会见到的其他数据类型， 它们都有一个非常统一的使用方式，这种使用方式既强大又优雅。
下一节，你将学习 Scala 错误处理的惯用法。


----------


----------


## 12.2 Try 与错误处理（串行中有异常处理时使用）

当你在尝试一门新的语言时，可能不会过于关注程序出错的问题， 但当真的去创造可用的代码时，就不能再忽视代码中的可能产生的错误和异常了。 鉴于各种各样的原因，人们往往低估了语言对错误处理支持程度的重要性。

事实会表明，Scala 能够很优雅的处理此类问题， 这一部分，我会介绍 Scala 基于 Try 的错误处理机制，以及这背后的原因。 我将使用一个在 Scala 2.10 新引入的特性，该特性向 2.9.3 兼容， 因此，请确保你的 Scala 版本不低于 2.9.3。


----------


### 12.2.1 异常的抛出和捕获

在介绍 Scala 错误处理的惯用法之前，我们先看看其他语言（如，Java，Ruby）的错误处理机制。 和这些语言类似，Scala 也允许你抛出异常：
```
case class Customer(age: Int)

class Cigarettes

case class UnderAgeException(message: String) extends Exception(message)

def buyCigarettes(customer: Customer): Cigarettes =
  if (customer.age < 16)
    throw UnderAgeException(s"Customer must be older than 16 but was ${customer.age}")
  else new Cigarettes
```

被抛出的异常能够以类似 Java 中的方式被捕获，虽然是使用偏函数来指定要处理的异常类型。 此外，Scala 的 try/catch 是表达式（返回一个值），因此下面的代码会返回异常的消息（**不推荐的方式**）：

```
val youngCustomer = Customer(15)

try {
  buyCigarettes(youngCustomer)
  "Yo, here are your cancer sticks! Happy smokin'!"
} catch {
    case UnderAgeException(msg) => msg
}
```


----------


### 12.2.2 函数式的错误处理

现在，如果代码中到处是上面的异常处理代码，那它很快就会变得丑陋无比，和函数式程序设计非常不搭。 对于高并发应用来说，这也是一个很差劲的解决方式，比如， 假设需要处理在其他线程执行的 actor 所引发的异常，显然你不能用捕获异常这种处理方式， 你可能会想到其他解决方案，例如去接收一个表示错误情况的消息。

一般来说，在 Scala 中，好的做法是通过从函数里返回一个合适的值来通知人们程序出错了。 别担心，我们不会回到 C 中那种需要使用按约定进行检查的错误编码的错误处理。 相反，Scala 使用一个特定的类型来表示可能会导致异常的计算，这个类型就是 Try。


----------


#### 12.2.2.1 Try 的语义

解释 Try 最好的方式是将它与上一章所讲的 Option 作对比。

Option[A] 是一个可能有值也可能没值的容器； 

Try[A] 则表示一种计算： 这种计算在成功的情况下，返回类型为 A 的值，在出错的情况下，返回 Throwable 。 
这种可以容纳错误的容器可以很轻易的在并发执行的程序之间传递。


----------


#### 12.2.2.2 Try 有两个子类型：

- Success[A]：代表成功的计算。
- 封装了 Throwable 的 Failure[A]：代表出了错的计算。

如果知道一个计算可能导致错误，我们可以简单的使用 Try[A] 作为函数的返回类型。 这使得出错的可能性变得很明确，而且强制客户端以某种方式处理出错的可能。

假设，需要实现一个简单的网页爬取器：用户能够输入想爬取的网页 URL， 程序就需要去分析 URL 输入，并从中创建一个 java.net.URL ：

```
import scala.util.Try
import java.net.URL
def parseURL(url: String): Try[URL] = Try(new URL(url))
```

正如你所看到的，函数返回类型为 Try[URL]： 如果给定的 url 语法正确，这将是 Success[URL]， 否则， URL 构造器会引发 MalformedURLException ，从而返回值变成 Failure[URL] 类型。

上例中，我们还用了 Try 伴生对象里的 apply 工厂方法，这个方法接受一个类型为 A 的 传名参数， 这意味着， new URL(url) 是在 Try 的 apply 方法里执行的。

apply 方法不会捕获任何非致命的异常，仅仅返回一个包含相关异常的 Failure 实例。

因此， parseURL("http://danielwestheide.com") 会返回一个 Success[URL] ，包含了解析后的网址， 而 parseULR("garbage") 将返回一个含有 MalformedURLException 的 Failure[URL]。


----------


#### 12.2.2.3 使用 Try

使用 Try 与使用 Option 非常相似，在这里你看不到太多新的东西。

你可以调用 isSuccess 方法来检查一个 Try 是否成功，然后通过 get 方法获取它的值， 但是，这种方式的使用并不多见，因为你可以用 getOrElse 方法给 Try 提供一个默认值：
```
val url = parseURL(Console.readLine("URL: ")) getOrElse new URL("http://duckduckgo.com")
```
如果用户提供的 URL 格式不正确，我们就使用 DuckDuckGo 的 URL 作为备用。


----------


`链式操作`

Try 最重要的特征是，它也支持高阶函数，就像 Option 一样。 在下面的示例中，你将看到，在 Try 上也进行链式操作，捕获可能发生的异常，而且代码可读性不错。

`Mapping 和 Flat Mapping`

将一个是 Success[A] 的 Try[A] 映射到 Try[B] 会得到 Success[B] 。 如果它是 Failure[A] ，就会得到 Failure[B] ，而且包含的异常和 Failure[A] 一样。
```
parseURL("http://danielwestheide.com").map(_.getProtocol)
// results in Success("http")

parseURL("garbage").map(_.getProtocol)
// results in Failure(java.net.MalformedURLException: no protocol: garbage)

```
如果链接多个 map 操作，会产生嵌套的 Try 结构，这并不是我们想要的。 考虑下面这个返回输入流的方法：
```
import java.io.InputStream

def inputStreamForURL(url: String): Try[Try[Try[InputStream]]] = 
  parseURL(url).map { u =>
    Try(u.openConnection()).map(conn => Try(conn.getInputStream))
  }
```
由于每个传递给 map 的匿名函数都返回 Try，因此返回类型就变成了 Try[Try[Try[InputStream]]] 。

这时候， flatMap 就派上用场了。 Try[A] 上的 flatMap 方法接受一个映射函数，这个函数类型是 (A) => Try[B]。 如果我们的 Try[A] 已经是 Failure[A] 了，那么里面的异常就直接被封装成 Failure[B] 返回， 否则， flatMap 将 Success[A] 里面的值解包出来，并通过映射函数将其映射到 Try[B] 。

这意味着，我们可以通过链接任意个 flatMap 调用来创建一条操作管道，将值封装在 Success 里一层层的传递。

现在让我们用 flatMap 来重写先前的例子：
```
def inputStreamForURL(url: String): Try[InputStream] =
 parseURL(url).flatMap { u =>
   Try(u.openConnection()).flatMap(conn => Try(conn.getInputStream))
 }
 
```
这样，我们就得到了一个 Try[InputStream]， 它可以是一个 Failure，包含了在 flatMap 过程中可能出现的异常； 也可以是一个 Success，包含了最后的结果。


----------


`过滤器和 foreach`

当然，你也可以对 Try 进行过滤，或者调用 foreach ，既然已经学过 Option，对于这两个方法也不会陌生。

当一个 Try 已经是 Failure 了，或者传递给它的谓词函数返回假值，filter 就返回 Failure （如果是谓词函数返回假值，那 Failure 里包含的异常是 NoSuchException ）， 否则的话， filter 就返回原本的那个 Success ，什么都不会变：
```
def parseHttpURL(url: String) = parseURL(url).filter(_.getProtocol == "http")

parseHttpURL("http://apache.openmirror.de")           // results in a Success[URL]

parseHttpURL("ftp://mirror.netcologne.de/apache.org") // results in a Failure[URL]

```
当一个 Try 是 Success 时， foreach 允许你在被包含的元素上执行副作用， 这种情况下，传递给 foreach 的函数只会执行一次，毕竟 Try 里面只有一个元素：
```
 parseHttpURL("http://danielwestheide.com").foreach(println)
```
当 Try 是 Failure 时， foreach 不会执行，返回 Unit 类型。


----------


`for 语句中的 Try`

既然 Try 支持 flatMap 、 map 、 filter ，能够使用 for 语句也是理所当然的事情， 而且这种情况下的代码更可读。 为了证明这一点，我们来实现一个返回给定 URL 的网页内容的函数：
```
import scala.io.Source

def getURLContent(url: String): Try[Iterator[String]] =
  for {
   url        <- parseURL(url)
   connection <- Try(url.openConnection())
   is         <- Try(connection.getInputStream)
   source     =  Source.fromInputStream(is)
  } yield source.getLines()
```  
这个方法中，有三个可能会出错的地方，但都被 Try 给涵盖了。 第一个是我们已经实现的 parseURL 方法， 只有当它是一个 Success[URL] 时，我们才会尝试打开连接，从中创建一个新的 InputStream 。 如果这两步都成功了，我们就 yield 出网页内容，得到的结果是 Try[Iterator[String]] 。

当然，你可以使用 Source#fromURL 简化这个代码，并且，这个代码最后没有关闭输入流， 这都是为了保持例子的简单性，专注于要讲述的主题。

>在这个例子中，Source#fromURL可以这样用：
```
import scala.io.Source
def getURLContent(url: String): Try[Iterator[String]] =
  for {
    url <- parseURL(url)
    source = Source.fromURL(url)
  } yield source.getLines()
```  
用 is.close() 可以关闭输入流。


----------


`模式匹配`（**处理Success和Failure逻辑不同的场景，类比Option的场景区分**）

代码往往需要知道一个 Try 实例是 Success 还是 Failure，这时候，你应该想到模式匹配， 也幸好， Success 和 Failure 都是样例类。

接着上面的例子，如果网页内容能顺利提取到，我们就展示它，否则，打印一个错误信息：
```
import scala.util.Success
import scala.util.Failure
getURLContent("http://danielwestheide.com/foobar") match {
  case Success(lines) => lines.foreach(println)
  case Failure(ex) => println(s"Problem rendering URL content: ${ex.getMessage}")
}
```


----------


`从故障中恢复`

如果想在失败的情况下执行某种动作，没必要去使用 getOrElse， 一个更好的选择是 recover ，它接受一个偏函数，并返回另一个 Try。 如果 recover 是在 Success 实例上调用的，那么就直接返回这个实例，否则就调用偏函数。 如果偏函数为给定的 Failure 定义了处理动作， recover 会返回 Success ，里面包含偏函数运行得出的结果。

下面是应用了 recover 的代码：
```
import java.net.MalformedURLException
import java.io.FileNotFoundException
val content = getURLContent("garbage") recover {
  case e: FileNotFoundException => Iterator("Requested page does not exist")
  case e: MalformedURLException => Iterator("Please make sure to enter a valid URL")
  case _ => Iterator("An unexpected error has occurred. We are so sorry!")
}
```
现在，我们可以在返回值 content 上安全的使用 get 方法了，因为它一定是一个 Success。 调用 content.get.foreach(println) 会打印 Please make sure to enter a valid URL。


----------


总结

Scala 的错误处理和其他范式的编程语言有很大的不同。 Try 类型可以让你将可能会出错的计算封装在一个容器里，并优雅的去处理计算得到的值。 并且可以像操作集合和 Option 那样统一的去操作 Try。

Try 还有其他很多重要的方法，鉴于篇幅限制，这一章并没有全部列出，比如 orElse 方法， transform 和 recoverWith 也都值得去看。

下一节，我们会探讨 Either，另外一种可以代表计算的类型，但它的可使用范围要比 Try 大的多。

----------


## 12.3 类型 Either

上一章介绍了 Try，它用函数式风格来处理程序错误。 这一章我们介绍一个和 Try 相似的类型 - Either， 学习如何去使用它，什么时候去使用它，以及它有什么缺点。

不过首先得知道一件事情： 在写作这篇文章的时候，Either 有一些设计缺陷，很多人都在争论到底要不要使用它。 既然如此，为什么还要学习它呢？ 因为，在理解 Try 这个错综复杂的类型之前，不是所有人都会在代码中使用 Try 风格的异常处理。 其次，Try 不能完全替代 Either，它只是 Either 用来处理异常的一个特殊用法。 Try 和 Either 互相补充，各自侧重于不同的使用场景。

因此，尽管 Either 有缺陷，在某些情况下，它依旧是非常合适的选择。


----------


### 12.3.1 Either 语义

Either 也是一个容器类型，但不同于 Try、Option，它需要两个类型参数： Either[A, B] 要么包含一个类型为 A 的实例，要么包含一个类型为 B 的实例。 这和 Tuple2[A, B] 不一样， Tuple2[A, B] 是两者都要包含。

Either 只有两个子类型： Left、 Right， 如果 Either[A, B] 对象包含的是 A 的实例，那它就是 Left 实例，否则就是 Right 实例。

在语义上，Either 并没有指定哪个子类型代表错误，哪个代表成功， 毕竟，它是一种通用的类型，适用于可能会出现两种结果的场景。 而异常处理只不过是其一种常见的使用场景而已， 不过，按照约定，处理异常时，Left 代表出错的情况，Right 代表成功的情况。


----------


### 12.3.2 创建 Either

创建 Either 实例非常容易，Left 和 Right 都是样例类。 要是想实现一个 “坚如磐石” 的互联网审查程序，可以直接这么做：
```
import scala.io.Source
import java.net.URL

def getContent(url: URL): Either[String, Source] =
 if(url.getHost.contains("google"))
   Left("Requested URL is blocked for the good of the people!")
 else
   Right(Source.fromURL(url))
```
调用 getContent(new URL("http://danielwestheide.com")) 会得到一个封装有 scala.io.Source 实例的 Right， 传入 new URL("https://plus.google.com") 会得到一个含有 String 的 Left。


----------


### 12.3.3 Either 用法

Either 基本的使用方法和 Option、Try 一样： 调用 isLeft （或 isRight ）方法询问一个 Either，判断它是 Left 值，还是 Right 值。 可以使用模式匹配，这是最方便也是最为熟悉的一种方法：
```
getContent(new URL("http://google.com")) match {
 case Left(msg) => println(msg)
 case Right(source) => source.getLines.foreach(println)
}
```

**立场**

你不能，至少不能直接像 Option、Try 那样把 Either 当作一个集合来使用， 因为 Either 是 无偏(unbiased) 的。

Try 偏向 Success： map 、 flatMap 以及其他一些方法都假设 Try 对象是一个 Success 实例， 如果是 Failure，那这些方法不做任何事情，直接将这个 Failure 返回。

但 Either 不做任何假设，这意味着首先你要选择一个立场，假设它是 Left 还是 Right， 然后在这个假设的前提下拿它去做你想做的事情。 调用 left 或 right 方法，就能得到 Either 的 LeftProjection 或 RightProjection实例， 这就是 Either 的 立场(Projection) ，它们是对 Either 的一个左偏向的或右偏向的封装。

**映射**

一旦有了 Projection，就可以调用 map ：
```
val content: Either[String, Iterator[String]] =
  getContent(new URL("http://danielwestheide.com")).right.map(_.getLines())
// content is a Right containing the lines from the Source returned by getContent

val moreContent: Either[String, Iterator[String]] =
  getContent(new URL("http://google.com")).right.map(_.getLines)
// moreContent is a Left, as already returned by getContent

// content: Either[String,Iterator[String]] = Right(non-empty iterator)
// moreContent: Either[String,Iterator[String]] = Left(Requested URL is blocked for the good of the people!)
```
这个例子中，无论 Either[String, Source] 是 Left 还是 Right， 它都会被映射到 Either[String, Iterator[String]] 。 如果，它是一个 Right 值，这个值就会被 _.getLines() 转换； 如果，它是一个 Left 值，就直接返回这个值，什么都不会改变。

LeftProjection也是类似的：
```
val content: Either[Iterator[String], Source] =
  getContent(new URL("http://danielwestheide.com")).left.map(Iterator(_))
// content is the Right containing a Source, as already returned by getContent

val moreContent: Either[Iterator[String], Source] =
  getContent(new URL("http://google.com")).left.map(Iterator(_))
// moreContent is a Left containing the msg returned by getContent in an Iterator

// content: Either[Iterator[String],scala.io.Source] = Right(non-empty iterator)
// moreContent: Either[Iterator[String],scala.io.Source] = Left(non-empty iterator)
```
现在，如果 Either 是个 Left 值，里面的值会被转换；如果是 Right 值，就维持原样。 两种情况下，返回类型都是 Either[Iterator[String, Source] 。

请注意， map 方法是定义在 Projection 上的，而不是 Either， 但其返回类型是 Either，而不是 Projection。

可以看到，Either 和其他你知道的容器类型之所以不一样，就是因为它的无偏性。 接下来你会发现，在特定情况下，这会产生更多的麻烦。 而且，如果你想在一个 Either 上多次调用 map 、 flatMap 这样的方法， 你总需要做 Projection，去选择一个立场。
Flat Mapping

Projection 也支持 flat mapping，避免了嵌套使用 map 所造成的令人费解的类型结构。

假设我们想计算两篇文章的平均行数，下面的代码可以解决这个 “富有挑战性” 的问题：
```
val part5 = new URL("http://t.co/UR1aalX4")
val part6 = new URL("http://t.co/6wlKwTmu")
val content = getContent(part5).right.map(a =>
  getContent(part6).right.map(b =>
    (a.getLines().size + b.getLines().size) / 2))
    
// => content: Product with Serializable with scala.util.Either[String,Product with Serializable with scala.util.Either[String,Int]] = Right(Right(537))
```
运行上面的代码，会得到什么？ 会得到一个类型为 Either[String, Either[String, Int]] 的玩意儿。 当然，你可以调用 joinRight 方法来使得这个结果 扁平化(flatten) 。

不过我们可以直接避免这种嵌套结构的产生， 如果在最外层的 RightProjection 上调用 flatMap 函数，而不是 map ， 得到的结果会更好看些，因为里层 Either 的值被解包了：
```
val content ＝ getContent(part5).right.flatMap(a =>
  getContent(part6).right.map(b =>
    (a.getLines().size + b.getLines().size) / 2))
// => content: scala.util.Either[String,Int] = Right(537)
```
现在， content 值类型变成了 Either[String, Int] ，处理它相对来说就很容易了。

**for 语句**

说到 for 语句，想必现在，你应该已经爱上它在不同类型上的一致性表现了。 在 for 语句中，也能够使用 Either 的 Projection，但遗憾的是，这样做需要一些丑陋的变通。

假设用 for 语句重写上面的例子：
```
def averageLineCount(url1: URL, url2: URL): Either[String, Int] =
  for {
    source1 <- getContent(url1).right
    source2 <- getContent(url2).right
  } yield (source1.getLines().size + source2.getLines().size) / 2
```  
这个代码还不是太坏，毕竟只需要额外调用 left 、 right 。

但是你不觉得 yield 语句太长了吗？现在，我就把它移到值定义块中：
```
def averageLineCountWontCompile(url1: URL, url2: URL): Either[String, Int] =
  for {
    source1 <- getContent(url1).right
    source2 <- getContent(url2).right
    lines1 = source1.getLines().size
    lines2 = source2.getLines().size
  } yield (lines1 + lines2) / 2
```
 
试着去编译它，然后你会发现无法编译！如果我们把 for 语法糖去掉，原因可能会清晰些。 展开上面的代码得到：
```
def averageLineCountDesugaredWontCompile(url1: URL, url2: URL): Either[String, Int] =
  getContent(url1).right.flatMap { source1 =>
    getContent(url2).right.map { source2 =>
      val lines1 = source1.getLines().size
      val lines2 = source2.getLines().size
      (lines1, lines2)
    }.map { case (x, y) => x + y / 2 }
  }
```  
问题在于，在 for 语句中追加新的值定义会在前一个 map 调用上自动引入另一个 map 调用， 前一个 map 调用返回的是 Either 类型，不是 RightProjection 类型， 而 Scala 并没有在 Either 上定义 map 函数，因此编译时会出错。

这就是 Either 丑陋的一面。要解决这个例子中的问题，可以不添加新的值定义。 但有些情况，就必须得添加，这时候可以将值封装成 Either 来解决这个问题：
```
def averageLineCount(url1: URL, url2: URL): Either[String, Int] =
  for {
    source1 <- getContent(url1).right
    source2 <- getContent(url2).right
    lines1 <- Right(source1.getLines().size).right
    lines2 <- Right(source2.getLines().size).right
  } yield (lines1 + lines2) / 2
```  
认识到这些设计缺陷是非常重要的，这不会影响 Either 的可用性，但如果不知道发生了什么，它会让你感到非常头痛。

**其他方法**

Projection 还有其他有用的方法：

可以在 Either 的某个 Projection 上调用 toOption 方法，将其转换成 Option。

假如，你有一个类型为 Either[A, B] 的实例 e ， e.right.toOption 会返回一个 Option[B] 。 如果 e 是一个 Right 值，那这个 Option[B] 会是 Some 类型， 如果 e 是一个 Left 值，那 Option[B] 就会是 None 。 调用 e.left.toOption 也会有相应的结果。

还可以用 toSeq 方法将 Either 转换为序列。

**Fold 函数**

如果想变换一个 Either（不论它是 Left 值还是 right 值），可以使用定义在 Either 上的 fold 方法。 这个方法接受两个返回相同类型的变换函数， 当这个 Either 是 Left 值时，第一个函数会被调用；否则，第二个函数会被调用。

为了说明这一点，我们用 fold 重写之前的一个例子：
```
val content: Iterator[String] =
  getContent(new URL("http://danielwestheide.com")).fold(Iterator(_), _.getLines())
  
val moreContent: Iterator[String] =
  getContent(new URL("http://google.com")).fold(Iterator(_), _.getLines())
```
这个示例中，我们把 Either[String, String] 变换成了 Iterator[String] 。 当然，你也可以在变换函数里返回一个新的 Either，或者是只执行副作用。 fold 是一个可以用来替代模式匹配的好方法。

**何时使用 Either**

知道了 Either 的用法和应该注意的事项，我们来看看一些特殊的用例。

错误处理

可以用 Either 来处理异常，就像 Try 一样。 不过 Either 有一个优势：可以使用更为具体的错误类型，而 Try 只能用 Throwable 。 （这表明 Either 在处理自定义的错误时是个不错的选择） 不过，需要实现一个方法，将这个功能委托给 scala.util.control 包中的 Exception 对象：
```
import scala.util.control.Exception.catching

def handling[Ex <: Throwable, T](exType: Class[Ex])(block: => T): Either[Ex, T] =
  catching(exType).either(block).asInstanceOf[Either[Ex, T]]
```
这么做的原因是，虽然 scala.util.Exception 提供的方法允许你捕获某些类型的异常， 但编译期产生的类型总是 Throwable ，因此需要使用 asInstanceOf 方法强制转换。

有了这个方法，就可以把期望要处理的异常类型，放在 Either 里了：
```
import java.net.MalformedURLException

def parseURL(url: String): Either[MalformedURLException, URL] =
  handling(classOf[MalformedURLException])(new URL(url))
```
handling 的第二个参数 block 中可能还会有其他产生错误的情形， 而且并不是所有情形都会抛出异常。 这种情况下，没必要为了捕获异常而人为抛出异常，相反，只需定义你自己的错误类型，最好是样例类， 并在错误情况发生时返回一个封装了这个类型实例的 Left。

下面是一个例子：
```
case class Customer(age: Int)
class Cigarettes
case class UnderAgeFailure(age: Int, required: Int)
def buyCigarettes(customer: Customer): Either[UnderAgeFailure, Cigarettes] =
  if (customer.age < 16) Left(UnderAgeFailure(customer.age, 16))
  else Right(new Cigarettes)
```
应该避免使用 Either 来封装意料之外的异常， 使用 Try 来做这种事情会更好，至少它没有 Either 这样那样的缺陷。

处理集合

有些时候，当按顺序依次处理一个集合时，里面的某个元素产生了意料之外的结果， 但是这时程序不应该直接引发异常，因为这样会使得剩下的元素无法处理。 Either 也非常适用于这种情况。

假设，在我们 “行业标准般的” Web 审查系统里，使用了某种黑名单：
```
type Citizen = String
case class BlackListedResource(url: URL, visitors: Set[Citizen])

val blacklist = List(
  BlackListedResource(new URL("https://google.com"), Set("John Doe", "Johanna Doe")),
  BlackListedResource(new URL("http://yahoo.com"), Set.empty),
  BlackListedResource(new URL("https://maps.google.com"), Set("John Doe")),
  BlackListedResource(new URL("http://plus.google.com"), Set.empty)
)
```
BlackListedResource 表示黑名单里的网站 URL，外加试图访问这个网址的公民集合。

现在我们想处理这个黑名单，为了标识 “有问题” 的公民，比如说那些试图访问被屏蔽网站的人。 同时，我们想确定可疑的 Web 网站：如果没有一个公民试图去访问黑名单里的某一个网站， 那么就必须假定目标对象因为一些我们不知道的原因绕过了筛选器，需要对此进行调查。

下面的代码展示了该如何处理黑名单的：
```
al checkedBlacklist: List[Either[URL, Set[Citizen]]] =
  blacklist.map(resource =>
    if (resource.visitors.isEmpty) Left(resource.url)
    else Right(resource.visitors))
```
我们创建了一个 Either 序列，其中 Left 实例代表可疑的 URL， Right 是问题市民的集合。 识别问题公民和可疑网站变得非常简单。
```
val suspiciousResources = checkedBlacklist.flatMap(_.left.toOption)
val problemCitizens = checkedBlacklist.flatMap(_.right.toOption).flatten.toSet
```

Either 非常适用于这种比异常处理更为普通的使用场景。

**总结**

目前为止，你应该已经学会了怎么使用 Either，认识到它的缺陷，以及知道该在什么时候用它。 鉴于 Either 的缺陷，使用不使用它，全都取决于你。 其实在实践中，你会注意到，有了 Try 之后，Either 不会出现那么多糟糕的使用情形。

不管怎样，分清楚它带来的利与弊总没有坏处。

----------


----------


## 12.4 类型 Future（并行中有无异常时都使用Future）

作为一个对 Scala 充满热情的开发者，你应该已经听说过 Scala 处理并发的能力，或许你就是被这个吸引来的。 相较于大多数编程语言低级的并发 API，Scala 提供的方法可以让人们更好的理解并发以及编写良构的并发程序。

本章的主题 Future 就是这种方法的两大基石之一。（另一个是 actor） 我会解释 Future 的优点，以及它的函数式特征。

如果你想动手试试接下来的例子，请确保 Scala 版本不低于 2.9.3， Future 在 2.10.0 版本中引入，并向后兼容到 2.9.3，最初，它是 Akka 库的一部分（API略有不同）。

### 12.4.1 顺序执行的劣势

假设你想准备一杯卡布奇诺，你可以一个接一个的执行以下步骤：

- 研磨所需的咖啡豆
- 加热一些水
- 用研磨好的咖啡豆和热水制做一杯咖啡
- 打奶泡
- 结合咖啡和奶泡做成卡布奇诺

转换成 Scala 代码，可能会是这样：
```
import scala.util.Try

// Some type aliases, just for getting more meaningful method signatures:
type CoffeeBeans = String
type GroundCoffee = String
case class Water(temperature: Int)
type Milk = String
type FrothedMilk = String
type Espresso = String
type Cappuccino = String

// dummy implementations of the individual steps:
def grind(beans: CoffeeBeans): GroundCoffee = s"ground coffee of $beans"
def heatWater(water: Water): Water = water.copy(temperature = 85)
def frothMilk(milk: Milk): FrothedMilk = s"frothed $milk"
def brew(coffee: GroundCoffee, heatedWater: Water): Espresso = "espresso"
def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = "cappuccino"

// some exceptions for things that might go wrong in the individual steps
// (we'll need some of them later, use the others when experimenting with the code):
case class GrindingException(msg: String) extends Exception(msg)
case class FrothingException(msg: String) extends Exception(msg)
case class WaterBoilingException(msg: String) extends Exception(msg)
case class BrewingException(msg: String) extends Exception(msg)

// going through these steps sequentially:
def prepareCappuccino(): Try[Cappuccino] = for {
  ground   <- Try(grind("arabica beans"))
  water    <- Try(heatWater(Water(25)))
  espresso <- Try(brew(ground, water))
  foam     <- Try(frothMilk("milk"))
} yield combine(espresso, foam)
```

这样做有几个优点： 
可以很轻易的弄清楚事情的步骤，一目了然，而且不会混淆。（毕竟没有上下文切换） 不好的一面是，大部分时间，你的大脑和身体都处于等待的状态： 在等待研磨咖啡豆时，你完全不能做任何事情，只有当这一步完成后，你才能开始烧水。 这显然是在浪费时间，所以你可能想一次开始多个步骤，让它们同时执行， 一旦水烧开，咖啡豆也磨好了，你可以制做咖啡了，这期间，打奶泡也可以开始了。

这和编写软件没什么不同。 一个 Web 服务器可以用来处理和响应请求的线程只有那么多， 不能因为要等待数据库查询或其他 HTTP 服务调用的结果而阻塞了这些可贵的线程。 相反，一个异步编程模型和非阻塞 IO 会更合适， 这样的话，当一个请求处理在等待数据库查询结果时，处理这个请求的线程也能够为其他请求服务。

>"I heard you like callbacks, so I put a callback in your callback!"

在并发家族里，你应该已经知道 nodejs 这个很酷的家伙，nodejs 完全通过回调来通信， 不幸的是，这很容易导致回调中包含回调的回调，这简直是一团糟，代码难以阅读和调试。

Scala 的 Future 也允许回调，但它提供了更好的选择，所以你不怎么需要它。

>"I know Futures, and they are completely useless!"

也许你知道些其他的 Future 实现，最引人注目的是 Java 提供的那个。 但是对于 Java 的 Future，你只能去查看它是否已经完成，或者阻塞线程直到其结束。 简而言之，Java 的 Future 几乎没有用，而且用起来绝对不会让人开心。

如果你认为 Scala 的 Future 也是这样，那大错特错了！


----------


### 12.4.2 Future 语义

scala.concurrent 包里的 Future[T] 是一个容器类型，代表一种返回值类型为 T 的计算。 计算可能会出错，也可能会超时；从而，当一个 future 完成时，它可能会包含异常，而不是你期望的那个值。

Future 只能写一次： 当一个 future 完成后，它就不能再被改变了。 同时，Future 只提供了读取计算值的接口，写入计算值的任务交给了 Promise，这样，API 层面上会有一个清晰的界限。 这篇文章里，我们主要关注前者，下一章会介绍 Promise 的使用。

### 12.4.3 使用 Future

Future 有多种使用方式，我将通过重写 “卡布奇诺” 这个例子来说明。

首先，**所有可以并行执行的函数，应该返回一个 Future**（需要并行执行的函数应该返回Future类型）：
```
import scala.concurrent.future
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

def grind(beans: CoffeeBeans): Future[GroundCoffee] = Future {
  println("start grinding...")
  Thread.sleep(Random.nextInt(2000))
  if (beans == "baked beans") throw GrindingException("are you joking?")
  println("finished grinding...")
  s"ground coffee of $beans"
}

def heatWater(water: Water): Future[Water] = Future {
  println("heating the water now")
  Thread.sleep(Random.nextInt(2000))
  println("hot, it's hot!")
  water.copy(temperature = 85)
}

def frothMilk(milk: Milk): Future[FrothedMilk] = Future {
  println("milk frothing system engaged!")
  Thread.sleep(Random.nextInt(2000))
  println("shutting down milk frothing system")
  s"frothed $milk"
}

def brew(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = Future {
  println("happy brewing :)")
  Thread.sleep(Random.nextInt(2000))
  println("it's brewed!")
  "espresso"
}
```
上面的代码有几处需要解释。

首先是 Future 伴生对象里的 apply 方法需要两个参数：
```
object Future {
  def apply[T](body: => T)(implicit execctx: ExecutionContext): Future[T]
}
```
要异步执行的计算通过传名参数 body 传入。 第二个参数是一个隐式参数，隐式参数是说，函数调用时，如果作用域中存在一个匹配的隐式值，就无需显示指定这个参数。 ExecutionContext 可以执行一个 Future，可以把它看作是一个线程池，是绝大部分 Future API 的隐式参数。
```
import scala.concurrent.ExecutionContext.Implicits.global
```
⬆语句引入了一个全局的执行上下文，确保了隐式值的存在。 这时候，只需要一个单元素列表,可以用大括号来代替小括号。 调用 future 方法时，经常使用这种形式，使得它看起来像是一种语言特性，而不是一个普通方法的调用。

这个例子没有大量计算，所以用随机休眠来模拟以说明问题， 而且，为了更清晰的说明并发代码的执行顺序，还在“计算”之前和之后打印了些东西。

计算会在 Future 创建后的某个不确定时间点上由 ExecutionContext 给其分配的某个线程中执行。

### 12.4.3 回调（Call back）（极不推荐的使用方式）

对于一些简单的问题，使用回调就能很好解决。 Future 的回调是偏函数，你可以把回调传递给 Future 的 onSuccess 方法， 如果这个 Future 成功完成，这个回调就会执行，并把 Future 的返回值作为参数输入：
```
 grind("arabica beans").onSuccess { case ground =>
   println("okay, got my ground coffee")
 }
```
类似的，也可以在 onFailure 上注册回调，只不过它是在 Future 失败时调用，其输入是一个 Throwable。

通常的做法是将两个回调结合在一起以更好的处理 Future：在 onComplete 方法上注册回调，回调的输入是一个 Try。

 import scala.util.{Success, Failure}
 grind("baked beans").onComplete {
   case Success(ground) => println(s"got my $ground")
   case Failure(ex) => println("This grinder needs a replacement, seriously!")
 }
传递给 grind 的是 “baked beans”，因此 grind 方法会产生异常，进而导致 Future 中的计算失败。

### 12.4.4 Future 组合

当嵌套使用 Future 时，回调就变得比较烦人。 不过，你也没必要这么做，因为 Future 是可组合的，这是它真正发挥威力的时候！

你一定已经注意到，之前讨论过的所有容器类型都可以进行 map 、 flatMap 操作，也可以用在 for 语句中。 作为一种容器类型，Future 支持这些操作也不足为奇！

真正的问题是，在还没有完成的计算上执行这些操作意味这什么，如何去理解它们？

### 12.4.5 Map 操作

Scala 让 “时间旅行” 成为可能！ 假设想在水加热后就去检查它的温度， 可以通过将 Future[Water] 映射到 Future[Boolean] 来完成这件事情：
```
 val tempreatureOkay: Future[Boolean] = heatWater(Water(25)) map { water =>
   println("we're in the future!")
   (80 to 85) contains (water.temperature)
 }
``` 
tempreatureOkay 最终会包含水温的结果。 你可以去改变 heatWater 的实现来让它抛出异常（比如说，加热器爆炸了）， 然后等待 “we're in the future!” 出现在显示屏上，不过你永远等不到。

写传递给 map 的函数时，你就处在未来（或者说可能的未来）。 一旦 Future[Water] 实例成功完成，这个函数就会执行，只不过，该函数所在的时间线可能不是你现在所处的这个。 如果 Future[Water] 失败，传递给 map 的函数中的事情永远不会发生，调用 map 的结果将是一个失败的 Future[Boolean]。

### 12.4.6 FlatMap 操作

如果一个 Future 的计算依赖于另一个 Future 的结果，那需要求救于 flatMap 以避免 Future 的嵌套。

假设，测量水温的线程需要一些时间，那你可能想异步的去检查水温是否 OK。 比如，有一个函数，接受一个 Water ，并返回 Future[Boolean] ：
```
def temperatureOkay(water: Water): Future[Boolean] = Future {
  (80 to 85) contains (water.temperature)
｝
```
使用 flatMap（而不是 map）得到一个 Future[Boolean]，而不是 Future[Future[Boolean]]：
```
val nestedFuture: Future[Future[Boolean]] = heatWater(Water(25)) map {
  water => temperatureOkay(water)
}

val flatFuture: Future[Boolean] = heatWater(Water(25)) flatMap {
  water => temperatureOkay(water)
}
```
同样，映射只会发生在 Future[Water] 成功完成情况下。

### 12.4.7 for 语句

除了调用 flatMap ，也可以写成 for 语句。上面的例子可以重写成：
```
val acceptable: Future[Boolean] = for {
  heatedWater <- heatWater(Water(25))
  okay        <- temperatureOkay(heatedWater)
} yield okay
```
如果有多个可以并行执行的计算，则需要特别注意，要先在 for 语句外面创建好对应的 Futures。
```
def prepareCappuccinoSequentially(): Future[Cappuccino] =
  // 串行执行
  for {
    ground   <- grind("arabica beans")
    water    <- heatWater(Water(25))
    foam     <- frothMilk("milk")
    espresso <- brew(ground, water)
  } yield combine(espresso, foam)
``` 
这看起来很漂亮，但要知道，for 语句只不过是 flatMap 嵌套调用的语法糖。 这意味着，只有当 Future[GroundCoffee] 成功完成后， heatWater 才会创建 Future[Water]。 你可以查看函数运行时打印出来的东西来验证这个说法。

因此，要确保在 for 语句之前实例化所有相互独立的 Futures：
```
def prepareCappuccino(): Future[Cappuccino] = {
  // for之外的部分并行执行
  val groundCoffee = grind("arabica beans")
  val heatedWater = heatWater(Water(20))
  val frothedMilk = frothMilk("milk")
  for {
    // for 里面的全部串行执行
    ground <- groundCoffee
    water <- heatedWater
    foam <- frothedMilk
    espresso <- brew(ground, water)
  } yield combine(espresso, foam)
}
```
在 for 语句之前，三个 Future 在创建之后就开始各自独立的运行，显示屏的输出是不确定的。 唯一能确定的是 “happy brewing” 总是出现在后面， 因为该输出所在的函数 brew 是在其他两个函数执行完毕后才开始执行的。 也因为此，可以在 for 语句里面直接调用它，当然，前提是前面的 Future 都成功完成。

### 12.4.8 失败偏向的 Future

你可能会发现 Future[T] 是成功偏向的，允许你使用 map、flatMap、filter 等。

但是，有时候可能处理事情出错的情况。 调用 Future[T] 上的 failed 方法，会得到一个失败偏向的 Future，类型是 Future[Throwable]。 之后就可以映射这个 Future[Throwable]，在失败的情况下执行 mapping 函数。

>总结
你已经见过 Future 了，而且它的前途看起来很光明！ 因为它是一个可组合、可函数式使用的容器类型，这让我们的工作变得异常舒服。
调用 future 方法可以轻易将阻塞执行的代码变成并发执行，但是，代码最好原本就是非阻塞的。 为了实现它，我们还需要 Promise 来完成 Future，这就是下一章的主题。


----------


----------

## 12.5 实战中的 Promise 和 Future

上一章介绍了 Future 类型，以及如何用它来编写高可读性、高组合性的异步执行代码。

Future 只是整个谜团的一部分： 它是一个只读类型，允许你使用它计算得到的值，或者处理计算中出现的错误。 但是在这之前，必须得有一种方法把这个值放进去。 这一章里，你将会看到如何通过 Promise 类型来达到这个目的。

### 12.5.1 类型 Promise

之前，我们把一段顺序执行的代码块传递给了 scala.concurrent 里的 future 方法， 并且在作用域中给出了一个 ExecutionContext，它神奇地异步调用代码块，返回一个 Future 类型的结果。

虽然这种获得 Future 的方式很简单，但还有其他的方法来创建 Future 实例，并填充它，这就是 Promise。 **Promise 允许你在 Future 里放入一个值**，不过只能做一次，Future 一旦完成，就不能更改了。

一个 Future 实例总是和一个（也只能是一个）Promise 实例关联在一起。 如果你在 REPL 里调用 future 方法，你会发现返回的也是一个 Promise：
```

scala> import concurrent.Future
import concurrent.Future

scala> import concurrent.future
import concurrent.future

scala> import concurrent.ExecutionContext.Implicits.global
import concurrent.ExecutionContext.Implicits.global

scala> val f: Future[String] = future { "Hello World!" }
f: scala.concurrent.Future[String] = scala.concurrent.impl.Promise$DefaultPromise@6d78f375

```
你得到的对象是一个 DefaultPromise ，它实现了 Future 和 Promise 接口， 不过这就是具体的实现细节了（译注，有兴趣的读者可翻阅其实现的源码）， 使用者只需要知道代码实现把 Future 和对应的 Promise 之间的联系分的很清晰。

这个小例子说明了：除了通过 Promise，没有其他方法可以完成一个 Future， future 方法也只是一个辅助函数，隐藏了具体的实现机制。

现在，让我们动动手，看看怎样直接使用 Promise 类型。


----------


### 12.5.2 给出承诺

当我们谈论起承诺能否被兑现时，一个很熟知的例子是那些政客的竞选诺言。

假设被推选的政客给他的投票者一个减税的承诺。 这可以用 Promise[TaxCut] 表示：
```
scala> import concurrent.Promise
import concurrent.Promise

scala> case class TaxCut(reduction: Int)
defined class TaxCut

// either give the type as a type parameter to the factory method:
scala> val taxcut = Promise[TaxCut]()
taxcut: scala.concurrent.Promise[TaxCut] = scala.concurrent.impl.Promise$DefaultPromise@31368b99

// or give the compiler a hint by specifying the type of your val:
scala> val taxcut2: Promise[TaxCut] = Promise()
taxcut2: scala.concurrent.Promise[TaxCut] = scala.concurrent.impl.Promise$DefaultPromise@4f6ee6e4

```

一旦创建了这个 Promise，就可以在它上面调用 future 方法来获取承诺的未来：

```
scala> val taxCutF: Future[TaxCut] = taxcut.future
taxCutF: scala.concurrent.Future[TaxCut] = scala.concurrent.impl.Promise$DefaultPromise@31368b99
``` 
返回的 Future 可能并不和 Promise 一样，但在同一个 Promise 上调用 future 方法总是返回同一个对象， 以确保 Promise 和 Future 之间一对一的关系。


----------


### 12.5.3 结束承诺

一旦给出了承诺，并告诉全世界会在不远的将来兑现它，那最好尽力去实现。 在 Scala 中，可以结束一个 Promise，无论成功还是失败。

#### 12.5.3.1 兑现承诺

为了成功结束一个 Promise，你可以调用它的 success 方法，并传递一个大家期许的结果：
```
  taxcut.success(TaxCut(20))
```
这样做之后，Promise 就无法再写入其他值了，如果偏要再写，会产生异常。

此时，和 Promise 关联的 Future 也成功完成，注册的回调会开始执行， 或者说对这个 Future 进行了映射，那这个时候，映射函数也该执行了。

一般来说，Promise 的完成和对返回的 Future 的处理发生在不同的线程。 很可能你创建了 Promise，并立即返回和它关联的 Future 给调用者，而实际上，另外一个线程还在计算它。

为了说明这一点，我们拿减税来举个例子：
```
object Government {
  def redeemCampaignPledge(): Future[TaxCut] = {
    val p = Promise[TaxCut]()
    Future {
      println("Starting the new legislative period.")
      Thread.sleep(2000)
      p.success(TaxCut(20))
      println("We reduced the taxes! You must reelect us!!!!1111")
    }
    p.future
  }
}
```
这个例子中使用了 Future 伴生对象，不过不要被它搞混淆了，这个例子的重点是：Promise 并不是在调用者的线程里完成的。

现在我们来兑现当初的竞选宣言，在 Future 上添加一个 onComplete 回调：
```
import scala.util.{Success, Failure}

val taxCutF: Future[TaxCut] = Government.redeemCampaignPledge()

println("Now that they're elected, let's see if they remember their promises...")

taxCutF.onComplete {
  case Success(TaxCut(reduction)) =>
    println(s"A miracle! They really cut our taxes by $reduction percentage points!")
  case Failure(ex) =>
    println(s"They broke their promises! Again! Because of a ${ex.getMessage}")
}
```
多次运行这个例子，会发现显示屏输出的结果顺序是不确定的，而且，最终回调函数会执行，进入成功的那个 case 。

#### 12.5.3.2 违背诺言

政客习惯违背诺言，Scala 程序员有时候也只能这样做。 调用 failure 方法，传递一个异常，结束 Promise：
```
case class LameExcuse(msg: String) extends Exception(msg)
object Government {
  def redeemCampaignPledge(): Future[TaxCut] = {
     val p = Promise[TaxCut]()
     Future {
       println("Starting the new legislative period.")
       Thread.sleep(2000)
       p.failure(LameExcuse("global economy crisis"))
       println("We didn't fulfill our promises, but surely they'll understand.")
     }
     p.future
   }
}
```
这个 redeemCampaignPledge 实现最终会违背承诺。 一旦用 failure 结束这个 Promise，也无法再次写入了，正如 success 方法一样。 相关联的 Future 也会以 Failure 收场。

如果已经有了一个 Try，那可以直接把它传递给 Promise 的 complete 方法，以此来结束这个它。 如果这个 Try 是一个 Success，关联的 Future 会成功完成，否则，就失败。

### 12.5.4 基于 Future 的编程实践

如果想使用基于 Future 的编程范式以增加应用的扩展性，那应用从下到上都必须被设计成非阻塞模式。 这意味着，基本上应用层所有的函数都应该是异步的，并且返回 Future。

当下，一个可能的使用场景是开发 Web 应用。 **流行的 Scala Web 框架，允许你将响应作为 Future[Response] 返回，而不是等到你完成响应再返回。 这个非常重要，因为它允许 Web 服务器用少量的线程处理更多的连接。 通过赋予服务器 Future[Response] 的能力，你可以最大化服务器线程池的利用率**。

而且，应用的服务可能需要多次调用数据库层以及（或者）某些外部服务， 这时候可以获取多个 Future，用 for 语句将它们组合成新的 Future，简单可读！ 最终，Web 层再将这样的一个 Future 变成 Future[Response]。

但是该怎样在实践中实现这些呢？需要考虑三种不同的场景：

#### 12.5.4.1 非阻塞IO

应用很可能涉及到大量的 IO 操作。比如，可能需要和数据库交互，还可能作为客户端去调用其他的 Web 服务。

如果是这样，可以使用一些基于 Java 非阻塞 IO 实现的库，也可以直接或通过 Netty 这样的库来使用 Java 的 NIO API。 这样的库可以用定量的线程池处理大量的连接。

但如果是想开发这样的一个库，直接和 Promise 打交道更为合适。

#### 12.5.4.2 阻塞 IO

有时候，并没有基于 NIO 的库可用。比如，Java 世界里大多数的数据库驱动都是使用阻塞 IO。 在 Web 应用中，如果用这样的驱动发起大量访问数据库的调用，要记得这些调用是发生在服务器线程里的。 为了避免这个问题，可以将所有需要和数据库交互的代码都放入 future 代码块里，就像这样：
```
// get back a Future[ResultSet] or something similar:
Future {
  queryDB(query)
}
```
到现在为止，我们都是使用隐式可用的全局 ExecutionContext 来执行这些代码块。 通常，更好的方式是创建一个专用的 ExecutionContext 放在数据库层里。 可以从 Java的 ExecutorService 来它，这也意味着，可以异步的调整线程池来执行数据库调用，应用的其他部分不受影响。
```
import java.util.concurrent.Executors
import concurrent.ExecutionContext
val executorService = Executors.newFixedThreadPool(4)
val executionContext = ExecutionContext.fromExecutorService(executorService)
```
#### 12.5.4.3 长时间运行的计算

取决于应用的本质特点，一个应用偶尔还会调用一些长时间运行的任务，它们完全不涉及 IO（CPU 密集的任务）。 这些任务也不应该在服务器线程中执行，因此需要将它们变成 Future：
```
Future {
  longRunningComputation(data, moreData)
}
```
同样，最好有一些专属的 ExecutionContext 来处理这些 CPU 密集的计算。 怎样调整这些线程池大小取决于应用的特征，这些已经超过了本文的范围。

>总结
这一章里，我们学习了 Promise - 基于 Future 的并发范式的可写组件，以及怎样用它来完成一个 Future； 同时，还给出了一些在实践中使用它们的建议。


----------


----------

References:
[1]. http://windor.gitbooks.io/beginners-guide-to-scala/content/index.html