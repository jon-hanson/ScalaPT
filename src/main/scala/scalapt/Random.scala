package scalapt

import cats.data.State

/**
  * Random Number Generator
  */

trait RNG[T] {
    def next : (RNG[T], T)
}

object RNG {
    type Type[T] = State[RNG[Double], T]

    def nextDouble : Type[Double] =
        State(rng => rng.next)
}

/**
  * Linear Congruential Generator RNG
  */

case class RandomLCG(seed : Long = 0) extends RNG[Double] {
    import RandomLCG._

    override def next : (RNG[Double], Double) = {
        val seed2 = (Mult * seed + Inc) % Mod
        (RandomLCG(seed2), seed2 / Scale)
    }
}

object RandomLCG {
    final val Mult = 214013L
    final val Inc = 2531011L
    final val Mod = 0x100000000L
    final val Scale = Int.MaxValue.toDouble - Int.MinValue.toDouble + 1.0
}

/**
  * Xor Shoft RNG
  */

case class XorShiftRNG(seed : Long) extends RNG[Long] {
    override def next : (RNG[Long], Long) = {
        val a = seed ^ (seed >>> 12)
        val b = a ^ (a << 25)
        val c = b ^ (b >>> 27)
        val d = if (c == 0) -1 else c
        (XorShiftRNG(d), d * 2685821657736338717L)
    }
}

/**
  * Convert a RNG of Longs into one of Doubles
  */

case class DoubleRNG(rng : RNG[Long]) extends RNG[Double] {
    import DoubleRNG._

    override def next: (RNG[Double], Double) = {
        val (rng2, rl) = rng.next
        val dl = (rl.toDouble - Long.MinValue.toDouble) / Scale
        (DoubleRNG(rng2), dl)
    }
}

object DoubleRNG {
    final val Scale = Long.MaxValue.toDouble - Long.MinValue.toDouble + 1.0
    final val Mod = 1L << 64
}

/**
  * API for requesting RNGs
  */
object Random {
    def randLong(seed: Long) : RNG[Long] =
        XorShiftRNG(seed)

    def randDouble(seed : Long) : RNG[Double] =
        DoubleRNG(XorShiftRNG(seed))
}
