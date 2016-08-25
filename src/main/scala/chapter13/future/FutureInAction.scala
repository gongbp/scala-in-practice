package chapter13.future

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Random, Success}

/**
 * 从基础用法到高级实战,全面总结Future的各种使用场景.
 * To sum up how to use Future correctly in all kinds of scenes from basic to senior.
 * 基本的な使用方法から高級まで、各種使用場面をまとめる。
 *
 * author: gong_baiping
 * date: 8/24/16 15:14
 * version: 0.1 (Scala 2.11.7, Play 2.5, SBT 0.13.11)
 * copyright: CyberAgent, Inc.
 */
trait FutureInAction {

  // To use a domain class instead of Long number
  case class RandomNumber(number: Long)

  /**
   * the common measure timings method for speed
   */
  def timer[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  /**
   * To generate RandomNumber, the seq length is size number.
   * The RandomNumber's value is between 0 and 100
   * Non-Referential Transparent 非引用透明
   *
   * @param size, RandomNumber list length
   * @return Future[Seq[RandomNumber]]
   */
  def apiSimulator(size: Int): Future[Seq[RandomNumber]] = Future {
    println(s"-- This is the api simulator method, parameter's value: size = $size --")
    Thread.sleep(2000) // this method need to 1 minute to calculate
    Seq.fill(size)(Random.nextInt(100)).map(RandomNumber(_))
  }

}


/**
 * 每个人都会这么使用的例子
 */
object TheMostCommonUseCase extends FutureInAction {

  /**
   * Blocking Style
   * @return
   */
  def sumRandomNumbersByTowCallsInBlockingStyle: Long = {
    val firstCall: Seq[RandomNumber] = Await.result(apiSimulator(50), 10 seconds)
    val secondCall: Seq[RandomNumber] = Await.result(apiSimulator(100), 20 seconds)
    firstCall.map(_.number).sum + secondCall.map(_.number).sum
  }

  /**
   * Blocking Style 2
   * @return
   */
  def sumRandomNumbersByTowCallsInBlockingStyle2: Long = {
    val sum: Future[Long] = for {
      firstCall <- apiSimulator(50)
      secondCall <- apiSimulator(100)
    } yield firstCall.map(_.number).sum + secondCall.map(_.number).sum

    Await.result(sum, 20 seconds)
  }

  /**
   * Blocking Style 3 [recommend]
   * 阻塞情况下, 推荐使用方式
   * ブロッキングの場合、オススメです。
   *
   * @return
   */
  def sumRandomNumbersByTowCallsInBlockingStyle3: Long = {
    val sum = async {
      await(apiSimulator(50)).map(_.number).sum + await(apiSimulator(100)).map(_.number).sum
    }
    Await.result(sum, 10 seconds)
  }

  // -------------------------------------------------------------------------------------------------------------------

  /**
   * Non-Blocking Style: faster
   * @return
   */
  def sumRandomNumbersByTowCallsInNonBlockingStyle: Long = {
    val firstCall: Future[Seq[RandomNumber]] = apiSimulator(50)
    val secondCall: Future[Seq[RandomNumber]] = apiSimulator(100)

    Await.result(firstCall, 10 seconds).map(_.number).sum +
      Await.result(secondCall, 20 seconds).map(_.number).sum
  }

  /**
   * Non-Blocking Style 2: fast
   * @return
   */
  def sumRandomNumbersByTowCallsInNonBlockingStyle2: Long = {
    val firstCall: Future[Seq[RandomNumber]] = apiSimulator(50)
    val secondCall: Future[Seq[RandomNumber]] = apiSimulator(100)

    val sum: Future[Long] = for {
      first <- firstCall
      second <- secondCall
    } yield first.map(_.number).sum + second.map(_.number).sum

    Await.result(sum, 10 seconds)
  }

  /**
   * Non-Blocking Style 3 [fastest] [recommend]
   * 非阻塞的情况,推荐使用方式
   * 非ブロッキングの場合、オススメです。
   * @return
   */
  def sumRandomNumbersByTowCallsInNonBlockingStyle3: Long = {
    val sum = async {
      val firstCall: Future[Seq[RandomNumber]] = apiSimulator(50)
      val secondCall: Future[Seq[RandomNumber]] = apiSimulator(100)
      await(firstCall).map(_.number).sum + await(secondCall).map(_.number).sum
    }
    Await.result(sum, 10 seconds)
  }


}


/**
 * 测试 async 模块中发生普通异常和致命异常的区别
 */
object AsyncTester extends App {

  // 普通的异常
  def testException: Future[Nothing] = async {
    throw new RuntimeException("intended exception!")
  }

  // 普通的异常可以捕获
  testException.onComplete {
    case Success(s) => println("Success!")
    case Failure(e) => println(e.getMessage)
  }

  // 致命异常
  def testException2: Future[Nothing] = async {
    throw new InterruptedException
  }

  // 致命异常不能捕获
  testException2.onComplete {
    case Success(s) => println("Success!")
    case Failure(e) => println(e.getMessage)
  }

  println("The main thread!")
  Thread.sleep(5000)
}
