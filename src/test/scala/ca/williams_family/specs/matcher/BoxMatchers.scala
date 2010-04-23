package ca.williams_family.specs.matcher

import org.specs.matcher.Matcher
import net.liftweb.common._

trait BoxMatchers {
  def beFull[T] = new BoxMatcher[T] {
    def fullApply(v: => Box[T]) = {
      val value = v
      (value match {
        case Full(x) => true
        case _ => false
      },
       d(value) + " is Full(x)",
       d(value) + " is not Full(x)")
    }
  }

  abstract class BoxMatcher[T] extends Matcher[Box[T]] {
    private var whichFunction: Box[T => Boolean] = Empty
    def fullApply(value: => Box[T]): (Boolean, String, String)

    def which(g: T => Boolean) = {
      whichFunction = Full(g)
      this
    }
    override def apply(a: => Box[T]) =
      if (whichFunction == Full(null))
        (false, "the 'which' property is a not a null function", "the 'which' property is a null function")
      else
        whichFunction match {
          case Empty => fullApply(a)
          case Failure(_,_,_) => fullApply(a)
          case Full(g) => (
            a match {
              case Full(x) => g(x)
              case _ => false
            },
            description.getOrElse("there") + " is a Full(x) verifying the given property",
            description.getOrElse("there") + " is no Full(x) verifying the given property")
        }
  }
}
