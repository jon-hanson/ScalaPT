package scalapt

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
        p : Point3,
        n : Vector3,
        nl : Vector3
    ) : RGB
}

/**
 * Camera
 */
case class Camera(ray : Ray, fov : Double)

/**
  * Shape
  */

object Shape {
    /**
      * Epsilion value to avoid object self-intersections.
      */

    final val T_EPS : Double = 1e-4
}

trait Shape {
    // Only required for debugging.
    val name : String

    val material : Material

    def intersect(ray : Ray, eps : Double) : Option[Double]

    def intersect(ray : Ray) : Option[Double] =
        intersect(ray, Shape.T_EPS)

    def normal(p : Point3) : Vector3
}

/**
  * Scene
  */

object Scene {
    val distance = Ordering.by((_: (Shape, Double))._2)
}

case class Scene(camera : Camera, shapes : List[Shape]) {
    /**
      * Find the closest shape intersected by the ray.
      * @param ray
      * @return closest shape and intersection point
      */
    def intersect(ray : Ray) : Option[(Shape, Point3)] = {
         shapes
            .flatMap(obj => obj.intersect(ray).map(t => (obj, t)))
            .reduceOption(Scene.distance.min)
            .map({case(obj, t) => (obj, ray(t))})
    }
}

/**
  * Renderer
  */

object Renderer {
    // Need a maximum to avoid stack overflow.
    final val MaxDepth = 200
}

trait Renderer {
    def width : Integer
    def height : Integer
    def scene : Scene

    private val cx = Vector3(width * scene.camera.fov / height, 0.0, 0.0)
    private val cy = cx.cross(scene.camera.ray.dir).normalise * scene.camera.fov

    def camRay(xs : Double, ys : Double) : Vector3 =
        cx * (xs / width - 0.5) +
            cy * (ys / height - 0.5)

    def render(rng : RandomNumGen, x : Int, y : Int) : SuperSamp = {
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

    def radiance(
        rng : RandomNumGen,
        ray : Ray,
        depth : Integer
    ) : RGB

}
