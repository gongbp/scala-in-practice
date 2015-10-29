# 0 Scala命令行详解

标签（空格分隔）：深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------

### 1. 进入Scala命令行
在安装Scala后，在命令行中输入scala，即可进入Scala的REPL中。
```
$ scala
Welcome to Scala version 2.11.7 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_66).
Type in expressions to have them evaluated.
Type :help for more information.

scala>

```

### 2. 操作命令行
而在REPL中进行输入时，必备的几个快捷键可以给你带来更多的便利：
>命令行中的光标移动：
移动到行首：Ctrl+A
移动到行尾：Ctrl+E
删除光标之前的所有字符：Ctrl+U
删除光标之后的所有字符：Ctrl+K
一个一个的删除光标之后的字符：Ctrl+D

>注：上面的快捷键与字母大小写无关

### 3. 退出命令行
从Scala的REPL中退出，两种方式：
>第一种方式： Ctrl+C
 第二种方式： scala> :quit
 
### 4. 命令行中运行Scala脚本文件 
Scala的命令行支持两种运行方式：
>命令行交互式方式
批处理方式（batch mode） 
 
创建一个Scala脚本HelloWorld.scala，内容如下：
```
println("This script is used to testing loading a file in Scala REPL!")
```
然后加载这个提前写好的Scala脚本HelloWorld.scala到Scala REPL中并执行（**命令行交互式方式**）：
```
scala> :load HelloWorld.scala // 加载并执行
Loading HelloWorld.scala...
This script is used to testing loading a file in Scala REPL! // 执行结果

scala>

```

或者使用批处理方式，就是通过scala命令来运行一个脚本文件：
```
$ scala HelloWorld.scala
This script is used to testing loading a file in Scala REPL!
```

>注：Scala中的脚本文件一般是若干方法或函数的调用，不涉及到继承或调用别的类文件。

### 5. Scala中的Shell脚本
Scala中的Shell脚本，创建一个hello.sh文件，内容如下：
```
#!/usr/bin/env scala
/**
 * Running as a Stand-alone Script on Unix-like Systems
 *
 * $ ./hello.sh world
 * $ scala hello.sh world
 *
 **/
println("Hello " + args(0)) // 接收一个参数
```
在该文件的目录下，执行该脚本：
```
$ ./hello.sh world
Hello world

$ scala hello.sh world
Hello world

```

### 6. 编译Scala中的object，class, trait，并运行

创建一个Scala object文件，Sample.scala，内容如下：
```
object Sample extends App {
  println("Hello Scala")
}

```
这个并非是一个Scala 脚本，如果以脚本的方式运行，不会有任何的结果输出：
```
$ scala Sample.scala 

// 无输出
```

要想运行这样的非Scala脚本文件，必须先编译，在运行：
```
// 在Sample.scala文件的目录下，编译该文件
$ scalac Sample.scala 

// 使用Scala的命令运行
$ scala Sample
Hello Scala

// 或者使用Java 命令运行
$ java -classpath /usr/local/Cellar/scala/2.11.7/libexec/lib/scala-library.jar:. Sample
Hello Scala

```
