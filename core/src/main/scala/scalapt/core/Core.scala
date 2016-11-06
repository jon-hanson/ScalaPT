package scalapt.core

import cats.implicits._
import com.typesafe.scalalogging.Logger

object MathUtil {

    /**
      * Tent function
      */
    def tent(x : Double) : Double = {
        val x2 = 2.0 * x
        if (x2 < 1.0)
            math.sqrt(x2) - 1.0
        else
            1.0 - math.sqrt(2.0 - x2)
    }

    /**
      * Clamp a value to a range.
      */
    def clamp(x : Double, low : Double = 0.0, high : Double = 1.0) : Double = {
        if (x > high)
            high
        else if (x < low)
            low
        else
            x
    }

    /**
      * Square function.
      */
    def sqr(x : Double) : Double =
        x * x

    // Standard gamma correction value.
    final val GAMMA : Double = 2.2

    /**
      * Standard gamma correction.
      */
    def gammaCorr(x : Double) : Double =
        math.pow(clamp(x), 1.0 / GAMMA)
}

/**
 * Ray
 */

case class Ray private (dir : Vector3, origin : Point3) {
    def apply(t : Double) : Point3 =
        origin + dir * t
}

object Ray {
    def apply(origin : Point3, dir : Vector3) : Ray =
        Ray(dir.normalise, origin)
}

/**
  * Material
  */

trait Material {
    val colour : RGB

    val emColour : RGB

    def emission : RGB =
        emColour

    def radiance(
        rdr : Renderer,
        ray : Ray,
        depth : Integer,
        p : Point3,
        n : Vector3,
        nl : Vector3,
        acc : RGB,
        att : RGB
    ) : RNG.Type[RGB]
}

object Material {
    def diffuse(r : Double, g : Double, b : Double) =
        Diffuse(RGB(r, g, b), RGB.black)

    def diffuse(colour : RGB) =
        Diffuse(colour, RGB.black)

    def emissive(r : Double, g : Double, b : Double) =
        Diffuse(RGB.black, RGB(r, g, b), true)

    def emissive(colour : RGB) =
        Diffuse(RGB.black, colour, true)

    def refractive(r : Double, g : Double, b : Double) =
        Refractive(RGB(r, g, b), RGB.black)

    def refractive(colour : RGB) =
        Refractive(colour, RGB.black)

    def reflective(r : Double, g : Double, b : Double) =
        Reflective(RGB(r, g, b), RGB.black)

    def reflective(colour : RGB) =
        Reflective(colour, RGB.black)
}

/**
 * Camera
 */

case class Camera(ray : Ray, fov : Double)

/**
  * Shape
  */

trait Shape {
    // Only required for debugging.
    val name : String

    val material : Material

    def intersect(ray : Ray, eps : Double) : Option[Double]

    def intersect(ray : Ray) : Option[Double] =
        intersect(ray, Shape.T_EPS)

    def normal(p : Point3) : Vector3
}

object Shape {
    /**
      * Epsilion value to avoid object self-intersections.
      */

    final val T_EPS : Double = 1e-6
}

/**
  * Scene
  */

case class Scene(camera : Camera, shapes : List[Shape]) {
    /**
      * Find the closest shape intersected by the ray.
      */
    def intersect(ray : Ray) : Option[(Shape, Point3)] =
         shapes
            .flatMap(obj => obj.intersect(ray).map(t => (obj, t)))
            .reduceOption(Scene.distance.min)
            .map({case(obj, t) => (obj, ray(t))})
}

object Scene {
    val distance = Ordering.by((_: (Shape, Double))._2)
}

/**
  * Represents a single rendered frame.
  */

case class Frame(width : Int, height : Int, rows : Array[Frame.Row]) {

    def apply(r : Int) : Frame.Row =
        rows(r)

    def apply(r : Int, c : Int) : SuperSamp =
        rows(r)(c)

    def merge(y : Int, newRow : Frame.Row, n : Int) : Frame.Row = {
        if (n == 0) {
            rows(y) = newRow
        } else {
            val row = rows(y)
            for (x <- row.indices) {
                row(x).mergeFrom(newRow(x), n)
            }
        }
        rows(y)
    }
}

object Frame {
    type Row = Array[SuperSamp]

    def apply(width : Int, height : Int) : Frame =
        Frame(width, height, new Array[Frame.Row](width))
}

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

    def render() : RNG.Type[Frame] = {
        (0 until height)
            .toList
            .traverseU(render(_))
            .map(rows => Frame(width, height, rows.map(_.toArray).toArray))
    }

    def render(y : Int) :  RNG.Type[List[SuperSamp]] =
        (0 until width)
            .toList
            .traverseU(render(_, y))

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

    val logger = Logger[Renderer]
}
