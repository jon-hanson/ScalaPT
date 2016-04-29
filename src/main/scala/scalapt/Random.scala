package scalapt

import java.util.concurrent.atomic.AtomicInteger

trait RandomNumGen {
    def nextDouble() : Double

    def apply() : Double =
        nextDouble()
}

object RandomLCG {
    final val Mult = 214013L
    final val Inc = 2531011L
    final val Mod = 0x100000000L
    final val Scale = Int.MaxValue.toDouble - Int.MinValue.toDouble + 1.0
}

class RandomLCG(var seed : Long) extends RandomNumGen {
    import RandomLCG._

    def nextDouble(): Double = {
        seed = (Mult * seed + Inc) % Mod
        seed / Scale
    }
}

class ThreadSafeRNG(private val rngGen : Int => RandomNumGen) extends RandomNumGen {

    val seed = new AtomicInteger()

    val random = new ThreadLocal[RandomNumGen]() {
        override def initialValue() : RandomNumGen = rngGen(seed.incrementAndGet())
    }

    override def nextDouble() : Double = {
        random.get().nextDouble()
    }
}
