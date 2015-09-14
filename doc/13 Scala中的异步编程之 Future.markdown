# 13 Scala中的异步编程之 Future

标签（空格分隔）： 级别L2:资深类库设计者 深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------

Scala中的异步编程主要通过`Future`、`Promise`、`Async Library`及`Future Frameworks`中的任一个来实现。
本篇主要通过具体的示例来展示Scala中异步编程的方方面面。

## 13.1 Future实战

Future[T]是 scala.concurrent 包里的一个容器类型，代表一种返回值类型为 T 的计算。
这个计算是异步执行的，当一个 Future 计算完成时，如果计算成功，则代表在未来某一个时刻可以获取计算的值。
如果计算出错，则Future里的不再是值而是包含一个异常。
在使用 Future 时，只是从里面获取值或异常，所以 Future 只提供了读取计算结果的接口，而把结果写入到 Future 中的任务就交给了 Promise[T] 。

### 13.1.1 如何创建一个Future【Future computations】

```
/**
 * [Example 1]:How to start a future computation in an example.
 */
object FuturesComputation extends App {

  // we first import the contents of the scala.concurrent package
  import scala.concurrent._ 
  
  // we then import the global execution context from the Implicits object.
  // This makes sure that the future computations execute on global-the default
  // execution context you can use in most cases:
  import ExecutionContext.Implicits.global 
                                           
  
  /**
   * The order, in which the log method calls(in the future computation and the main thread) 
   * execute, is nondeterministic（random）.
   */
  Future {
    log(s"the future is here")
  }

  log(s"the future is coming")

  // to sleep 1 second in order to Future can finish and then exit main thread.
  Thread.sleep(1000) 
}

```


----------

### 13.1.2 通过轮询（polling）的方式获取值【不推荐的使用方式】

```
/**
 * [Example 2]: poll future value until it is completed
 * 
 * In this example, we used polling to obtain the value of the future.
 * Polling的方式就是每隔一段时间就会询问一次异步代码块是否结束，没有结束，值就为None。
 * 如果结束，就可以获得异步代码块的返回值。
 * 
 * Polling在Future中就是通过isCompleted方法和value实现的
 */
object FuturesDataType extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val buildFile: Future[String] = Future {
    val f = Source.fromFile("build.sbt")
    try f.getLines().mkString("\n") finally f.close()
  }

  log(s"started reading build file asynchronously")
  
  // The Future singleton object's polling methods(such as isCompleted)
  // are non-blocking, but they are also nondeterministic.
  log(s"status: ${buildFile.isCompleted}") 
  
  log(s"status: ${buildFile.value}") // print None
  
  Thread.sleep(250)
  log(s"status: ${buildFile.isCompleted}")
  log(s"status: ${buildFile.value}")
}
```

----------

### 13.1.3 Future中的回调（callbacks）【代替上面轮询的方式】

```
/**
 * [Example 3] [Future callbacks]
 * A callback is a function that is called once its arguments become available.
 * 
 * 我们真正需要的不是不停的询问是否异步代码块是否执行结束，而是留下一个方法，
 * 等异步代码块结束后能够自动调用这个方法来返回结果。
 * 这个就是Future中的callbacks方法干的事儿。
 * 
 * Scala Future callbacks: foreach, onSuccess[deprecated after 2.11]
 * The foreach method only accepts callbacks that handle values from 
 * a successfully completed future.
 *
 * 通过foreach method 只能处理Future成功后的情况
 */
object FuturesCallbacks extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  def getUrlSpec: Future[Seq[String]] = Future {
    val f = Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt")
    try f.getLines().toList finally f.close()
  }

  val urlSpec: Future[Seq[String]] = getUrlSpec

  /**
   * collect 方法示例：得到 (4, 16, 36, 64, 100)
   * 写法1：(1 to 10) filter (_%2==0) map (x=>x*x)
   * 写法2：for(x<-1 to 10 if x%2==0) yield x*x
   * 写法3：(1 to 10) collect { case x if x%2==0 => x*x }
   *
   * @param lines
   * @param word
   * @return
   */
  def find(lines: Seq[String], word: String) = lines.zipWithIndex collect {
    case (line, n) if line.contains(word) => (n, line)
  } mkString "\n"

  // we install a callback to the future using the foreach method. 
  // the equivalent of foreach method is called onSuccess, but might 
  // be deprecated after Scala 2.11.
  urlSpec foreach { 
    lines => log(s"Found occurrences of 'telnet'\n${find(lines, "telnet")}\n")
  }

  // The foreach method only accepts callbacks that handle values from 
  // a successfully completed future
  urlSpec foreach { 
    // 2 but the log statement in the callback can be called much later.
    lines => log(s"Found occurrences of 'password'\n${find(lines, "password")}\n") 
  }

  // 1 The log statement in the main thread immediately executes after 
  // the callback is registered
  log("callbacks installed, continuing with other work") 

  Thread.sleep(2000)
}
```

----------

### 13.1.4 Future中的异常处理

```
/**
 * [Example 4] [Futures and exceptions]
 * Just to handle failure.
 *
 * How to handle failures in asynchronous computations
 * we need another method to install failure callbacks, this method is called failed.
 * 
 * 假定Future失败后的处理，通过failed method.
 */
object FuturesFailure extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val urlSpec: Future[String] = Future {
    Source.fromURL("http://www.w3.org/non-existent-url-spec.txt").mkString
  }

  // The failed method is a failure callbacks that handle exception from a failure future
  urlSpec.failed foreach { 
    case t => log(s"exception occurred - $t")
  }

  Thread.sleep(5000)
}
```


----------


### 13.1.5 同时处理成功或异常的Future

```
/**
 *  [Example 5]
 *  Using the Try type for sometimes we want to subscribe to both 
 *  successes and failures in the same callback
 *  
 *  Future 在异步的情况下处理成功或失败
 *  同时处理成功和失败的情况， 通过onComplete method处理。
 */
object FuturesExceptions extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val file = Future { Source.fromFile(".gitignore-SAMPLE").getLines.mkString("\n") }

  file foreach {  // 异步调用: 只处理成功的情况，忽略失败的情况
    text => log(text)
  }

  file.failed foreach { // 异步调用: 只处理失败的情况，忽略成功的情况
    case fnfe: java.io.FileNotFoundException => log(s"Cannot find file - $fnfe")
    case t => log(s"Failed due to $t")
  }

  import scala.util.{Try, Success, Failure}

  // Using the Try type for sometimes we want to subscribe to both successes 
  // and failures in the same callback
  // The callback is onComplete method
  file onComplete { // 异步调用: 同时处理成功和失败的情况
    case Success(text) => log(text)
    case Failure(t)    => log(s"Failed due to $t")
  }
}
```


----------

### 13.1.6 Future VS Try

Future是异步的处理计算，并返回成功值或异常
Try是同步的处理计算，并返回成功值或异常

```
/**
 * The Try[T] objects are immutable objects used synchronously; 
 * unlike futures, they contain a value or an exception from the moment they are created. 
 * They are more akin to collections than to futures.
 * 
 * Try 只能在同步的情况下处理成功或失败
 */
object FuturesTry extends App {
  import scala.util._

  val threadName: Try[String] = Try(Thread.currentThread.getName)
  val someText: Try[String]   = Try("Try objects are created synchronously")
  
  val message: Try[String] = for {
    tn <- threadName
    st <- someText
  } yield s"$st, t = $tn"

  message match {
    case Success(msg)   => log(msg)
    case Failure(error) => log(s"There should be no $error here.")
  }
}
```


----------

### 13.1.7 Future的计算不能捕获致命异常

Future在异步计算过程中，可能会出现异常情况，则其返回值就是一个异常。
但是如果出现致命异常（InterruptedException）时，不能捕获而导致程序中断。

```
/**
 * Future computations do not catch fatal errors.
 */
object FuturesNonFatal extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val f = Future { throw new InterruptedException }     // fatal exception
  val g = Future { throw new IllegalArgumentException } // non fatal exception
  f.failed foreach { case t => log(s"error - $t") }
  g.failed foreach { case t => log(s"error - $t") }

  Thread.sleep(1000)

  /**
   * run result:
   * java.lang.InterruptedException
   * 	at org.learningconcurrency.ch4.FuturesNonFatal$$anonfun$17.apply(Futures.scala:153)
   * 	at org.learningconcurrency.ch4.FuturesNonFatal$$anonfun$17.apply(Futures.scala:153)
   * 	at scala.concurrent.impl.Future$PromiseCompletingRunnable.liftedTree1$1(Future.scala:24)
   * 	at scala.concurrent.impl.Future$PromiseCompletingRunnable.run(Future.scala:24)
   * 	at scala.concurrent.impl.ExecutionContextImpl$AdaptedForkJoinTask.exec(ExecutionContextImpl.scala:121)
   * 	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
   * 	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
   * 	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
   * 	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
   *  ForkJoinPool-1-worker-5: error - java.lang.IllegalArgumentException
   *
   *  Summary: Future computations do not catch fatal errors.
   */
}
```


----------

### 13.1.8 Future 中的 map

```
/**
 * map就是一个Functor，用于把 A => B
 * 例如：List[Int] => List[String]
 */
object FuturesMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
  import scala.util.Success

  val buildFile: Future[Iterator[String]] = Future { Source.fromFile("build.sbt").getLines }
  // 通过map，Future[Iterator[String]] => Future[String] 的转变，找出最长的一行
  val longestBuildLine: Future[String]    = buildFile.map(lines => lines.maxBy(_.length))
  
  val gitignoreFile = Future { Source.fromFile(".gitignore-SAMPLE").getLines }
  val longestGitignoreLine = for (lines <- gitignoreFile) yield lines.maxBy(_.length)

  longestBuildLine onComplete {
    case Success(line) => log(s"the longest line is '$line'")
  }

  longestGitignoreLine.failed foreach {
    case t => log(s"no longest line, because ${t.getMessage}")
  }
}
```


----------

### 13.1.9 组合多个Future 【不推荐的方式】

```
object FuturesFlatMapRaw extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val netiquette = Future { Source.fromURL("http://www.ietf.org/rfc/rfc1855.txt").mkString }
  val urlSpec = Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString }
  
  // 可读性差
  val answer = netiquette.flatMap { nettext =>
    urlSpec.map { urltext =>
      "First, read this: " + nettext + ". Now, try this: " + urltext
    }
  }

  answer foreach {
    case contents => log(contents)
  }
}
```


----------

### 13.1.10 通过 for 表达式来组合多个 Future 【推荐】

- **异步** 的方式使用 for 表达式

```
/**
 * Prefer for-comprehensions to using flatMap directly to make 
 * programs more concise and understandable.
 */
object FuturesFlatMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
  
  val netiquette = Future { Source.fromURL("http://www.ietf.org/rfc/rfc1855.txt").mkString }
  val urlSpec = Future {
    Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString 
  }
  
  // 可读性好
  val answer = for {
    nettext <- netiquette
    urltext <- urlSpec
  } yield {
    "First of all, read this: " + nettext + " Once you're done, try this: " + urltext
  }

  answer foreach {
    case contents => log(contents)
  }

}
```


----------

- **顺序** 的方式使用 for 表达式

```
/**
 * The nettext value is extracted from the first future. 
 * Only after the first future is completed, the second future computation starts
 */
object FuturesDifferentFlatMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val answer = for {
    nettext <- Future { Source.fromURL("http://www.ietf.org/rfc/rfc1855.txt").mkString }
    urltext <- Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString }
  } yield {
    "First of all, read this: " + nettext + " Once you're done, try this: " + urltext
  }

  answer foreach {
    case contents => log(contents)
  }

}
```


----------


### 13.1.11 通过 scala-async library来组合多个 Future 

- **异步** 的方式使用 scala-async library

```
object FuturesFlatMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
  import scala.async.Async.{async, await}
  
  val netiquette = Future { Source.fromURL("http://www.ietf.org/rfc/rfc1855.txt").mkString }
  val urlSpec = Future {
    Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString 
  }
  
  // 异步方式
  val answer = async {
    "First of all, read this: " + await(netiquette) + 
    " Once you're done, try this: " + await(urlSpec)
  }

  answer foreach {
    case contents => log(contents)
  }

}
```

----------

- **同步** 的方式使用 scala-async library

```
object FuturesFlatMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
  import scala.async.Async.{async, await}
  
  // 同步方式
  val answer = async {
    "First of all, read this: " + 
    await(Future { Source.fromURL("http://www.ietf.org/rfc/rfc1855.txt").mkString }) + 
    " Once you're done, try this: " + 
    await(Future {
      Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString 
    })
  }

  answer foreach {
    case contents => log(contents)
  }

}
```


----------

### 13.1.12 对异常的 Future 进行恢复

```
/**
 * So far, we have only considered future combinators that work with successful futures.
 * When any of the input futures fails or the computation in the combinator throws an 
 * exception, the resulting future fails with the same exception. 
 *
 * In some situations, we want to handle the exception in the future in the same way as
 * we handle exception with a try-catch block in sequential programming.
 *
 * A combinator that is helpful in these situations is called recover.
 * Using the recover combinator on the Future to provide a default operation 
 * if anything fails.
 */
object FuturesRecover extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val netiquetteUrl = "http://www.ietf.org/rfc/rfc1855.doc"
  val netiquette = Future { Source.fromURL(netiquetteUrl).mkString } recover {
    case f: java.io.FileNotFoundException =>
      "Dear boss, thank you for your e-mail." +
      "You might be interested to know that ftp links " +
      "can also point to regular files we keep on our servers."
  }

  netiquette foreach {
    case contents => log(contents)
  }

}
```


----------

### 13.1.13 Future中的常用方法

#### 13.1.13.1 reduce 处理Future列表（List[Future[A]]）

```
/**
 * reduce方法用于对一个Future列表进行fold计算，其中，初始值为第一个计算完成的Future的值。
 */
object FuturesReduce extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  // 理想情况: 列表中的Future全部都成功
  val squares: Seq[Future[Int]] = for (i <- 0 until 10) yield Future { i * i }
  // reduce: 完成 Seq[Future[Int]] => Future[Int] 的转换
  val sumOfSquares: Future[Int] = Future.reduce(squares)(_ + _)

  sumOfSquares foreach {
    case sum => log(s"Sum of squares = $sum")
  }

  // 现实情况就是：不可能所有Future都是成功的
  val futureWithException = List(Future( 1 + 2), Future(throw new RuntimeException("hello")))
  val reduceFutures = Future.reduce(futureWithException)(_ + _)

  // 不会被执行
  reduceFutures foreach { num => // 只处理成功的情况
    log(s"plus with exception = $num")
  }

  // 被调用执行，其结果是列表中第一个最新计算完成的异常的Future
  reduceFutures.failed foreach { e => // 只处理失败的情况
    log(s"plus with exception = $e")
  }

  Thread.sleep(2000)
}
```


----------

#### 13.1.13.2 traverse

```
/**
 * traverse方法用于 List[A] ==> Future[List[A]]的转换
 * sequence方法用于 List[Future[A]] ==> Future[List[A]]的转换
 *
 * {{{
 * http://qiita.com/mtoyoshi/items/297f6acdfe610440c719
 * }}}
 */
object FuturesTraverse extends App {

  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val list = List(1, 2, 3, 4, 5)

  // 这种方式会使用list的size个工作线程，这里是使用了5个线程
  val listFutures: List[Future[Int]] = list map (num => Future(num * num))
  listFutures.foreach { case future =>
    future foreach {
      num => log(s"num in future is $num")
    }
  }

  log("the message is printed by main thread!")

  // 这种方式只会使用一个工作线程
  val futureList: Future[List[Int]] = Future.traverse(list)(num => Future(num * num))
  futureList.foreach{ case listNum =>
    listNum foreach {
      num => log(s"num in list is $num")
    }
  }
  log("the message2 is printed by main thread!")

}
```


----------

```
/**
 * 如果只要列表中有一个Future是异常的，那么就不进行任何处理的情况，
 * 可以使用下面的例子的这种写法
 */
object FuturesWithExceptionTraverse extends App {

  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.util.{Try, Success, Failure}

  val list = List(1, 2, 3, 4, 5)

  // 这种方式会使用list的size个工作线程，这里是使用了5个线程【不优雅，异常能够处理到】
  // 这种正常的Future和异常的Future组成的集合，都可以处理到，但是方式不太好
  val listFutures: List[Future[Int]] = list map {num => 
    if (num % 2 == 0) Future(throw new RuntimeException(s"hello$num")) 
    else Future(num * num)
  }
  
  listFutures.foreach { case future =>
    future foreach {
      num => log(s"num in future is $num")
    }
    future.failed foreach {
      num => log(s"exception in future is $num")
    }
  }

  log("the message is printed by main thread!")

  // 这种方式只会使用一个工作线程【优雅，异常处理弱】
  // 这种方式是如果List中只要有一个异常的Future，最终的Future其实就是List中的第一个异常的Future
  // List[Future[Int]] => Future[List[Int]]
  val futureList: Future[List[Int]] = Future.traverse(list)(num =>
    if (num % 2 == 0)
      Future(throw new RuntimeException(s"hello$num"))
    else Future(num * num)
  )

  futureList.failed.foreach { case e =>
    log(s"exception in list is $e")
  }

  log("the message2 is printed by main thread!")


  /**
   * Future 里面的所有的call back，都是异步执行的。
   */
}
```


----------

#### 13.1.13.3 开发中的实用例子（sequence）

```
/**
 * 一个List中的Future，有的是成功的，有的是异常的。
 * 普通的做法就是把 List[Future[A]] ==> Future[List[A]]，
 * 但是在这个过程中，如果有一个异常的Future，成功的Future全部被忽略，
 * 最早确定失败的Future最为最后结果返回。
 *
 * 这显然不是我们需要的。我们在实际的开发中，需要的是把List中成功的Future进行计算，
 * 把失败的所有的Future进行异常输出
 */
object FuturesWithException extends App {

  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.util.{Try, Success, Failure}

  log("this log is printed by main thread: begin")

  /**
   * List[Future[Int]] => List[Future[Try[Int]]]
   * @param f
   * @return
   */
  def future2FutureTry(f: Future[Int]): Future[Try[Int]] = 
    f.map(Success(_)).recover { case e => Failure(e) }

  // 把异常的Future的异常信息输出，正常的Future进行结果的计算。
  // 那么如何做呢？

  // 第一种方式
  val list = List(1, 2, 3, 4, 5)

  // 首先生成一个 List[Future[Int]]
  val listFutures: List[Future[Int]] = list map { num =>
    if (num % 2 == 0) Future(throw new RuntimeException(s"hello$num"))
    else Future(num * num)
  }

  // 然后把 List[Future[Int]] => List[Future[Try[Int]]]
  val listFutureTry = listFutures.map(future2FutureTry)

  // 再把 List[Future[Try[Int]]] => Future[List[Try[Int]]]
  val futureListTry = Future.sequence(listFutureTry)

  // 最后分别处理成功的Future和异常的Future
  futureListTry.map(_.collect { case Success(num) => log(s"num in future is $num") })
  futureListTry.map(_.collect { case Failure(e) => log(s"exception in future is $e") })

  log("this log is printed by main thread: end")
  Thread.sleep(4000)
}
```


----------

第二种方式：

```
object FuturesWithException2 extends App {

  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.util.{Try, Success, Failure}

  log("this log is printed by main thread: begin")

  // 把异常的Future的异常信息输出，正常的Future进行结果的计算。
  // 那么如何做呢？

  // 第二种方式，直接一步到位，生成Future[List[Try[Int]]]
  val list = List(1, 2, 3, 4, 5)

  val futureListTry: Future[List[Try[Int]]] = Future.traverse(list){num =>
   val future = if (num % 2 == 0)
      Future(throw new RuntimeException(s"hello$num"))
    else Future(num * num)

    future.map(Success(_)).recover{case e => Failure(e)}
  }

  // 最后分别处理成功的Future和异常的Future
  futureListTry.map(_.collect { case Success(num) => log(s"num in future is $num") })
  futureListTry.map(_.collect { case Failure(e) => log(s"exception in future is $e") })

  log("this log is printed by main thread: end")
  Thread.sleep(4000)
}

```

----------


----------


References:
[1]. learning concurrent programming in Scala 
[2]. http://qiita.com/mtoyoshi/items/297f6acdfe610440c719