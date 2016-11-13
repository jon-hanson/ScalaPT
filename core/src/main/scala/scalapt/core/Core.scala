package scalapt.core

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

case class Frame(seed : Long, width : Int, height : Int, rows : Array[Frame.Row]) {

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

    def apply(seed : Long, width : Int, height : Int) : Frame =
        Frame(seed, width, height, new Array[Frame.Row](width))
}




