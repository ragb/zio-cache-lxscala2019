package com.ruiandrebatista.zio.cache

trait LoadCache[F[_], K, V] {
  def get(k: K): F[V]

  def remove(k: K): F[Boolean]

  def refresh(key: K): F[V]
}

