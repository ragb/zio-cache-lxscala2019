package com.ruiandrebatista.zio.cache

import zio._
import zio.syntax._

class ZioLoadCache[K, R, E, V] private (
    loader: K => ZIO[R, E, V],
    ref: RefM[Map[K, ZioLoadCache.LoadState[E, V]]]
) extends LoadCache[ZIO[R, E, ?], K, V] {

  def get(key: K): ZIO[R, E, V] =
    ref
      .modify { cacheState =>
        cacheState
          .get(key)
          .fold(doLoad(cacheState, key))(state => (state.p, cacheState).succeed)
      }
      .flatMap(_.await)

  def remove(key: K): UIO[Boolean] =
    ref
      .modify[Any, Nothing, Boolean] { cacheState =>
        cacheState
          .get(key)
          .fold((false, cacheState).succeed) { state =>
            state.fiber.interrupt.const((true, cacheState - key))
          }
      }

  def refresh(key: K): ZIO[R, E, V] =
    ref
      .modify { cacheState =>
        cacheState.get(key).fold(UIO.unit)(_.fiber.interrupt.unit) *>
          doLoad(cacheState, key)

      }
      .flatMap(_.await)

  private def doLoad(cacheState: Map[K, ZioLoadCache.LoadState[E, V]], key: K) =
    for {
      p <- Promise.make[E, V]
      fiber <- loader(key).interruptible.to(p).fork
    } yield (p, cacheState.updated(key, ZioLoadCache.LoadState(fiber, p)))

  private[cache] def purge: UIO[Unit] =
    ref.update { cacheState =>
      val fibers = cacheState.values.map(_.fiber)
      Fiber
        .interruptAll(fibers)
        .const(Map.empty)
    }.unit

}

object ZioLoadCache {

  private case class LoadState[E, V](
      fiber: Fiber[Nothing, Boolean],
      p: Promise[E, V]
  )

  private def aquire[K, R, E, V](
      loader: K => ZIO[R, E, V]
  ): UIO[ZioLoadCache[K, R, E, V]] =
    for {
      ref <- RefM.make(Map.empty[K, LoadState[E, V]])
    } yield new ZioLoadCache(loader, ref)

  def apply[K, R, E, V](
      loader: K => ZIO[R, E, V]
  ): ZManaged[R, E, ZioLoadCache[K, R, E, V]] =
    ZManaged.make(aquire(loader))(_.purge)

}
