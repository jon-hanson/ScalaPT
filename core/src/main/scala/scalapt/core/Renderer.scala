package scalapt.core

import cats.data.State
import cats.implicits._
import com.typesafe.scalalogging.Logger
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Renderer
  */

trait Renderer {
    import Renderer._

    val width : Integer
    val height : Integer
    val scene : Scene

    private val cx = Vector3(width * scene.camera.fov / height, 0.0, 0.0)
    private val cy = cx.cross(scene.camera.ray.dir).normalise * scene.camera.fov

    def camRay(xs : Double, ys : Double) : Vector3 =
        cx * (xs / width - 0.5) +
            cy * (ys / height - 0.5)

    def render(
        frameI : Int,
        frameSeed : Long,
        rowSink : (Int, Long, Array[SuperSamp]) => Unit
    ) : Unit = {
        logger.info("Frame " + frameI)

        val tasks =
            (0 until height)
                .toList
                .traverse(genRowSeed)
                .runA(Random.xorShift(frameSeed))
                .value
                .map({case (y, seed) =>
                    Task({
                        val cells = render(y)
                            .runA(Random.xorShift(seed))
                            .value
                            .toArray
                        rowSink(y, seed, cells)
                    })
                })
        Await.result(Task.gather(tasks).runAsync, 60.minutes)
    }

    def genRowSeed(y : Int) : RNG.Type[(Int, Long)] =
        for {seed <- RNG.nextLong} yield (y, seed)

    def render(y : Int) :  RNG.Type[List[SuperSamp]] =
        (0 until width)
            .toList
            .traverse(render(_, y))

    def render(x : Int, y : Int) : RNG.Type[SuperSamp] = {
        def subPixelRad(cx : Double, cy : Double) : RNG.Type[RGB] = {
            for {
                d1 <- RNG.nextDouble
                d2 <- RNG.nextDouble
                dx = MathUtil.tent(d1)
                dy = MathUtil.tent(d2)
                sx = x + (0.5 + cx + dx) * 0.5
                sy = y + (0.5 + cy + dy) * 0.5
                dir = scene.camera.ray.dir + camRay(sx, sy)
                origin = scene.camera.ray.origin
                ray = Ray(origin, dir)
                result <- radiance(ray, 0, RGB.black, RGB.white)
            } yield result
        }

        for {
            aa <- subPixelRad(0, 0)
            ba <- subPixelRad(1, 0)
            ab <- subPixelRad(0, 1)
            bb <- subPixelRad(1, 1)
        } yield SuperSamp(aa, ba, ab, bb)
    }

    def radiance(
        ray : Ray,
        depth : Integer,
        acc : RGB,
        att : RGB
    ) : RNG.Type[RGB]
}

object Renderer {
    // Need a maximum to avoid stack overflow.
    // In practice we should never hit it due to the Russian Roulette termination.
    final val MaxDepth = 2000

    val logger : Logger = Logger[Renderer]
}
