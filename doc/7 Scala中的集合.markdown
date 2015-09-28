# 7 Scala中的集合

标签（空格分隔）： 级别A2:中级程序设计者 深入学习Scala

[TOC]

------------------------------------------------------------

------------------------------------------------------------
## 7.1 集合概述

我们先对集合进行一个整体使用上的介绍，再逐个深入的去说明各个集合的特性，使用场景及具体用法。

Scala的主要集合特质的继承层级如下图：

![](http://i.stack.imgur.com/bSVyA.png)

①. Traverable代表可以遍历全部数据的机制的集合，而且能够反复地遍历。这个特质有一个从GenericTraversableTemplate特质中获得的抽象foreach方法。使用Traverable特质需要实现这个foreach抽象方法。
```
class MyTraversable(age: Int) extends Traversable[Int]{
  override def foreach[U](f: Int => U): Unit =
    println("hello-" + f(age))
}

object Test extends App {
  val t = new MyTraversable(11)
  println(t) // 打印TraversableTest对象，
             // 会调用TraversableTest的toString方法。
             // 而toString方法会自动调用foreach方法
  /**
   * print result:
   * hello-()
   * TraversableTest(11)
   */
}
```

②. Iterable类型的集合可以生成迭代器（Iterator）来访问集合中的所有元素。

```
scala> val coll = Iterable(1, 4, 2)
coll: Iterable[Int] = List(1, 4, 2) // 缺省使用List

scala> val iter = coll.iterator
iter: Iterator[Int] = non-empty iterator

scala> while(iter.hasNext){
     |   println(iter.next)
     | }
1
4
2 
```
③. Seq是一个有先后顺序的值的序列，代表连续有序，并且元素允许重复。（这里的顺序是指添加到序列中的元素的先后次序，不是元素的排序顺序。）

Seq代表连续有序（有序指的是追加元素时的顺序，不代表是排序的sorted）的集合。
在使用序列Seq时，应该仅用来把有序集合和Set/Map区分开（Set和Map是无序的）。也就是说，如果元素插入集合的顺序是重要的，并且允许重复元素，那么就应该使用Seq。

Seq经常被用在抽象方法里，因为算法经常以Seq的两个子类为目标数据结构：IndexdSeq和LinearSeq。在合适的场合应优先选择这两个集合类型。

```
scala> val seq = Seq(1,19,15, 1)
seq: Seq[Int] = List(1, 19, 15, 1) // 缺省使用List

scala> seq foreach println
1
19
15
1
```

④. Set是一组没有先后顺序的值的集合，并且元素不允许重复。
```
scala> val set = Set(1, 3, 4, 1)
set: scala.collection.immutable.Set[Int] = Set(1, 3, 4) // 消除重复元素

scala> set foreach println
1
3
4

```
⑤. Map是一组无序不重复的键值对。
```
scala> val map = Map("jack"->30, "lily" -> 28)
map: scala.collection.immutable.Map[String,Int] = Map(jack -> 30, lily -> 28)

scala> map("jack")
res3: Int = 30

scala> map.get("jack")
res4: Option[Int] = Some(30)

scala> for((name, age) <- map) println(s"name = $name, age = $age")
name = jack, age = 30
name = lily, age = 28
```

②③④⑤构造集合的方式，是通过集合的伴生对象的apply方法来创建集合实例的。

Scala中的常用集合主要分为三类：

- 可变集合 VS 不可变集合
- 线性(串行) VS 并行
- 即时计算 VS 延迟计算

集合的时间复杂度英文见[这里](http://www.scala-lang.org/docu/files/collections-api/collections_40.html)，中文见[这里](https://code.csdn.net/DOC_Scala/chinese_scala_offical_document/file/PerformanceCharacteristics.md)。

## 7.2 集合的选择

scala的集合分为可变集合（mutable）和不可变（immutable）集合。

一个可变集合（ mutable  ）能够对该集合本身进行长度的扩展（**集合长度不固定**），能够往该集合里追加元素，删除该集合里的元素，修改该集合里面的元素。**每一次对可变集合的操作，都是直接作用在该集合上，所以返回的就是被修改后的原可变集合本身**。例如：
```
// 引入可变集合包
scala> import scala.collection.mutable
import scala.collection.mutable

// 初始化一个可变空集合，其长度为0
scala> val lb = mutable.ListBuffer.empty[String]
lb: scala.collection.mutable.ListBuffer[String] = ListBuffer()

// 进行添加元素操作，则lb集合本身长度被扩展1
scala> lb.append("hello")

// 直接作用在本集合上的效果，返回的就是被修改后的原可变集合本身
scala> lb
res8: scala.collection.mutable.ListBuffer[String] = ListBuffer(hello)

scala> lb.append("world")

scala> lb
res10: scala.collection.mutable.ListBuffer[String] = ListBuffer(hello, world)

// 对可变集合进行删除元素操作
// Removes the element on a given index position. May take time linear in the buffer size.
scala> lb.remove(1)
res11: String = world

scala> lb
res12: scala.collection.mutable.ListBuffer[String] = ListBuffer(hello)

// 对可变集合进行元素的修改操作
// Replaces element at index n with the new element newelem. Takes time linear in the buffer size. (except the first element, which is updated in constant time).
scala> lb.update(0, "haha")

scala> lb
res14: scala.collection.mutable.ListBuffer[String] = ListBuffer(haha)

```

一个不可变（immutable）集合（**集合长度固定，不可扩展**），则是你在该集合上的任何操作，都不会改变原集合本身，而是返回一个新的集合。
你仍然可以做一些类似的增加，删除，或者更新，但是实际上，跟java的string一样他返回了一个新的对象，这里就是指返回了一个新的集合，而老的集合没有改变。例如：
```
scala> val list = List()
list: List[Nothing] = List()

// 添加一个元素，返回一个新集合list2
scala> val list2 = 1 :: list
list2: List[Int] = List(1)

// 原集合不变
scala> list
res16: List[Nothing] = List()

// 删除list2的第一个元素，返回一个新的集合res18
scala> list2.tail
res18: List[Int] = List()

// list2不变
scala> list2
res19: List[Int] = List(1)

```

在选择使用集合时，应该优先使用不可变集合。不可变集合适用于大多数情况，让程序易于理解和推断，因为它们是引用透明的( referentially transparent )因此缺省也是线程安全的。

而在使用可变集合时，明确地引用可变集合的命名空间。不要使用import scala.collection.mutable._ 然后引用 Set ，应该用下面的方式替代：
```
 import scala.collections.mutable
 val set = mutable.Set()
```
这样就很明确在使用一个可变集合。

使用集合类型缺省的构造函数。每当你需要一个有序的序列(不需要链表语义)，用 Seq() 等诸如此类的方法构造：
```
 val seq = Seq(1, 2, 3)
 val set = Set(1, 2, 3)
 val map = Map(1 -> "one", 2 -> "two", 3 -> "three")
``` 
这种风格**从语意上分离了集合与它的实现**。
让集合库使用更适当的类型。比如，你需要Map，而不是必须要一个红黑树(Red-Black Tree，注：红黑树TreeMap是Map的实现者)

此外，默认的构造函数通常使用专有的表达式，例如：Map() 将使用有3个成员的对象(专用的Map3类)来映射3个keys。

上面的推论是：
在你自己的方法和构造函数里，适当地接受最宽泛的集合类型。
通常可以归结为Iterable, Seq, Set, 或 Map中的一个。
如果你的方法需要一个 sequence，使用 Seq[T]，而不是List[T]。

## 7.3 不可变集合

不可变集合是Scala的默认集合类型。在一般编程任务中，不可变集合有很多超出可变集合的优点。尤其重要的一点是不可变集合可以在多线程之间共享而无需加锁。

不可变集合的继承树:

![](http://i.stack.imgur.com/2fjoA.png)

说明：
![](http://i.stack.imgur.com/szWUr.png)

根据不可变集合的继承关系，通常有三种类型的集合供使用：

- Set: 无序不重复
- Seq: 有序可重复
- Map: 无序不重复键值对(key -> value)

Scala不可变集合的设计目标是提供既高效又安全的实现。这些集合中的大部分都使用高级技巧来在集合的不同版本之间共享内存。其中最常用的不可变的序列（Seq）集合分别是**Vector，List，Stream，Stack, Queue, Range, String**。

----------

### 7.3.1 Vector [不可变集合][线性(串行)][即时计算]
 
算法只处理序列的首部元素时，List 是非常高效的。
访问、添加和删除 List的首部元素都只需要常数时间O(1)，
但是访问 List中首部外的元素需要正比于被访问元素深度的线性时间复杂度O(n)。

Vector (从 scala 2.8 中引入) 这种集合类型很好的解决了 List 上随机访问操作低效的问题。
Vector 访问任何元素都是常数时间复杂度。
这里的常数要比访问 List 的首部或者是访问数组中的元素时的时间复杂度常数更大，但是仍然是一个常数时间复杂度。
因此，使用 Vector 的算法无需像 List 一样为了高效而注意只访问序列的首部。他们能访问和修改任意位置上的元素，从而也更便于程序编写。

Vector 内部表示为一个高分支因子的树（**前缀树**）(一棵树或图的分支因子是每个节点上子节点的数目)。
每个树节点包含最多32个元素（叶子节点）或者最多32个其他树节点。
最多含有32个元素的 Vector 可以用单个节点表示。最多含有 32*32 个元素的 Vector 可以表示为一个两层的树。从树的根节点到最终的元素节点经过两跳（也就是32叉树的深度为3）就足够表示最多 2^15 （= 2^5 * 2^5 * 2^5 = 32 * 32 * 32）就足够表示最多 2^15 个元素的 Vector，经过3跳就足够表示最多含有 2^20 个元素的 Vector，四跳就足够表示最多 2^25 个元素的 Vector，5跳就足够表示最多含有 2^30 个元素的 Vector。因此，对于所有合理长度的 Vector 来说，一个元素的选择操作涉及到最多 5 次数组的选择操作。这就是所说的“常数时间”复杂度的意思。

Scala2.8为了提高list的随机存取效率而引入的新集合类型Vector（而list存取前部的元素快，越往后越慢）。

Vector的性能：
|  ____  |head|tail|apply|update|prepend|append|insert
|--------------|---------------------------------------|
|Vector|eC|	eC|	eC	|eC	|eC	|eC	|-|




----------


1.Vector 的创建和修改方式与其他序列类型一样。

```
// 创建一个空的Vector集合
scala> val vec1 = Vector() 
vec1: scala.collection.immutable.Vector[Nothing] = Vector()

//等价于下面
scala> val vec2 = scala.collection.immutable.Vector.empty  
vec2: scala.collection.immutable.Vector[Nothing] = Vector()  

// 创建一个包含指定元素的集合
scala> val vec3 = Vector(1, 2, 3)
vec3: scala.collection.immutable.Vector[Int] = Vector(1, 2, 3)

// 往一个已知的Vector的尾部追加元素，追加的元素放在尾部
scala> val vec4 = vec1 :+ 1 :+ 2  // A copy of this vector with an element appended.
vec4: scala.collection.immutable.Vector[Int] = Vector(1, 2)  

// 往一个已知的Vector的头部追加元素，追加的元素放在头部
scala> val vec5 = 100 +: vec4  // A copy of the vector with an element prepended.
vec5: scala.collection.immutable.Vector[Int] = Vector(100, 1, 2)  

// 根据索引获得Vector里面的值
scala> vec5(0)  
res1: Int = 100  
```

----------


2.初始化Vector：

```
scala> val v = Vector.empty
v: scala.collection.immutable.Vector[Nothing] = Vector()

scala> v :+ 30 // 尾部追加
res0: scala.collection.immutable.Vector[Int] = Vector(30)

scala> 40 +: v // 头部追加
res1: scala.collection.immutable.Vector[Int] = Vector(40)

scala> val vector = 0 +: v :+ 10 :+ 20　// Vector(0, 10, 20), Vector 那一边始终有":"
vector: scala.collection.immutable.Vector[Int] = Vector(0, 10, 20)

```


----------


3.根据索引取值（索引从0开始）:

```
// 获得Vector中的第二个元素
scala> vector(1) // (也可以写成 vector apply 1)。索引 1 处的序列元素值
res3: Int = 10

// 等价于下面的写法
scala> vector apply 1
res5: Int = 10

// 返回vector的所有索引组成的Range
scala> vector.indices // vector 的索引范围，从 0 到 vector.length - 1 
res9: scala.collection.immutable.Range = Range(0, 1, 2)

// 判断一个索引值是否在Vector中存在
scala> vector isDefinedAt 10 // 判断索引 10 是否包含在 vector.indices 中
res7: Boolean = false
```

----------

4.求一个Vector的长度（既元素个数）:

```
scala> vector.length // 序列的长度（与 size 相同意义）
res10: Int = 3

scala> vector.size
res11: Int = 3

scala> vector.lengthCompare(15) // 如果 v2 长度小于 15，则返回 v2.length-15；
res17: Int = -12                // 如果 v2 长度大于 15，则返回 v2.length-15 ；
                                // 如果两者长度相同，则返回 0。
                                // 这个方法在其中一个序列长度无限的情况下任然有效。
```


----------


5.根据Vector中的值求其索引:

```
// 获得元素2的索引值，如果不包含改元素，返回索引-1
scala> vector indexOf 2
res9: Int = -1

// 获得元素20的索引，索引值为2
scala> vector indexOf 20
res10: Int = 2

// 从左往右
scala> vector indexOf 2 // 序列 vector 中等于 2 的第一个元素的索引值（存在几种变体）
res20: Int = -1

// 从右往左
scala> vector lastIndexOf 2 // 序列 vector 中等于2 的最后一个元素的索引值（存在几种变体）
res21: Int = -1

scala> val v = Vector(10, 20)
v: scala.collection.immutable.Vector[Int] = Vector(10, 20)

// // 从左往右
scala> vector indexOfSlice v // 返回满足此条件的第一个索引值：从该索引开始的后续序列包含序列v
res13: Int = 1

// vector lastIndexOfSlice v 返回满足此条件的最后一个索引值：从该索引开始的后续序列包含序列 v
scala> vector lastIndexOfSlice v // 从右往左
res14: Int = 1

xs indexWhere p 序列 xs 中满足谓词条件 p 的第一个元素的索引值（存在几种变体） 

xs segmentLength (p, i) 从 xs(i) 开始的满足谓词条件 p 的最长连续序列的长度（即该序列中素有元素满足谓词条件 p） 

xs prefixLength p 从第一个元素开始的满足谓词条件 p 的最长连续序列的长度
```


----------

6.追加元素：

```
 x +: xs 将 x 添加到 xs 前形成的一个新的序列 
 xs :+ x 将 x 添加到 xs 后形成的一个新的序列 
 xs padTo (len, x) 将 x 逐个添加到 xs 直到达到长度 len
```


----------


7.更新元素:

```
 xs patch (i, ys, r) 将 xs 序列中从索引 i 开始的 r 个元素用序列 ys 替换 
 xs updated (i, x) 返回将索引 i 处的元素替换为 x 的一个新的集合 
 xs(i) =  x 将序列 xs 中索引 i 处的元素替换为 x（也可以这样写 xs.update(i, x)，这个方法只对 mutable.Seq 可用）
                  
```


----------


8.排序`Sorting:`  
 xs.sorted 返回对 xs 中元素按照该序列中元素类型的标准顺序进行排序的一个新的序列 
 xs sortWith lt 返回对 xs 中元素按照 lt 比较操作进行排序的一个新的序列 
 xs sortBy f 返回对 xs 中元素按照每个元素应用 f 函数的计算结果进行排序的一个新的序列


----------


9.反转`Reversals:`  
 xs.reverse 返回序列 xs 的一个逆序序列 
 xs.reverseIterator 返回序列 xs 的一个逆序的迭代器 
 xs reverseMap f 返回对序列 xs 逆序应用函数 f 得到的一个函数结果的序列


----------


10.比较`Comparisons: `   
 xs startsWith ys 判断序列 xs 是否以序列 ys 开头 
 xs endsWith ys 判断序列 xs 是否以序列 ys 结尾 
 xs contains x 判断序列 xs 是否包含元素 x 
 xs containsSlice ys 判断序列 xs 是否含有一个与序列 ys 值相同的序列 
 (xs corresponds ys) (p) 判断序列 xs 中每一个元素与序列 ys 中对应索引的元素是否满足谓词条件 p


----------


11.`Multiset Operations:`  
 xs intersect ys 序列 xs 中元素与序列 ys 中元素的**交集**，结果保留 xs 中元素的顺序 
 xs diff ys 序列 xs 中元素与序列 ys 中元素的**差集**，结果保留 xs 中元素的顺序 
 xs union ys 序列 xs 与序列 ys 的**并集**，同 xs ++ ys 
 xs.dintinct 返回序列 xs 中**去掉重复元素**后的一个新的序列


----------


Vector 是不可变的，因此无法原地改变当前 Vector 中的元素。
但是，利用 updated 方法可以创建一个与所给定 vector 只有一个元素差异的新的 Vector。
```
scala> v2 updated (1,100)
res4: scala.collection.immutable.Vector[Int] = Vector(0, 100, 20)

Seq的缺省实现是List：
Seq(1,2,3) // List(1, 2, 3)

IndexSeq的缺省实现是Vector:
IndexSeq(1,2,3) // Vector(1, 2, 3)

```


----------


----------

### 7.3.2 List [不可变集合][线性(串行)][即时计算]

Scala中的List是一个不可变的**单链表**，具有O(1)复杂度的头/尾分解和头部添加操作。

在List的头部做添加/删除元素操作是常数时间，而尾插法是线性时间。
所以对List的操作推荐使用在头部进行。
头插法的方式添加元素，其使用场景是：先插入的元素后使用，类似于堆栈进栈的过程，先进后出，先插入的后使用。这样会由不错的性能。

List的性能：
|  ____  |head|tail|apply|update|prepend|append|insert
|--------------|---------------------------------------|
|List|C	|C|	L|	L|	C|	L|	-|


1.创建List：

```

scala> val list = List(1, 3, 4, 5, 6)
list: List[Int] = List(1, 3, 4, 5, 6)

// 或者
scala> val list = List(1 to 6: _*)
list: List[Int] = List(1, 2, 3, 4, 5, 6)

scala> val list1 = List("a", "b", "c", "d")
list1: List[String] = List(a, b, c, d)

// 或者
scala> val list1 = List('a' to 'd': _*) map (_.toString)
list1: List[String] = List(a, b, c, d)

// 元素合并成List：
scala> val list2 = "a" :: "b" :: "c" :: Nil // Nil是必须的
list2: List[String] = List(a, b, c)

// :: 这个是List里面往头部追加元素的方法。在Scala里面有个约定，凡是以冒号结尾的方法都是右结合的
scala> val list3 = "begin" :: list2 // 向已知的List的头部追加一个元素，并生成一个新的List，而原List不变。等价于list2.::("begin")
list3: List[String] = List(begin, a, b, c) // 生成的新的List

// 多个List合并用++，也可以用:::(不如++)
// ++ can be used with any Traversable
// ::: can only be used with lists
val list4 = list2 ++ "end" ++ Nil 
val list4 = list2 ::: "end" :: Nil // 相当于 list2 ::: List("end")
```

----------

2.通过List定义变量

````
建议定义方式：
scala> val head::body = List(4,"a","b","c","d")
head: Any = 4
body: List[Any] = List(a, b, c, d)

scala> val a::b::c = List(1,2,3)
a: Int = 1
b: Int = 2
c: List[Int] = List(3)
```

----------


3.定义固定长度的List：
```
scala> List.fill(10)(2) 
res15: List[Int] = List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2)

scala> List.fill(10)(scala.util.Random.nextPrintableChar)
res16: List[Char] = List(B, B, ', z, v, E, T, o, [, -)

scala> List.fill(10)(scala.util.Random.nextInt(101))
res17: List[Int] = List(74, 90, 64, 22, 13, 55, 35, 18, 96, 15)
```


----------


4.List的常用方法汇总：

`take drop splitAt`
```
1 to 10 by 2 take 3 // Range(1, 3, 5) 要满足条件的前部分
1 to 10 by 2 drop 3 // Range(7, 9)    要满足条件的后部分
1 to 10 by 2 splitAt 3 // (Range(1, 3, 5),Range(7, 9)) 要两部分
```

`takeWhile, dropWhile, span`
```
while语句的缩写，
takeWhile (...)等价于：while (...) { take }
dropWhile (...)等价于：while (...) { drop }
span (...)等价于：while (...) { take; drop }
   
 
1 to 10 takeWhile (_<5) // (1,2,3,4)
1 to 10 takeWhile (_>5) // ()
10 to (1,-1) takeWhile(_>6) // (10,9,8,7)
1 to 10 takeWhile (n=>n*n<25) // (1, 2, 3, 4)


如果不想直接用集合元素做条件，可以定义var变量来判断：
例如，从1 to 10取前几个数字，要求累加不超过30：
var sum=0;
val rt = (1 to 10).takeWhile(e=> {sum=sum+e;sum<30}) // Range(1, 2, 3, 4, 5, 6, 7)
注意：takeWhile中的函数要返回Boolean，sum<30要放在最后；
 
1 to 10 dropWhile (_<5)       // (5,6,7,8,9,10)
1 to 10 dropWhile (n=>n*n<25) // (5,6,7,8,9,10)
 
1 to 10 span (_<5)       // ((1,2,3,4),(5,6,7,8)
List(1,0,1,0) span (_>0) // ((1), (0,1,0))

// 注意，partition是和span完全不同的操作
List(1,0,1,0) partition (_>0) // ((1,1),(0,0))
```

`partition span splitAt groupBy`
```
val (a,b) = List(1,2,3,4,5).partition(_%2==0) // (List(2,4), List(1,3,5))可把Collection分成：满足条件的一组，其他的另一组。

// 和partition相似的是span，但有不同：
List(1,9,2,4,5).span(_<3)       // (List(1),List(9, 2, 4, 5))，碰到不符合就结束
List(1,9,2,4,5).partition(_<3)  // (List(1, 2),List(9, 4, 5))，扫描所有
 
List(1,3,5,7,9) splitAt 2    // (List(1, 3),List(5, 7, 9))
List(1,3,5,7,9) groupBy (5<) // Map((true,List(7, 9)), (false,List(1, 3, 5)))

```

>请注意
在List的头部进行操作，比尾部效率要高，这是因为List是由单链表实现，访问第一个元素只需要O(1)的时间，而最后一个元素则需要O(n)。 因此使用List时，请尽量设计为越常访问的数据在越靠前，构建List时，尽量从头部添加元素。

List的常用方法[见这里](http://www.tutorialspoint.com/scala/scala_lists.htm)
更多关于List的内容参考[这里](http://stackoverflow.com/questions/1241166/preferred-way-to-create-a-scala-list)


----------


----------


### 7.3.3 Stream [不可变集合][线性(串行)][延迟计算]【级别 A3：专家程序设计者】

Stream与List比较类似，只不过它的元素是延迟计算的。
正因为如此，Stream才可以无限长。只有被请求的元素才被计算出来。另外，Stream的性能特征与 List 相同。

List 可以用 :: 操作符构建，而 Stream 可以用类似的 #:: 操作符构建。

Stream的性能：
|  ____  |head|tail|apply|update|prepend|append|insert
|--------------|---------------------------------------|
|Stream|C	|C|	L|	L|	C|	L|	-|


下面是一个简单的包含 1、2、3 三个整数的Stream 例子。

```
scala> val str = 1 #:: 2 #:: 3 #:: Stream.empty
str: scala.collection.immutable.Stream[Int] = Stream(1, ?)

// 或者
scala> val str = 1 #:: 2 #:: 3 #:: Stream()
str: scala.collection.immutable.Stream[Int] = Stream(1, ?)
```

这个 Stream 的头是 1，尾部列表包含 2 和 3。因为这里的尾部还有计算，所以没有打印出来。Stream 的元素是**延迟计算**的，而且 Stream 的 toString 方法不会触发 Stream 的元素的计算。
        
下面是一个复杂一点的例子。它用 Stream 来计算给定前两个元素的 Fibonacci 数列。一个 Fibonacci 数列满足序列中任意一个元素的值是前两个元素之和。

```
scala> def fibFrom(a: Int, b: Int): Stream[Int] = a #:: fibFrom(b, a+b)
fibFrom: (a: Int, b: Int)Stream[Int] 
```

这个函数看起来很容易。这个序列的首个元素很清楚是 a，并且序列的剩余部分是一个以 b、a+b 开头的 Fibonacci 数列。微妙的地方在于计算这个序列不会导致无限循环。如果函数中用 :: 替代 #:: 操作符，那么对函数的每一次调用都会触发另一次调用，这将会导致无限递归。因此，这里使用 #:: 操作符，这样的话右手边的值除非被请求否则不会被计算。下面是以两个 1 开头的 Fibonacci 数列的前面一些元素。

```
// 取Fibonacci数列的前7个数，使用了take照样延迟计算
scala> fibFrom(1,1).take(7)
res0: scala.collection.immutable.Stream[Int] = Stream(1, ?)

// 要获得结果，必须在take的基础上调用force方法，才可得到计算结果
scala> fibFrom(1,1).take(7).force
res1: scala.collection.immutable.Stream[Int] = Stream(1, 1, 2, 3, 5, 8, 13)

//或者对res0调用toList方法，也会强制计算结果
scala> res0.toList
res2: List[Int] = List(1, 1, 2, 3, 5, 8, 13)
```


----------

**Stream的一些常用小例子：**

```
Stream相当于lazy List，避免在中间过程中生成不必要的集合。

定义生成：
val st = 1 #:: 2 #:: 3 #:: Stream.empty // Stream(1, ?)
 
例子：fib数列的Stream版本简单易懂
def fib(a: Int, b: Int): Stream[Int] = a #:: fib(b,  a+b)

val fibs = fib(1, 1).take(7).toList // List(1, 1, 2, 3, 5, 8, 13)

fib数列的前后项比值趋于黄金分割：
def fn(n:Int) = fib(1,1)(n) // 获得第n个fibnacci数列中的值

1 to 10 map (n=> 1.0*fn(n)/fn(n+1)) // Vector(0.5, 0.666, ..., 0.618)
 
例子1：
scala> Range(1,10)
res23: scala.collection.immutable.Range = Range(1, 2, 3, 4, 5, 6, 7, 8, 9)

scala> 1 until 10
res24: scala.collection.immutable.Range = Range(1, 2, 3, 4, 5, 6, 7, 8, 9)

// 把1到50000000的数全部放入内存，在过滤出所有13的倍数并生成一个中间集合，再从这个中间集合中取第二个13的倍数。当然会很慢，且消耗大量内存。
Range(1,50000000).filter (_ % 13==0)(1) // 26, 但很慢，需要大量内存

// 只计算前26个数
Stream.range(1,50000000).filter(_%13==0)(1) // 26，很快，只计算最终结果需要的内容

注意：
第一个版本在filter后生成一个中间collection，size=50000000/13;而后者不生成此中间collection，只计算到26即可。

scala> Stream.range(1,50000000).filter{e => println(s"hello-$e");e%13==0}(0) // 获得第一个13的倍数
hello-1
hello-2
hello-3
hello-4
hello-5
hello-6
hello-7
hello-8
hello-9
hello-10
hello-11
hello-12
hello-13
res7: Int = 13 //计算前13个数，只是延迟计算了，而非跳过前面的数，直接计算第13个数。延迟就是需要的时候再从头计算。

scala> Stream.range(1,50000000).filter{e => println(s"hello-$e");e%13==0}(1) // 获得第二个13的倍数
hello-1
hello-2
hello-3
hello-4
hello-5
hello-6
hello-7
hello-8
hello-9
hello-10
hello-11
hello-12
hello-13
hello-14
hello-15
hello-16
hello-17
hello-18
hello-19
hello-20
hello-21
hello-22
hello-23
hello-24
hello-25
hello-26
res8: Int = 26 // 计算了前26个数，只是延迟计算了，而非跳过前面的数，直接计算第13个数。延迟就是需要的时候再从头计算。


 
例子2：
(1 to 100).map(i=> i*3+7).filter(i=> (i%10)==0).sum // map和filter生成两个中间collection

(1 to 100).toStream.map(i=> i*3+7).filter(i=> (i%10)==0).sum

例子3：判断一个数是否为一个List中任意两个数之和
def sumExists2(values: List[Int], target: Int): Boolean =
    values.toStream.combinations(2).exists(s2 => s2.sum == target)

```


----------

**view**

在某类型的集合对象上调用view方法，得到相同类型的集合，但所有的transform函数都是lazy的，从该view返回调用force方法。

例子：
```
scala> val v = Vector(1 to 10:_*)
v: scala.collection.immutable.Vector[Int] = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

scala> v map (1+) map (2*)
res9: scala.collection.immutable.Vector[Int] = Vector(4, 6, 8, 10, 12, 14, 16, 18, 20, 22)

```

以上过程得生成2个新的Vector，而：

```
scala> val v = Vector(1 to 10:_*)
v: scala.collection.immutable.Vector[Int] = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

scala> v.view map (1+) map (2*) force
res10: Seq[Int] = Vector(4, 6, 8, 10, 12, 14, 16, 18, 20, 22)
```
只在过程中生成1个新的Vector，相当于：
```
v map (x=> 2 * (1 + x))
```
又例如：
```
// 在集合上调用view
((1 to 1000000000) view).take(3).force // Vector(1,2,3)

// 使用Stream
Stream.range(1,1000000000).take(3).force //  Stream(1, 2, 3)

```
**stream和view的区别：**

先来个正常的：
```
scala> (0 to 5).map((x:Int)=>{println(x);x*2}).foreach(println)
0
1
2
3
4
5
0
2
4
6
8
10
```
再来个stream版的：
```
scala> (0 to 5).toStream.map((x:Int)=>{println(x);x*2}).foreach(println)
0
0
1
2
2
4
3
6
4
8
5
10
```
最后来个view版的：
```
scala> (0 to 5).view.map((x:Int)=>{println(x);x*2}).foreach(println)
0
0
1
2
2
4
3
6
4
8
5
10
```
目前看，view和stream的表现一样，那么区别何在呢？观察下一个：
```
scala> (0 to 5).toStream.map((x:Int)=>{println(x);x*2})
0
res5: scala.collection.immutable.Stream[Int] = Stream(0, ?)

scala> (0 to 5).view.map((x:Int)=>{println(x);x*2})
res6: scala.collection.SeqView[Int,Seq[_]] = SeqViewM(...)
```
stream会优先求第一个值。

更多Stream的使用场景，[参见这篇](http://cuipengfei.me/blog/2014/10/23/scala-stream-application-scenario-and-how-its-implemented/)


----------


----------



### 7.3.4 Stack [不可变集合][线性(串行)][即时计算]

如果您想要实现一个**先进后出**的序列，那您可以使用Stack。
你可以使用push向栈中压入一个元素，用pop从栈中弹出一个元素，用top查看栈顶元素而不用删除它。
所有的这些操作都仅仅耗费固定的运行时间。

Stack的性能：
|  ____  |head|tail|apply|update|prepend|append|insert
|--------------|---------------------------------------|
|Stack|C	|C|	L|	L|	C|	C|	L|


这里提供几个简单的stack操作的例子：

1.创建栈Stack：
```
// 因为Stack在Scala里面不常用，并没有像List那样在scala这个对象（package object scala extends scala.AnyRef）里面有定义，所以需要显示引入包。
scala> import scala.collection.immutable.Stack
import scala.collection.immutable.Stack

// 创建Stack方式1
scala> val s1 = new Stack
s1: scala.collection.immutable.Stack[Nothing] = Stack()

// 创建Stack方式2
scala> val s1 = new Stack()
s1: scala.collection.immutable.Stack[Nothing] = Stack()

// 创建Stack方式3
scala> val s1 = Stack()
s1: scala.collection.immutable.Stack[Nothing] = Stack()

scala> val s1 = Stack // 这是个例外，没有创建成功
s1: scala.collection.immutable.Stack.type = scala.collection.immutable.Stack$@2de366bb

// 创建Stack方式4
scala> val s1 = Stack.empty
s1: scala.collection.immutable.Stack[Nothing] = Stack()
```


----------


2.往栈里面添加元素：
```
// 显示引入Stack
scala> import scala.collection.immutable.Stack
import scala.collection.immutable.Stack

// 创建一个空栈
scala> val s1 = Stack.empty
s1: scala.collection.immutable.Stack[Nothing] = Stack()

// 往栈里面添加一个元素1，生成一个新的Stack，里面含有元素1
scala> s1 push 1
res0: scala.collection.immutable.Stack[Int] = Stack(1)

// 原来的Stack不变，s1还是空栈。
scala> s1
res3: scala.collection.immutable.Stack[Nothing] = Stack()

// 往栈里添加多个元素，任然是生成新的Stack，原s1不变
scala> s1 push(1, 2, List(3, 4, 5): _*)
res1: scala.collection.immutable.Stack[Int] = Stack(5, 4, 3, 2, 1)

// 往栈里添加多个元素，任然是生成新的Stack，原s1不变
scala> s1 pushAll List(1, 2, 3, 4, 5)
res2: scala.collection.immutable.Stack[Int] = Stack(5, 4, 3, 2, 1)

// 左边是栈顶，右边还是栈底。栈顶元素5，栈底元素1
scala> val s2 = Stack(5, 4, 3, 2, 1)
s2: scala.collection.immutable.Stack[Int] = Stack(5, 4, 3, 2, 1)

// append方式（尾部追加）追加元素，时间复杂度为常数时间
scala> s2 :+ 0
res19: scala.collection.immutable.Stack[Int] = Stack(5, 4, 3, 2, 1, 0)

// prepend方式（头部追加）追加元素，时间复杂度为常数时间
scala> 6 +: s2
res20: scala.collection.immutable.Stack[Int] = Stack(6, 5, 4, 3, 2, 1)

```

----------

3.获得栈里面的元素：
```
scala> val s2 = Stack(5, 4, 3, 2, 1)
s2: scala.collection.immutable.Stack[Int] = Stack(5, 4, 3, 2, 1)

// 获得栈顶元素
scala> s2.top
res10: Int = 5

// 原栈不变
scala> s2
res11: scala.collection.immutable.Stack[Int] = Stack(5, 4, 3, 2, 1)

// 弹出栈顶元素，返回一个新生成的不包含弹出元素的栈
scala> s2.pop
res12: scala.collection.immutable.Stack[Int] = Stack(4, 3, 2, 1)
// 原栈不变
scala> s2
res13: scala.collection.immutable.Stack[Int] = Stack(5, 4, 3, 2, 1)

// 弹出栈顶元素，返回一个Tuple（栈顶元素, 一个新生成的不包含弹出元素的栈）
scala> s2.pop2
res14: (Int, scala.collection.immutable.Stack[Int]) = (5,Stack(4, 3, 2, 1))
// 原栈不变
scala> s2
res15: scala.collection.immutable.Stack[Int] = Stack(5, 4, 3, 2, 1)
```

>说明：
不可变stack一般很少用在Scala编程中，因为List结构已经能够覆盖到它的功能：push操作同List中的::基本相同，pop则对应着tail。


----------


----------



### 7.3.5 Queue [不可变集合][线性(串行)][即时计算]

Queue是一种与stack很相似的数据结构，除了与stack的**先进后出**不同，Queue结构的是**先进先出**的队列。

Queue的性能：
|  ____  |head|tail|apply|update|prepend|append|insert
|--------------|---------------------------------------|
|Queue|aC|	aC|	L|	L|	L|	C|	-|

1.创建队列Queue
```
scala> import scala.collection.immutable.Queue
import scala.collection.immutable.Queue

// 只有这两种方式创建一个队列
scala> val q1 = Queue()
q1: scala.collection.immutable.Queue[Nothing] = Queue()

scala> val q1 = Queue.empty
q1: scala.collection.immutable.Queue[Nothing] = Queue()

scala> val q1 = Queue(1, 2, 3)
q1: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3)

```


----------


2.往队列Queue添加元素
```
scala> q1
res1: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3)

// Creates a new queue with element added at the end of the old queue.
scala> q1 enqueue 4
res2: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3, 4)

// 原队列不变
scala> q1
res3: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3)

// Returns a new queue with all elements provided by an Iterable object added at the end of the queue.
scala> q1 enqueue List(4, 5, 6)
res4: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3, 4, 5, 6)

// 原队列不变
scala> q1
res5: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3)

// 连续追加元素
scala> q1 enqueue 4 enqueue List(5, 6, 7)
res8: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3, 4, 5, 6, 7)

// append的方式，从队尾追加元素， 常数时间
scala> q1 :+ 4
res6: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3, 4)

// prepend的方式，从对头追加元素， 线性时间
scala> 0 +: q1
res7: scala.collection.immutable.Queue[Int] = Queue(0, 1, 2, 3)

```


----------

3.从队列中获得元素
```
scala> res8
res20: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3, 4, 5, 6, 7)

// Returns a tuple with the first element in the queue, and a new queue with this element removed.
scala> res8.dequeue
res13: (Int, scala.collection.immutable.Queue[Int]) = (1,Queue(2, 3, 4, 5, 6, 7))

// 获得第一个元素
scala> res8.head
res14: Int = 1

scala> val q2 = Queue()
q2: scala.collection.immutable.Queue[Nothing] = Queue()

scala> q2.dequeue // 空队列报错
java.util.NoSuchElementException: dequeue on empty queue
        at scala.collection.immutable.Queue.dequeue(Queue.scala:117)
        at .<init>(<console>:10)
        at .<clinit>(<console>)
        at .<init>(<console>:7)
        at .<clinit>(<console>)
        ....
        
scala> q2.head // 空队列报错
java.util.NoSuchElementException: head on empty queue
        at scala.collection.immutable.Queue.head(Queue.scala:79)
        at .<init>(<console>:10)
        at .<clinit>(<console>)
        at .<init>(<console>:7)
        at .<clinit>(<console>)    
        ...
        
        
```




----------


----------


## 7.4 可变集合

可变集合是指在生命周期内，集合里面的元素是可改变的。
其中这些可改变指的是，可往集合里追加元素，可删除集合里面的元素，可以改变集合里面某个元素的值。

可变集合的继承树：

![](http://i.stack.imgur.com/Dsptl.png)

说明：
![](http://i.stack.imgur.com/szWUr.png)


----------


### 7.4.1 数组Array（长度固定的**线性**可变集合）

数组（Array）其实并不在scala.collection包里面，它属于scala包，直接对应于Java的数组，比如，Scala中的Array[Int]， 在底层，与Java的int[]一样。

根据可变集合的继承树，在Scala中Array属于可变集合。但是通过实践得知，**Array的特性是介于不可变集合和可变集合之间的**。
它的**不可变性**体现在，数组长度固定（定义完后，不能随意扩展），你对该数组Array进行元素的追加，删除操作时，返回一个新的数组。
其**可变性**又体现在，对数组元素的修改（update），是作用在该数组本身上，不会返回一个元素修改之后的新数组。

```
// 初始化一个数组
scala> val array = Array(1, 2, 3)
array: Array[Int] = Array(1, 2, 3)

// 追加一个元素，返回一个新数组，长度增加1，原数组array不变
scala> array :+ 5
res27: Array[Int] = Array(1, 2, 3, 5)

// 删除第一个元素，返回一个新数组，原数组Array不变
scala> array.tail
res28: Array[Int] = Array(2, 3)

// 对于上面返回的新数组，除了增加或删除的元素之外，别的所有元素都来自于原数组array的拷贝。这是一种Immutable的方式，并不高效。因为数据没有共享，而是复制。而Scala中List的不变性并不是Immutable的方式，而是通过Persistent的方式共享数据来到达了Immutable的效果。所有对于上面的情况（增加删除元素，返回一个新集合），推荐使用List。

// 修改第一个元素为4，作用的是该数组本身，不会返回新数组
scala> array.update(0, 4)

// 修改了数组本身
scala> array
res24: Array[Int] = Array(4, 2, 3)

```
综上所述，Scala中的Array的**使用场景**就是：
>创建一个固定长度的集合，一开始就知道集合里面要存放的所有元素，并且之后不会对集合进行元素的追加，删除，但是能够对集合里面存放的元素进行修改（update），并且可以通过下标访问集合中的元素。例如：
```
/**
   * 字符串逆序
   * 使用中间变量交换
   * 时间复杂度：O(n) 
   * 空间复杂度：O(1)
   * @param content
   * @return
   */
  def reverseByTempVariable(content: String): String = {
    // 创建一个固定长度的集合，一开始就知道集合里面的所有元素
    val target = content.toCharArray
    var i = 0
    var j = target.length - 1
    while (i < j) {
      // 只对集合进行了元素的修改操作，没有追加和删除操作。并且通过下标来访问元素
      val temp = target(j)
      target(j) = target(i)
      target(i) = temp
      i = i + 1
      j = j - 1
    }
    target.mkString("")
  }
```

下面继续探讨Array使用的细节。

Array的性能：
|  ____  |head|tail|apply|update|length|prepend|append|insert|
|--------------|---------------------------------------|
|Array|C|	L|	C|	C|C|	-|	-|	-|


1.初始化数组Array

```
//长度固定为10的整数数组，所有元素初始化为0
val nums = new Array[Int] (10) 

//长度固定为10的字符串数组，所有元素初始化为null
val a = new Array [String] (10) 

//长度固定为2的Array[String]，类型是推断出来的，已提供初始值就不需要new
val s = Array("Hello", "World") 

// 修改第一个元素（可变性的体现）
s(0) ="Goodbye" //Array（"Goodby "，"World"）,使用()而不是[]来访问元素
```
在JVM中，Scala的Array以Java数组方式实现。示例中的数组在JVM中的类型为java.lang.String[]。Int、Double或其他与Java中基本类型对应的数组都是基本类型数组。
举例来说，Array(2，3，5，7，11)在JVM中就是一个int[]。

----------

2.访问数组Array中的元素
数组是一种线性集合，可以通过下标（从0开始）来访问数组中的元素。
```
scala> val s = Array("Hello", "World")
s: Array[String] = Array(Hello, World)

// 下面是三种方式来访问数组的第一个元素，常数时间
scala> s.head
res29: String = Hello

scala> s(0)
res30: String = Hello

scala> s.apply(0)
res31: String = Hello

// 访问下标不存在的元素
scala> s(2)
java.lang.ArrayIndexOutOfBoundsException: 2
  ... 32 elided

```

----------

3.修改数组Array中的元素
```
// 直接对索引位置的元素进行修改，这里修改第一个元素
scala> s(0) = "Hi"

scala> s
res34: Array[String] = Array(Hi, World)
```

----------

4.遍历数组
```
// 全遍历
// 循环变量i先后取值0、1，等等，直到s.length -1
for (i <- 0 until s.length) // until不包含s.length
  println(i+"："+s(i))
  
// 如果想要每两个元素一跳，可以让i这样来进行遍历  
for (i <- 0 until (s.length, 2)) 
  println(i+"："+s(i))
```
**until 和 to**
```
0 until 5 // Range(0,1,2,3,4)
0 to 5    // Range(0,1,2,3,4,5)

// 指定步长
0 until (10, 2) // Range(0,2,4,6,8)
0 to (10, 2)    // Range(0,2,4,6,8,10)

```

如果在循环体中不需要用到数组下标，我们也可以直接访问数组元素，就像这样：
```
for (elem <- s)
  println (elem)
 
``` 

这和Java中的"增强版"for循环，或者C++中的"基于区间的"for循环很相似。变量elem先后被设为a(0)，然后a(1)，依此类推。


----------

5.数组转换

对数组以某种方式对它进行转换是很简单的。这些转换动作不会修改原始数组，而是产生一个全新的数组。像这样使用for推导式：
```
val a = Array(2, 3, 5, 7, 11)

//result是Array(4,6,10, 14, 22)
val resul = for (elem <- a) yield 2 * elem 
```
for(…)yield循环创建了一个类型与原始集合相同的新集合。

结果包含yield之后的表达式的值，每次迭代对应一个。通常，当你遍历一个集合时，你只想处理那些满足特定条件的元素。这个需求可以通过for中的if来实现。

在这里我们过滤掉奇数元素，对偶数元素翻倍：
```
for (elem <- a if elem % 2 == 0) yield 2 * elem
```
请留意结果是个新的集合，原始集合并没有受到影响。

除上述之外，还有另一种等价方法是：
```
a.filter (_%2==0).map(2*_)
```
或者
```
a.filter { _%2 == 0 } map {2*_ }
```

某些有着函数式编程经验的程序员倾向于使用filter和map而不是守卫和yield，这不过是一种风格罢了与for循环所做的事完全相同。你可以根据喜好任意选择。


----------


----------

### 7.4.2 数组ArrayBuffer（长度**不**固定的**线性**可变集合）

在Scala中如果要使用一个长度不固定的线性可变集合，那就是ArrayBuffer。 
ArrayBuffer属于scala.collection.mutable包，使用时需要先引入。

ArrayBuffer的性能：
|  ____  |head|tail|apply|update|prepend|append|insert|
|--------------|---------------------------------------|
|ArrayBuffer|C|	L|	C|	C|	L|	aC|	L|

通过ArrayBuffer，你可以很方便的添加或删除元素。
很多情况下，我们在需要构建一个数组，预先不知道数组长度，但是构建完毕后，使用时不再需要增加或删除元素。
这时候，我们一般使用ArrayBuffer来构建数组，构建完毕后，调用它的toArray方法，得到不可变（指长度固定）的Array供后续使用，以提高性能。

```
// 引入ArrayBuffer
scala> import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ArrayBuffer

// 初始化ArrayBuffer的三种方式
scala> val ab = new ArrayBuffer()
ab: scala.collection.mutable.ArrayBuffer[Nothing] = ArrayBuffer()

scala> val ab = ArrayBuffer()
ab: scala.collection.mutable.ArrayBuffer[Nothing] = ArrayBuffer()

scala> val ab = ArrayBuffer.empty
ab: scala.collection.mutable.ArrayBuffer[Nothing] = ArrayBuffer()

scala> val buffer = ArrayBuffer(1)
buffer: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(1)

// 从尾部追加一个或多个元素，常数时间
// Appends a single element to this buffer and returns the identity of the buffer. It takes constant amortized time.
scala> buffer += 2
res25: buffer.type = ArrayBuffer(1, 2)

scala> buffer
res26: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(1, 2)

// adds two or more elements to this growable collection.
scala> buffer += (3,4,5)
res27: buffer.type = ArrayBuffer(1, 2, 3, 4, 5)

// 从头部追加一个元素，线性时间
scala> buffer.prepend(0)

scala> buffer
res29: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(0, 1, 2, 3, 4, 5)

// Removes the last n elements of this buffer.
scala> buffer.trimEnd(2)

scala> buffer
res32: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(0, 1, 2, 3)

// 从尾部追加一个集合
scala> buffer ++= Array(7,8)
res33: buffer.type = ArrayBuffer(0, 1, 2, 3, 7, 8)

// def insert(n: Int, elems: A*): Unit // Inserts new elements at a given index into this buffer.
scala> buffer.insert(4,4,5,6)

scala> buffer
res35: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8)

scala> val a = buffer.toArray
a: Array[Int] = Array(0, 1, 2, 3, 4, 5, 6, 7, 8)

```
>注意：
向ArrayBuffer的**尾部**添加或删除元素的效率很高，但是在别的位置插入元素的效率就比较低下了，因为这涉及到将该位置之后的元素后移。 因此，在使用ArrayBuffer时，请尽量在尾部进行操作。



----------


----------


### 7.4.2 ListBuffer

List是一个不可变的单链表，在头部操作效率比较高。
如果你需要频繁的更改元素，而且在尾部的操作比较多，可以使用对应的可变单链表，ListBuffer。
在ListBuffer上append（+=）和prepend（+=:）操作都是常数时间O(1)。

>说明：
+=:是右结合操作符（所有以:结尾的操作符都是右结合的），因此，不能写作**buffer +=: 1**，如果想写成操作符形式，需要将buffer放在右边， 也就是**1 +=: buffer**。

在构建完毕之后，可以使用toList【Converts this buffer to a list. Takes constant time】得到不可变的List。

ListBuffer的性能：
|  ____  |head|tail|apply|update|prepend|append|insert|toList|
|--------------|---------------------------------------|
|ListBuffer|C|	L|	L|	L	|C	|C	|L| C|


----------


1.创建ListBuffer

```
// 使用ListBuffer必须从可变集合包中引入
scala> import scala.collection.mutable
import scala.collection.mutable

// 创建ListBuffer需要指定类型参数，否则往ListBuffer里面添加元素会报错：类型不匹配
scala> val lb = mutable.ListBuffer()
lb: scala.collection.mutable.ListBuffer[Nothing] = ListBuffer()

scala> lb.append(1)
<console>:10: error: type mismatch;
 found   : Int(1)
 required: Nothing
              lb.append(1)
                        ^

// 指定ListBuffer里面存放的元素类型为Int
scala> val lb2 = mutable.ListBuffer[Int]()
lb2: scala.collection.mutable.ListBuffer[Int] = ListBuffer()

scala> lb2.append(1)

scala> lb2
res2: scala.collection.mutable.ListBuffer[Int] = ListBuffer(1)

// 使用new 的方式创建，省略括号
scala> val lb3 = new mutable.ListBuffer[Int]
lb3: scala.collection.mutable.ListBuffer[Int] = ListBuffer()

scala> lb3.append(2)

scala> lb3
res4: scala.collection.mutable.ListBuffer[Int] = ListBuffer(2)

// 使用new 的方式创建，带括号
scala> val lb4 = new mutable.ListBuffer[Int]()
lb4: scala.collection.mutable.ListBuffer[Int] = ListBuffer()

scala> lb4.append(4)

scala> lb4
res8: scala.collection.mutable.ListBuffer[Int] = ListBuffer(4)

// 或者使用empty方法
scala> val lb5 = mutable.ListBuffer.empty[Int]
lb5: scala.collection.mutable.ListBuffer[Int] = ListBuffer()
```

----------

2.往ListBuffer里添加元素

```
// 从lb4的尾部追加元素5
scala> lb4.append(5) // 从尾部追加元素是常数时间

scala> lb4
res10: scala.collection.mutable.ListBuffer[Int] = ListBuffer(4, 5)

// 或者使用 += 方法从尾部追加元素，This operation takes constant time.
scala> lb4 += 6
res13: lb4.type = ListBuffer(4, 5, 6)

// 从lb4的头部追加元素3
scala> lb4.prepend(3) // 从头部追加元素也是常数时间

scala> lb4
res12: scala.collection.mutable.ListBuffer[Int] = ListBuffer(3, 4, 5, 6)

// 或者使用 +=: 方法从头部追加元素，This operation takes constant time.
scala> 2 +=: lb4
res15: lb4.type = ListBuffer(2, 3, 4, 5, 6)
```


----------

3.往ListBuffer里追加集合
```
// 通过 ++= 方法从尾部追加集合
scala> lb5 ++= List(1,2,3)
res16: lb5.type = ListBuffer(1, 2, 3)

scala> lb5 ++= List(4, 5, 6)
res17: lb5.type = ListBuffer(1, 2, 3, 4, 5, 6)

// 通过 ++=: 方法从头部追加集合
scala> List(-2, -1, 0) ++=: lb5
res18: lb5.type = ListBuffer(-2, -1, 0, 1, 2, 3, 4, 5, 6)

```

----------

4.把ListBuffer转换为List
```
// 通过toList方法，用ListBuffer里面的元素生成一个新的List集合
scala> lb5.toList
res20: List[Int] = List(-2, -1, 0, 1, 2, 3, 4, 5, 6)

// 而原来的ListBuffer不变。
scala> lb5
res21: scala.collection.mutable.ListBuffer[Int] = ListBuffer(-2, -1, 0, 1, 2, 3, 4, 5, 6)
```


----------


5.清空ListBuffer
```
scala> lb5.clear
res22: scala.collection.mutable.ListBuffer[Int] = ListBuffer()

```


----------


----------



## 7.5 集合中的常用循环操作

 
### 7.5.1 for

用for对常用集合进行迭代：

```
// 对Range的迭代
for (s <- ss) foo(s)

for (i <- 0 to n) foo(i) // 包含n，即Range(0,1,2,...,n)

for (i <- 0 until n) foo(i)  // 不包含n，即Range(0,1,2,3,...,n-1)

// for表达式无返回值的情况
scala> for(i<-1 to 10; j=i*i) println(j)
1
4
9
16
25
36
49
64
81
100

// 对List的迭代，for表达式通过yield返回值，类型跟被迭代的集合类型相同
for (n<-List(1,2,3,4) if n%2==1) yield n*n  // List(1, 9)

// 对Array的迭代
for (n <- Array(1,2,3,4) if n%2==1) yield n*n  // Array(1, 9)

// 命令式的方式使用for迭代
var s = 0; 
for (i <- 0 until 100) { 
  s += i 
} 
// s = 4950
 
// 等价于不用for的函数式写法：
List(1,2,3,4).filter(_%2==1).map(n => n*n)
 
// 如果for表达式是多行的，并且使用圆括号()时，生成器和定义之间的分号";"不能少。定义和过滤器之间的";"缺可以省略
def testFor() =
  for (p <- persons; //生成器
    n = p.name; //定义
    if (n startsWith "B") //过滤器
  )yield n


// 或者可以使用花括号{}，for表达式是多行时，推荐使用花括号方式
def testFor() =
  for {
    p <- persons //生成器
    n = p.name //定义
    if (n startsWith "B") //过滤器
  } yield n


// 边长21以内所有符合勾股弦的三角形：
def triangle(n: Int) = for {
  x <- 1 to 21
  y <- x to 21
  z <- y to 21
  if x * x + y * y == z * z
} yield (x, y, z)

// 结果：
 Vector((3,4,5), (5,12,13), (6,8,10), (8,15,17), (9,12,15), (12,16,20))

``` 
 
for .. yield的使用，该表达式有返回值：

把每次循环的结果“移”进一个集合（类型和循环内的一致）

`for {子句} yield {循环体}`
 
正确：
```
for (e<-List(1,2,3)) yield (e*e)   // List(1,4,9)
for {e<-List(1,2,3)} yield { e*e } // List(1,4,9)
for {e<-List(1,2,3)} yield e*e     // List(1,4,9)
```
错误：
```
for (e<-List(1,2,3)) { yield e*e } // 语法错误,yield不能在任何括号内
```
 

----------


 
### 7.5.2 foreach

```
List(1,2,3).foreach(println)
1
2
3
 
// 也可以写成：
(1 to 3).foreach(println)

//或者
(1 until 4) foreach println

//或者
Range(1,3) foreach println
 
// to和until的区别：
1 to 10：   [1, 10]，闭区间
1 until 10：[1, 10)，半开半闭区间，不包括10

都可以写步长，如：
1 to (11, 2) // 1,3,5,7,9,11 步长为2
// 或者
1 to 11 by 2

1 until (11, 2) // 1,3,5,7,9
1 until 11 by 2

scala> val r = (1 to 10 by 4)
r: scala.collection.immutable.Range = Range(1, 5, 9)

scala> r.end
res49: Int = 10

scala> r.last
res50: Int = 9

scala> r.start
res51: Int = 1

// 也可以是BigInt
scala> (1:BigInt) to 3
res56: scala.collection.immutable.NumericRange.Inclusive[BigInt] = NumericRange(1, 2, 3)

```


----------


### 7.5.3 forall

"所有都符合"——相当于 A1 && A2 && A3 && ... && Ai && ... && An

也就是集合中的`所有元素都必须满足`forall方法参数的predication，才返回true，否则返回false。

```
scala> (1 to 3) forall (_ > 0)
res12: Boolean = true

scala> (-1 to 3) forall (_ > 0)
res13: Boolean = false
```

判断一个数是否是质数(素数)。
```
// 从2到自己一半的数(n/2)都不能整除，则为质数
def isPrime(n: Int) = 2 until n/2 forall (n % _ != 0)

scala> for (i<-1 to 100 if isPrime(i)) println(i)

scala> (2 to 10) partition (isPrime _)
res18: (scala.collection.immutable.IndexedSeq[Int], scala.collection.immutable.IndexedSeq[Int]) = (
Vector(2, 3, 4, 5, 7),Vector(6, 8, 9, 10)
)
```
也可直接调用BigInt的内部方法：
```
// 把普通的Int转换为BigInt后，就可以调用BigInt中的方法
(2 to 10) partition (BigInt(_).isProbablePrime(10))

// 注：isProbablePrime(c)中c越大，是质数的概率越高，
// 10对应概率：1 - 1/(2**10) = 0.999
```


----------


### 7.5.4 reduceLeft和reduceRight

reduceLeft and reduceRight cumulate a single result.

reduceLeft方法声明：
```
def reduceLeft [B  > : A] (f: (B, A) = >  B): B

```
通过声明我们可以看出，返回类型B必须是A或A的父类型。

reduceLeft 方法首先应用于前两个元素，然后再应用于第一次应用的结果和接下去的一个元素，等等，直至整个列表。把整个列表最后变成一个元素。

例如，计算阶乘：
```
scala> def fac(n: Int) = 1 to n reduceLeft(_ * _)
fac: (n: Int)Int

scala> fac(5) // 相当于：((((1*2)*3)*4)*5)
res20: Int = 120
```

计算sum：
```
scala> List(2,4,6).reduceLeft(_ + _) // 相当于：((2+4)+6)
res21: Int = 12
```

取max：
```
scala> List(1,4,9,6,7).reduceLeft((x,y)=> if (x>y) x else y)
res22: Int = 9 
     
// 或者简化为：
scala> List(1,4,9,6,7).reduceLeft(_ max _)
res23: Int = 9
// 相当于：((((1 max 4) max 9) max 6) max 7)
```    
 
reduceRight 方法首先应用于最后两个元素，然后再应用于第一次应用的结果和接下去的倒数第三个元素，等等，直至整个列表。
```
scala> List(1,4,9,6,7).reduceRight(_ max _)
res23: Int = 9
// 相当于：((((1 max (4 max (9 max (6 max 7))))

```

>注意：
如果一个空集合来调用reduceLeft/reduceRight方法，将会抛出异常。

----------


   
### 7.5.5 foldLeft和foldRight

foldLeft and foldRight cumulate a single result using a start value.

foldLeft的声明为：
```
def foldLeft [B] (z: B)(f: (B, A) = >  B): B
```
这里B与A的类型并没有任何关系，所以比reduceLeft使用更广。
假设集合里有a,b,c 三个元素，实现上等同于 op(op(op(z,a),b),c)。
 
使用例子：累加或累乘
```
def sum(L: List[Int]): Int = {
  var result = 0
  for (item <- L) result += item
  result
}
```
更scalable的写法：
```
def sum(L: Seq[Int]) = L.foldLeft(0)((a, b) => a + b)

def sum(L: Seq[Int]) = L.foldLeft(0)(_ + _)

def sum(L: Seq[Int]) = (0 /: L){_ + _}

scala> sum(List(1,3,5,7))
res0: Int = 16
```

乘法：
```
def multiply(L: Seq[Int]) = L.foldLeft(1)(_ * _)

multiply(Seq(1,2,3,4,5)) // 120

multiply(1 until 5+1) // 120

```

foldLeft和foldRight对应的操作符：

```
// foldLeft
(z /: List(a, b, c))(op)  // 相当于 op(op(op(z, a), b), c)

// foldRight
(List(a, b, c) :\ z)(op)  // 相当于 op(a, op(b, op(c, z)))
```

>reduceLeft和foldLeft的区别请见[这里](http://www.helplib.com/qa/418716)。
 
对于foldLeft和foldRight使用时的选择，优先选择foldLeft。
因为foldLeft是尾递归实现的，效率更高。
而foldRight是普通递归，容易内存溢出！


----------


### 7.5.6 scanLeft和scanRight 

scanLeft and scanRight cumulate a collection of intermediate cumulative results using a start value.

```
List(1,2,3,4,5).scanLeft(0)(_ + _) // (0,1,3,6,10,15)

// 相当于：
(0,(0+1),(0+1+2),(0+1+2+3),(0+1+2+3+4),(0+1+2+3+4+5))

List(1,2,3,4,5).scanLeft(1)(_ * _) // (1,2,6,24,120)

// 相当于
(1, 1*1, 1*1*2, 1*1*2*3, 1*1*2*3*4, 1*1*2*3*4*5)
```

>When should I use reduceLeft, reduceRight, foldLeft, foldRight, scanLeft or scanRight?
具体见这里的[解释](http://stackoverflow.com/questions/17408880/reduce-fold-or-scan-left-right)。
 


----------


### 7.5.7 take drop splitAt

```
1 to 10 by 2 take 3    // Range(1, 3, 5)
1 to 10 by 2 drop 3    // Range(7, 9)
1 to 10 by 2 splitAt 2 // (Range(1, 3),Range(5, 7, 9))
```

例子：前10个质数
```
scala> def isPrime(n:Int) = (((2 to math.sqrt(n).toInt) forall (i=> n%i != 0)))
prime: (n: Int)Boolean

scala> 2 to 100 filter isPrime take 10
res4: scala.collection.immutable.IndexedSeq[Int] = Vector(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)

scala> isPrime(19)
res2: Boolean = true

scala> isPrime(20)
res3: Boolean = false
```


----------


### 7.5.8 takeWhile和dropWhile, span和partition

while语句的缩写， takeWhile (...)
等价于：while (...) { take }

``` 
// takeWhile表示从集合的第一个元素开始，满足条件的元素就取出，直到第一个不满足条件时，就终止。然后得到满足条件的元素组成的集合。

// 1 到 4 都满足小于5，到5时，条件不满足终止。
scala> 1 to 10 takeWhile (_ < 5)
res5: scala.collection.immutable.Range = Range(1, 2, 3, 4)

// 第一个元素1就不满足条件，终止。所以返回空集合Range()
scala> 1 to 10 takeWhile (_ > 5)
res6: scala.collection.immutable.Range = Range()

// 从10到1中取出满足大于6的元素。
scala> 10 to (1, -1) takeWhile(_ > 6)
res7: scala.collection.immutable.Range = Range(10, 9, 8, 7)

scala> 1 to 10 takeWhile (n=>n * n < 25)
res8: scala.collection.immutable.Range = Range(1, 2, 3, 4)
```


----------


dropWhile (...)
等价于：while (...) { drop }

```
// dropWhile表示从集合的第一个元素开始，满足条件的元素就排除，直到第一个不满足条件时，就终止。然后从不满足条件的元素开始，及剩余的所有元素组成的集合被返回。

// 1到4都满足小于5，到5时，条件不满足，终止。然后排除满足条件的元素。剩余的元素组成集合返回。
scala> 1 to 10 dropWhile (_ < 5)
res9: scala.collection.immutable.Range = Range(5, 6, 7, 8, 9, 10)

// 第一个元素6不满足小于5，终止。从不满足条件的元素开始，及剩余的所有元素组成的集合被返回。
scala> List(6,2,3,4). dropWhile (_<5)
res10: List[Int] = List(6, 2, 3, 4)

```
 
----------


span (...)
等价于：while (...) { take; drop }

```
// span表示从集合的第一个元素开始，满足条件的元素组成一个集合，直到元素不满足条件时终止。然后从不满足条件的元素开始及剩余的所有元素组成一个新的集合。最后返回一个Tuple: (满足条件的元素集合，不满足条件的元素集合)

// 所有小于5的元素组成一个集合，大于等于5的元素组成一个集合
scala> 1 to 10 span (_ < 5)
res11: (scala.collection.immutable.Range, scala.collection.immutable.Range) = (
Range(1, 2, 3, 4),Range(5, 6, 7, 8, 9, 10))

// 第一个元素就不满足条件，所以返回一个空集合，然后剩余的元素组成一个集合
scala> List(6,2,3,4).span (_ < 5)
res12: (List[Int], List[Int]) = (List(),List(6, 2, 3, 4))
```

span和partition的区别：

```
// 满足条件的组成一个集合，有一个元素条件不满足就终止，然后从不满足条件的元素及剩余的所有元素组成另一个集合。
scala> List(1,0,1,0) span (_>0)
res13: (List[Int], List[Int]) = (List(1),List(0, 1, 0))

// 遍历整个集合，把满足条件的元素分为一组，不满足条件的为另一组
scala> List(1,0,1,0) partition (_>0)
res14: (List[Int], List[Int]) = (List(1, 1),List(0, 0))
```
>注意，partition是和span完全不同的操作



----------


----------




References:

[1].https://twitter.github.io/effectivescala/index-cn.html
[2].http://stackoverflow.com/questions/1722137/scala-2-8-collections-design-tutorial
[3].http://blog.javachen.com/2015/04/22/scala-collections.html
[4].http://yjplxq.blog.51cto.com/4081353/1427927
[5].http://www.cnblogs.com/molyeo/p/4720855.html
[6].http://qiujj.com/static/Scala-Handbook.htm#_Toc306132178
[7]. 【Scala for the impatient chapter 13】