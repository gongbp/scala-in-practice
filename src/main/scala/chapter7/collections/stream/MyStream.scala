package chapter7.collections.stream

import scala.util.Random

/**
 * 实现一个Stream的集合.
 * {{{
 *   reference site:
 *   http://cuipengfei.me/blog/2014/10/23/scala-stream-application-scenario-and-how-its-implemented/
 * }}}
 *
 * author: gong_baiping
 * date: 15/09/29 10:52
 * version: 0.1 (Scala 2.11.6, Play 2.4.2)
 * copyright: TonyGong, Inc.
 */
trait MyStream[+A] {

  import MyStream._

  def filter(p: A => Boolean): MyStream[A] = {
    this match {
      case Cons(h, t) =>
        if (p(h())) cons(h(), t().filter(p))
        else t().filter(p)
      case Empty => empty
    }
  }

  def take(n: Int): MyStream[A] = {
    if (n > 0) this match {
      case Cons(h, t) if n == 1 => cons(h(), MyStream.empty)
      case Cons(h, t) => cons(h(), t().take(n - 1))
      case _ => MyStream.empty
    }
    else MyStream()
  }

  def toList: List[A] = {
    this match {
      case Cons(h, t) => h() :: t().toList
      case Empty => Nil
    }
  }

}

case object Empty extends MyStream[Nothing]

case class Cons[+A](h: () => A, t: () => MyStream[A]) extends MyStream[A]

object MyStream {

  def apply[A](elems: A*): MyStream[A] = {
    if (elems.isEmpty) empty
    else cons(elems.head, apply(elems.tail: _*))
  }

  def cons[A](hd: => A, tl: => MyStream[A]): MyStream[A] = {
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)
  }

  def empty[A]: MyStream[A] = Empty
}

object MyStreamTest extends App {
  def randomList = (1 to 50).map(_ => Random.nextInt(100)).toList

  def isDivisibleBy3(n: Int) = {
    val isDivisible = n % 3 == 0
    println(s"$n $isDivisible")
    isDivisible
  }

  MyStream(randomList: _*).filter(isDivisibleBy3).take(2).toList
}