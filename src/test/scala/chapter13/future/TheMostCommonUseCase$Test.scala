package chapter13.future

import org.scalatest.FunSuite
import TheMostCommonUseCase._

/**
 *
 * author: gong_baiping
 * date: 8/25/16 17:13
 * version: 0.1 (Scala 2.11.7, Play 2.5, SBT 0.13.11)
 * copyright: CyberAgent, Inc.
 */
class TheMostCommonUseCase$Test extends FunSuite {

  test("testSumRandomNumbersByTowCallsInBlockingStyle") {
    println(s"sumRandomNumbersByTowCallsInBlockingStyle:")
    println(s"---------------------- begin -----------------------")
    val sum = timer {
      TheMostCommonUseCase.sumRandomNumbersByTowCallsInBlockingStyle
    }
    println(s"sum = $sum")
    println(s"---------------------- end -----------------------")
  }

  test("testSumRandomNumbersByTowCallsInBlockingStyle2") {
    println(s"sumRandomNumbersByTowCallsInBlockingStyle2:")
    println(s"---------------------- begin -----------------------")
    val sum = timer {
      TheMostCommonUseCase.sumRandomNumbersByTowCallsInBlockingStyle2
    }
    println(s"sum = $sum")
    println(s"---------------------- end -----------------------")
  }

  test("testSumRandomNumbersByTowCallsInBlockingStyle3") {
    println(s"sumRandomNumbersByTowCallsInBlockingStyle3:")
    println(s"---------------------- begin -----------------------")
    val sum = timer {
      TheMostCommonUseCase.sumRandomNumbersByTowCallsInBlockingStyle3
    }
    println(s"sum = $sum")
    println(s"---------------------- end -----------------------")
  }

  // -------------------------------------------------------------------------------------------------------------------

  test("testSumRandomNumbersByTowCallsInNonBlockingStyle") {
    println(s"sumRandomNumbersByTowCallsInNonBlockingStyle:")
    println(s"---------------------- begin -----------------------")
    val sum = timer {
      TheMostCommonUseCase.sumRandomNumbersByTowCallsInNonBlockingStyle
    }
    println(s"sum = $sum")
    println(s"---------------------- end -----------------------")
  }

  test("testSumRandomNumbersByTowCallsInNonBlockingStyle2") {
    println(s"sumRandomNumbersByTowCallsInNonBlockingStyle2:")
    println(s"---------------------- begin -----------------------")
    val sum = timer {
      TheMostCommonUseCase.sumRandomNumbersByTowCallsInNonBlockingStyle2
    }
    println(s"sum = $sum")
    println(s"---------------------- end -----------------------")
  }

  test("testSumRandomNumbersByTowCallsInNonBlockingStyle3") {
    println(s"sumRandomNumbersByTowCallsInNonBlockingStyle3:")
    println(s"---------------------- begin -----------------------")
    val sum = timer {
      TheMostCommonUseCase.sumRandomNumbersByTowCallsInNonBlockingStyle3
    }
    println(s"sum = $sum")
    println(s"---------------------- end -----------------------")
  }


}
