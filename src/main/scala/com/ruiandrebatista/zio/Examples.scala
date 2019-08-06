package com.ruiandrebatista.zio

import scala.Console
import zio._
import zio.syntax._

import java.io.{IOException, EOFException}
import scala.io.StdIn

import java.nio.ByteBuffer

import java.nio.channels.{AsynchronousByteChannel, CompletionHandler}

object Examples {
  val programThatReturns1 = UIO.succeed(1)
  val programThatFails = ZIO.fail("Error!")

// Using syntax
  val anotherProgram = 1.succeed

  def putStrLn(str: String): UIO[Unit] =
    UIO.effectTotal(Console.println(str))

  def readPrompt(prompt: String): IO[IOException, String] =
    IO.effect {
        val line = StdIn.readLine(prompt)
        if (line == null) {
          throw new EOFException("There is no more input left to read")
        } else line
      }
      .refineToOrDie[IOException]

  val printName: IO[IOException, Unit] = for {
    name <- readPrompt("What is your name")
    _ <- putStrLn(s"Hi $name!")
  } yield ()

  def asyncRead(chanel: AsynchronousByteChannel)(n: Int): Task[Array[Byte]] =
    Task.effectAsyncM { register =>
      Task.effect {
        val buffer = ByteBuffer.allocate(n)
        chanel.read(
          buffer,
          (),
          new CompletionHandler[Integer, Unit] {
            override def completed(read: Integer, a: Unit) =
              register(UIO.succeed(buffer.array()))
            override def failed(ex: Throwable, a: Unit) =
              register(Task.fail(ex))
          }
        )
      }
    }

  def mixedProgram(chan: AsynchronousByteChannel) =
    for {
      bytes <- asyncRead(chan)(1024)
      _ <- putStrLn(new String(bytes))
    } yield ()

  programThatReturns1 zipPar programThatFails

  def myZipPar[R, E, A, B](
      program1: ZIO[R, E, A],
      program2: ZIO[R, E, B]
  ): ZIO[R, E, (A, B)] =
    for {
      fiber1 <- program1.fork
      fiber2 <- program2.fork
      r1 <- fiber1.join
      r2 <- fiber2.join
    } yield (r1, r2)
  val program = for {
    ref <- Ref.make(0)
    _ <- ref.update(_ + 2)
    result <- ref.get
  } yield result
  lazy val doComputation: UIO[Int] = 1.succeed



for {

    p <- Promise.make[Nothing, Int]
    _ <- doComputation.tap(p.succeeded _).fork

  } yield p

ZIOApp
}
