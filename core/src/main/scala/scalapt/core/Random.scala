package scalapt.core

import cats.data.State

/**
  * Random Number Generator.
  * The two nextXXX methods are implemented in terms of each other,
  * so implementing classes must override at least one method.
  */

trait RNG {

    /**
      * @return a random double in the range 0 to 1 inclusive.
      */
    def nextDouble0To1 : (RNG, Double) = {
        val (rng2, rl) = nextLong
        val d = (rl.toDouble - Long.MinValue.toDouble) / RNG.Scale
        (rng2, d)
    }

    /**
      * @return a random long.
      */
    def nextLong : (RNG, Long) = {
        val (rng2, rd) = nextDouble0To1
        val l = (rd * RNG.Scale + Long.MinValue.toDouble).toLong
        (rng2, l)
    }
}

object RNG {
    private final val Scale = Long.MaxValue.toDouble - Long.MinValue.toDouble + 1.0

    type Type[T] = State[RNG, T]

    def nextDouble : Type[Double] =
        State(rng => rng.nextDouble0To1)

    def nextLong : Type[Long] =
        State(rng => rng.nextLong)
}

/**
  * Linear Congruential Generator RNG
  */

case class LcgRNG(seed : Long = 0) extends RNG {
    import LcgRNG._

    override def nextDouble0To1 : (RNG, Double) = {
        val seed2 = (Mult * seed + Inc) % Mod
        (LcgRNG(seed2), seed2 / Scale)
    }
}

object LcgRNG {
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

/**
  * API for requesting RNGs
  */
object Random {
    def xorShift(seed : Long) : RNG =
        XorShiftRNG(seed)

    def lcg(seed : Long) : RNG =
        LcgRNG(seed)
}
