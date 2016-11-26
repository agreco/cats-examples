package uk.carwynellis.cats.typeclasses.functors

import cats._
import cats.implicits._
import cats.Semigroup
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, OneAnd, Validated, ValidatedNel}

import scala.concurrent.Future

/**
  * Traverse extends the Functor typeclass with a new function, traverse.
  *
  * Broadly speaking, traverse applies a function, f: A => G[B] to some functor
  * F[A] accumulating the result in functor G[B].
  *
  * See http://typelevel.org/cats/typeclasses/traverse.html
  */

object TraverseExample extends App {

  // In functional programming we typically eoncode effects as data types, for
  // example Option encodes a value that may not be present.

  // These effects tend to appear in functions that operate on a single
  // argument, for example parsing a String into an Int. Consider the following
  // examples.

  // Parsing a string may or may not succeed and result in Some value.
  def parseInt(s: String): Option[Int] = ???

  // Validating a login will Either succeed or fail with some error.
  trait Credentials
  trait AuthenticationError

  def validateCredentials(c: Credentials): Either[AuthenticationError, Unit] = ???

  // Retrieving a user profile will eventually return some data.
  trait Profile
  trait User

  def getUserProfile(u: User): Future[Profile] = ???

  // Now we have a simple function for fetching a user profile, it can be used
  // to fetch profiles for a list of users via map.
  def getUserProfiles(users: List[User]): List[Future[Profile]] = users map { getUserProfile }

  // However the result is a List of Future which is awkward to work with.
  // Future provides a traverse method which takes a List[Future[_]] and
  // returns a Future[List[_]] but this is a very specific implementation.

  // We can apply this idea more generally using the traverse method from the
  // Traverse type class.

  // Thus we could convert a List[String] or validate credentials for a List
  // of users.

  // The type signature of traverse is very abstract. What traverse does as it
  // walks the F[A] depends on the function being applied.

  // Some examples, using the traverseU which provides some type-level trickery
  // to help scalac infer the correct types for data types that do not easily
  // satisfy the F[_] shape required by the applicative.
  // See http://typelevel.org/blog/2013/09/11/using-scalaz-Unapply.html

  def parseIntEither(s: String): Either[NumberFormatException, Int] =
    // catchOnly is provided by cats implicits and is implemented in
    // EitherObjectOps.
    Either.catchOnly[NumberFormatException](s.toInt)

  def parseIntValidated(s: String): ValidatedNel[NumberFormatException, Int] =
    Validated.catchOnly[NumberFormatException](s.toInt).toValidatedNel

  assert(List("1", "2", "3").traverseU(parseIntEither) == Right(List(1, 2, 3)))

  // The entire traversal is failed at the point the exception is thrown...
  List("1", "foo!", "3").traverseU(parseIntEither) match {
    case Left(e) =>
      assert(e.getMessage == "For input string: \"foo!\"")
    case _ => ???
  }

  // ...which is highlighted in the following exception where the exception
  // string still refers to the first failure.
  List("1", "foo!", "bar").traverseU(parseIntEither) match {
    case Left(e) =>
      assert(e.getMessage == "For input string: \"foo!\"")
    case _ => ???
  }

  // For the Validated type traversal will continue on error, with any errors
  // accumulated in the result.
  assert(List("1", "2", "3").traverseU(parseIntValidated) == Valid(List(1, 2, 3)))

  // The following generates a single error caused by second value.
  List("1", "foo!", "3").traverseU(parseIntValidated) match {
    case Invalid(l) => assert(l.size == 1)
    case _ => ???
  }

  // The following generates two errors caused by the second and third values.
  // The following generates a single error caused by second value.
  List("1", "foo!", "bar").traverseU(parseIntValidated) match {
    case Invalid(l) => assert(l.size == 2)
    case _ => ???
  }

  // In both cases the behaviour of the traversal is closely related to the
  // Applicative behaviour of type.




}
