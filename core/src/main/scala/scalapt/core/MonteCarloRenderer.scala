package scalapt.core

import cats.data.State
import com.typesafe.scalalogging.Logger

/**
  * Monte-Carlo path tracing renderer.
  */
class MonteCarloRenderer(
    val width : Integer,
    val height : Integer,
    val scene : Scene
) extends Renderer {

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
            case Some((prim, isect)) => {
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
                    for {
                        rnd <- RNG.nextDouble
                        result <- if (rnd >= max) {
                            State.pure(acc2).asInstanceOf[RNG.Type[RGB]]
                        } else {
                            prim.material.radiance(this, ray, newDepth, isect, n, nl, acc2, colour / max)
                        }
                    } yield result
                } else {
                    prim.material.radiance(this, ray, newDepth, isect, n, nl, acc2, colour)
                }
            }
        }
    }
}

object MonteCarloRenderer {
    val logger = Logger[MonteCarloRenderer]
}
