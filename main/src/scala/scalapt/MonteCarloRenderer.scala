package scalapt

import java.util.concurrent.TimeUnit

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
            .grouped(groupSize).toList.map( { subRange =>
            Future {subRange.foreach { impl(_) }}
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

    override def radiance(
        rng : RandomNumGen,
        ray : Ray,
        depth : Integer
    ) : RGB = {
        scene.intersect(ray) match {
            case None => RGB.black
            case Some((prim, isect)) => {
                val n = prim.normal(isect)
                val n1 =
                    if (n.dot(ray.dir) < 0)
                        n
                    else
                        -n

                val newDepth = depth + 1

                val refl = {
                    val colour = prim.material.colour

                    val lazyCol = () => prim.material.radiance(this, rng, ray, newDepth, isect, n, n1) * colour
                    if (newDepth > 5) {
                        // Modified Russian roulette.
                        val max = colour.max * MathUtil.sqr(1.0 - depth / Renderer.MaxDepth)
                        if (rng() >= max) {
                            RGB.black
                        } else {
                            lazyCol() / max
                        }
                    } else {
                        lazyCol()
                    }
                }

                prim.material.emission + refl
            }
        }
    }
}
