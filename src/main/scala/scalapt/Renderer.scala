package scalapt

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
//
///**
//  * RenderContext
//  */
//case class RenderContext(width : Integer, height : Integer, scene : Scene) {
//    private val cx = Vector3(width * scene.camera.fov / height, 0.0, 0.0)
//    private val cy = cx.cross(scene.camera.ray.dir).normalise * scene.camera.fov
//
//    def camRay(xs : Double, ys : Double) : Vector3 =
//        cx * (xs / width - 0.5) +
//            cy * (ys / height - 0.5)
//}

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
  * Renderer
  */
object Renderer {
    // Need a maximum to avoid stack overflow.
    final val MaxDepth = 200
}

abstract class Renderer(
    val width : Integer,
    val height : Integer,
    val scene : Scene
) {
    private val cx = Vector3(width * scene.camera.fov / height, 0.0, 0.0)
    private val cy = cx.cross(scene.camera.ray.dir).normalise * scene.camera.fov

    def camRay(xs : Double, ys : Double) : Vector3 =
        cx * (xs / width - 0.5) +
            cy * (ys / height - 0.5)

    def radiance(
        rng : RandomNumGen,
        ray : Ray,
        depth : Integer
    ) : RGB

    def render(
        rng : RandomNumGen,
        x : Int,
        y : Int
    ) : SuperSamp
}

class MonteCarloRenderer(
    width : Integer,
    height : Integer,
    scene : Scene
) extends Renderer(width, height, scene) {

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

                    val lazyRad = () => prim.material.radiance(this, rng, ray, newDepth, isect, n, n1)
                    if (newDepth > 5) {
                        // Modified Russian roulette.
                        val max = colour.max * MathUtil.sqr(1.0 - depth / Renderer.MaxDepth)
                        if (rng() >= max) {
                            RGB.black
                        } else {
                            lazyRad() * colour / max
                        }
                    } else {
                        colour * lazyRad()
                    }
                }

                prim.material.emission + refl
            }
        }
    }

    override def render(rng : RandomNumGen, x : Int, y : Int) : SuperSamp = {
        def subRad(cx : Double, cy : Double) = {
            val dx = MathUtil.tent(rng())
            val dy = MathUtil.tent(rng())
            val sx = x + (0.5 + cx + dx) * 0.5
            val sy = y + (0.5 + cy + dy) * 0.5
            val dir = scene.camera.ray.dir + camRay(sx, sy)
            val origin = scene.camera.ray.origin
            val ray = Ray(origin, dir)
            radiance(rng, ray, 0)
        }

        SuperSamp(subRad(0, 0), subRad(1, 0), subRad(0, 1), subRad(1, 1))
    }
}
