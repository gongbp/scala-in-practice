package chapter1

import java.io.File

/**
 * Created by A12975 on 11/10/15.
 */
class Rational(val n: Int, val d: Int) {
  require(d != 0)
  override def toString = s"$n / $d"
  def add(that: Rational): Rational = new Rational(n * that.d + that.n * d, d * that.d)

  def gcd(a: Int, b: Int): Int = if(b ==0) a else gcd(b, a % b)


}
