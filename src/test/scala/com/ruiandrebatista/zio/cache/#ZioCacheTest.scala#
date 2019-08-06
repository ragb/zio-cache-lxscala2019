package com.ruiandrebatista.zio.cache

import org.specs2._

import zio._

import zio.clock.Clock
import zio.duration._

class ZioCacheTest extends mutable.Specification {

  val runtime = new DefaultRuntime {}

  "ZioCache" should {
    "load values" in {
      val program: TaskR[Clock, Int] = for {
        clock <- ZIO.environment[Clock]
        delay = UIO.succeedLazy(1).delay(1 second).provide(clock)
        value <- ZioLoadCache[String, Any, Nothing, Int](
          (_: String) => Task.succeed(1)
        ).use { cache =>
          cache.get("key")
        }
      } yield value

      runtime.unsafeRun(program) === 1
    }

    "Execute only once" in {
      val program: TaskR[Clock, Int] = for {
        ref <- Ref.make(0)
        delay = ref.update(_ + 1)

        value <- ZioLoadCache[String, Any, Throwable, Int](_ => delay).use {
          cache =>
            ZIO.collectAllPar((1 to 1000).map(_ => cache.get("key"))) *>
              ref.get
        }
      } yield value

      runtime.unsafeRun(program) === 1
    }

    "Interrupt load operation when removing" in {

      val program: ZIO[Clock, Nothing, Boolean] =
        ZioLoadCache[String, Clock, Nothing, Int](
          _ => ZIO.never)
            .use { cache =>
          val key = "hello"
          (for {
            fiber <- cache.get(key).fork
            _ <- cache.remove(key).delay(10 millis)
            res <- fiber.join.run
          } yield res.interrupted)

        }
      runtime.unsafeRun(program) === true

    }

  }
}
