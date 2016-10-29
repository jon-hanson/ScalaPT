package scalapt

import java.util.concurrent.TimeUnit

import cats.data.State
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object ConcurrentUtils {

    implicit final val ExCtx = ExecutionContext.fromExecutor(null)

    final val processorCount = Runtime.getRuntime.availableProcessors

    def parallelFor(range : Range)(impl : Int => Unit) = {
        val wait = Duration.create(10, TimeUnit.DAYS)
        val groupSize = range.size / processorCount

        range
            .sortBy(i => i % processorCount)
            .grouped(groupSize)
            .toList.map( { subRange =>
                Future {subRange.foreach { impl }}
            }).foreach(f => Await.result(f, wait))
    }
}

/**
  * Monte-Carlo path tracing renderer.
  */
class MonteCarloRenderer(
    val width : Integer,
    val height : Integer,
    val scene : Scene
) extends Renderer {
    import MonteCarloRenderer.logger

    final override def radiance(
        ray : Ray,
        depth : Integer,
        acc : RGB,
        att : RGB
    ) : RNG.Type[RGB] = {
        scene.intersect(ray) match {
            case None => {
                State.pure(acc)
            }
            case Some((prim, isect)) =>
                val n = prim.normal(isect)
                val nl =
                    if (n.dot(ray.dir) < 0)
                        n
                    else
                        -n

                val newDepth = depth + 1

                val colour = prim.material.colour * att
                val acc2 = acc + prim.material.emission * att

                if (newDepth > 5) {
                    // Modified Russian roulette.
                    val max = colour.max * MathUtil.sqr(1.0 - depth / Renderer.MaxDepth)
                    RNG.nextDouble.flatMap(rnd => {
                        if (rnd >= max) {
                            State.pure(acc2)
                        } else {
                            prim.material.radiance(this, ray, newDepth, isect, n, nl, acc2, colour / max)
                        }
                    })
                } else {
                    prim.material.radiance(this, ray, newDepth, isect, n, nl, acc2, colour)
                }
        }
    }
}

object MonteCarloRenderer {
    val logger = Logger[Main]
}
