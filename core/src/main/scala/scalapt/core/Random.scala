package scalapt.core

import cats.data.State

/**
  * Random Number Generator
  */

trait RNG {

    /**
      * @return a random double in the range 0 to 1 inclusive.
      */
    def nextDouble : (RNG, Double) = {
        val (rng2, rl) = nextLong
        val d = (rl.toDouble - Long.MinValue.toDouble) / RNG.Scale
        (rng2, d)
    }

    /**
      * @return a random long.
      */
    def nextLong : (RNG, Long) = {
        val (rng2, rd) = nextDouble
        val l = (rd * RNG.Scale + Long.MinValue.toDouble).toLong
        (rng2, l)
    }
}

object RNG {
    private final val Scale = Long.MaxValue.toDouble - Long.MinValue.toDouble + 1.0
    private final val Mod = 1L << 64

    type Type[T] = State[RNG, T]

    def nextDouble : Type[Double] =
        State(rng => rng.nextDouble)

    def nextLong : Type[Long] =
        State(rng => rng.nextLong)
}

/**
  * Linear Congruential Generator RNG
  */

case class LiCoGrRNG(seed : Long = 0) extends RNG {
    import LiCoGrRNG._

    override def nextDouble : (RNG, Double) = {
        val seed2 = (Mult * seed + Inc) % Mod
        (LiCoGrRNG(seed2), seed2 / Scale)
    }
}

object LiCoGrRNG {
    final val Mult = 214013L
    final val Inc = 2531011L
    final val Mod = 0x100000000L
    final val Scale = Int.MaxValue.toDouble - Int.MinValue.toDouble + 1.0
}

/**
  * Xor Shift RNG
  */

case class XorShiftRNG(seed : Long = 0) extends RNG {
    override def nextLong : (RNG, Long) = {
        val a = seed ^ (seed >>> 12)
        val b = a ^ (a << 25)
        val c = b ^ (b >>> 27)
        val d = if (c == 0) -1 else c
        (XorShiftRNG(d), d * 2685821657736338717L)
    }
}

///**
//  * Convert a RNG of Longs into one of Doubles
//  */
//
//case class DoubleRNG(rng : RNG[Long]) extends RNG[Double] {
//    import DoubleRNG._
//
//    override def next: (RNG[Double], Double) = {
//        val (rng2, rl) = rng.next
//        val dl = (rl.toDouble - Long.MinValue.toDouble) / Scale
//        (DoubleRNG(rng2), dl)
//    }
//}
//
//object DoubleRNG {
//    final val Scale = Long.MaxValue.toDouble - Long.MinValue.toDouble + 1.0
//    final val Mod = 1L << 64
//}

/**
  * API for requesting RNGs
  */
object Random {
    def xorShift(seed : Long) : RNG =
        XorShiftRNG(seed)

    def lcg(seed : Long) : RNG =
        LiCoGrRNG(seed)

//
//    def randDouble(seed : Long) : RNG[Double] =
//        XorShiftRNG(seed)
}
