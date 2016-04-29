package scalapt

object MathUtil {

    def tent(x : Double) : Double = {
        val x2 = 2.0 * x
        if (x2 < 1.0)
            math.sqrt(x2) - 1.0
        else
            1.0 - math.sqrt(2.0 - x2)
    }

    def clamp(x : Double) : Double =
        clamp(x, 0.0, 1.0)

    def clamp(x : Double, low : Double, high : Double) : Double = {
        if (x > high)
            high
        else if (x < low)
            low
        else
            x
    }

    def sqr(x : Double) : Double =
        x * x

    // Gamma correction.
    final val GAMMA : Double = 2.2

    def gammaCorr(x : Double) : Double =
        math.pow(clamp(x), 1.0 / GAMMA)
}

/**
 * Ray
 */

object Ray {
    def apply(origin : Point3, dir : Vector3) : Ray =
        Ray(dir.normalise, origin)
}

case class Ray private (dir : Vector3, origin : Point3) {
    def apply(t : Double) : Point3 =
        origin + dir * t
}

/**
  * Material
  */
object Material {
    def diffuse(r : Double, g : Double, b : Double) =
        new Diffuse(RGB(r, g, b), RGB.black)

    def diffuse(colour : RGB) =
        new Diffuse(colour, RGB.black)

    def emissive(r : Double, g : Double, b : Double) =
        new Diffuse(RGB.black, RGB(r, g, b), true)

    def emissive(colour : RGB) =
        new Diffuse(RGB.black, colour, true)

    def refractive(r : Double, g : Double, b : Double) =
        new Refractive(RGB(r, g, b), RGB.black)

    def refractive(colour : RGB) =
        new Refractive(colour, RGB.black)

    def reflective(r : Double, g : Double, b : Double) =
        new Reflective(RGB(r, g, b), RGB.black)

    def reflective(colour : RGB) =
        new Reflective(colour, RGB.black)
}

trait Material {
    val colour : RGB

    val emColour : RGB

    def emission : RGB =
        emColour

    def radiance(
        rdr : Renderer,
        rng : RandomNumGen,
        ray : Ray,
        depth : Integer,
        x : Point3,
        n : Vector3,
        nl : Vector3
    ) : RGB
}

/**
 * Shape
 */

object Shape {
    // Epsilion value to avoid object self-intersections.
    final val T_EPS : Double = 1e-4
}

trait Shape {
    val name : String

    val material : Material

    def intersect(ray : Ray, eps : Double) : Option[Double]

    def intersect(ray : Ray) : Option[Double] =
        intersect(ray, Shape.T_EPS)

    def normal(p : Point3) : Vector3
}

/**
 * Camera
 */
case class Camera(ray : Ray, fov : Double)

/**
 * Scene
 */
case class Scene(camera : Camera, prims : List[Shape]) {

    def intersect(ray : Ray) : Option[(Shape, Point3)] = {
        val isectOpts = prims.map(obj => obj.intersect(ray).map(t => (obj, t)))
        val isects = isectOpts.flatten
        val first = isects.foldLeft(None : Option[(Shape, Double)])((first, isect) => {
            first match {
                case None => Some(isect)
                case Some(closestIsect) => {
                    if (closestIsect._2 < isect._2)
                        first
                    else
                        Some(isect)
                }
            }
        })

        first.map(isect => (isect._1, ray(isect._2)))
    }
}
