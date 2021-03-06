/*
 *  scala-exercises - exercises-cats
 *  Copyright (C) 2015-2020 47 Degrees, LLC. <http://www.47deg.com>
 *
 */

package catslib

import cats._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Eval is a data type for controlling synchronous evaluation.
 * Its implementation is designed to provide stack-safety at all times using a technique called trampolining.
 * There are two different factors that play into evaluation: memoization and laziness.
 * Memoized evaluation evaluates an expression only once and then remembers (memoizes) that value.
 * Lazy evaluation refers to when the expression is evaluated.
 * We talk about eager evaluation if the expression is immediately evaluated when defined and about lazy evaluation if the expression is evaluated when it’s first used.
 * For example, in Scala, a lazy val is both lazy and memoized, a method definition def is lazy, but not memoized, since the body will be evaluated on every call.
 * A normal val evaluates eagerly and also memoizes the result.
 * Eval is able to express all of these evaluation strategies and allows us to chain computations using its Monad instance.
 *
 * @param name Eval
 */
object EvalSection extends AnyFlatSpec with Matchers with org.scalaexercises.definitions.Section {

  /** = Eval.now =
   *
   * First of the strategies is eager evaluation, we can construct an Eval eagerly using Eval.now:
   *
   * {{{
   * import cats.Eval
   * // import cats.Eval
   *
   * import cats.implicits._
   * // import cats.implicits._
   *
   * val eager = Eval.now {
   *   println("Running expensive calculation...")
   *   1 + 2 * 3
   * }
   * // Running expensive calculation...
   * // eager: cats.Eval[Int] = Now(7)
   * }}}
   *
   * We can run the computation using the given evaluation strategy anytime by using the value method.
   * eager.value
   * // res0: Int = 7
   *
   */
  def nowEval(resultList: List[Int]) = {
    //given
    val eagerEval = Eval.now {
      println("This is eagerly evaluated")
      1 :: 2 :: 3 :: Nil
    }

    //when/then
    eagerEval.value shouldBe (resultList: List[Int])
  }

  /** = Eval.later =
   *
   * If we want lazy evaluation, we can use Eval.later
   * In this case
   *
   * {{{
   * val lazyEval = Eval.later {
   *   println("Running expensive calculation...")
   *   1 + 2 * 3
   * }
   * // lazyEval: cats.Eval[Int] = cats.Later@6c2b03e9
   *
   * lazyEval.value
   * // Running expensive calculation...
   * // res1: Int = 7
   *
   * lazyEval.value
   * // res2: Int = 7
   * }}}
   *
   * Notice that “Running expensive calculation” is printed only once, since the value was memoized internally.
   * Meaning also that the resulted operation was only computed once.
   * Eval.later is different to using a lazy val in a few different ways.
   * First, it allows the runtime to perform garbage collection of the thunk after evaluation, leading to more memory being freed earlier.
   * Secondly, when lazy vals are evaluated, in order to preserve thread-safety, the Scala compiler will lock the whole surrounding class, whereas Eval will only lock itself.
   *
   */
  def laterEval(resultList: List[Int], counterResult: Int) = {
    //given
    val n = 2
    var counter = 0
    val lazyEval = Eval.later {
      println("This is lazyly evaluated with caching")
      counter = counter + 1
      (1 to n)
    }

    //when/then
    List.fill(n)("").foreach(_ => lazyEval.value)
    lazyEval.value shouldBe (resultList: List[Int])
    counter shouldBe counterResult
  }

  /** = Eval.always =
   *
   * If we want lazy evaluation, but without memoization akin to Function0, we can use Eval.always
   * Here we can see, that the expression is evaluated every time we call .value.
   * {{{
   * val alwaysEval = Eval.always(println("Always evaluated"))
   * //Always evaluated
   * alwaysEval.eval
   * //Always evaluated
   * alwaysEval.eval
   * //Always evaluated
   * alwaysEval.eval
   * }}}
   *
   */
  def alwaysEval(resultList: List[Int], counterAfterListEval: Int, latestCounter: Int) = {
    //given
    val n = 4
    var counter = 0
    val alwaysEval = Eval.always {
      println("This is lazyly evaluated without caching")
      counter = counter + 1
      (1 to n)
    }

    //when/then
    List.fill(n)("").foreach(_ => alwaysEval.value)
    counter shouldBe counterAfterListEval
    alwaysEval.value shouldBe (resultList: List[Int])
    counter shouldBe latestCounter
  }

  /** = Eval.defer =
   *
   * Defer a computation which produces an Eval[A] value
   * This is useful when you want to delay execution of an expression which produces an Eval[A] value. Like .flatMap, it is stack-safe.
   * Because Eval guarantees stack-safety, we can chain a lot of computations together using flatMap without fear of blowing up the stack.
   *
   */
  def deferEval(resultList: List[Int]) = {
    //given
    val list = List.fill(3)(0)

    //when
    val deferedEval: Eval[List[Int]] = Eval.now(list).flatMap(e => Eval.defer(Eval.later(e)))

    //then
    Eval.defer(deferedEval).value shouldBe (resultList: List[Int])
  }

}
