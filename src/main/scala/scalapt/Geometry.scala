package scalapt

/**
  * Axis
  */
object Axis extends Enumeration {
    type Type = Value
    val X, Y, Z = Value
}

/**
  * Point3
  */

object Point3 {
    final val Zero = Point3(0.0, 0.0, 0.0)
    final val XUnit = Point3(1.0, 0.0, 0.0)
    final val YUnit = Point3(0.0, 1.0, 0.0)
    final val ZUnit = Point3(0.0, 0.0, 1.0)

    val NegInf = new Point3(Double.NegativeInfinity, Double.NegativeInfinity, Double.NegativeInfinity)
    val PosInf = new Point3(Double.PositiveInfinity, Double.PositiveInfinity, Double.PositiveInfinity)
}

case class Point3(x : Double, y : Double, z : Double) {

    def apply(i : Int) : Double =
        i match {
            case 0 => x
            case 1 => y
            case 2 => z
        }

    def apply(ax : Axis.Type) : Double =
        ax match {
            case Axis.X => x
            case Axis.Y => y
            case Axis.Z => z
        }

    def withX(x : Double) =
        Point3(x, y, z)

    def withY(y : Double) =
        Point3(x, y, z)

    def withZ(z : Double) =
        Point3(x, y, z)

    def unary_+ =
        this

    def unary_- =
        Vector3(-x, -y, -z)

    def +(rhs : Vector3) : Point3 =
        Point3(x + rhs.x, y + rhs.y, z + rhs.z)

    def -(rhs : Vector3) : Point3 =
        Point3(x - rhs.x, y - rhs.y, z - rhs.z)

    def *(s : Double) : Point3 =
        Point3(x * s, y * s, z * s)

    def /(s : Double) : Point3 =
        Point3(x / s, y / s, z / s)

    def -(rhs : Point3) : Vector3 =
        Vector3(x - rhs.x, y - rhs.y, z - rhs.z)

    def length =
        math.sqrt(lengthSquared)

    def lengthSquared =
        x * x + y * y + z * z

    def hasNaNs =
        x.isNaN || y.isNaN || z.isNaN

    def asVector =
        Vector3(x, y, z)
}

/**
  * Vector3
  */
object Vector3 {
    final val Zero = Vector3(0.0, 0.0, 0.0)
    final val XUnit = Vector3(1.0, 0.0, 0.0)
    final val YUnit = Vector3(0.0, 1.0, 0.0)
    final val ZUnit = Vector3(0.0, 0.0, 1.0)

    def unit(ax :Axis.Type) : Vector3 =
        ax match {
            case Axis.X => XUnit
            case Axis.Y => YUnit
            case Axis.Z => ZUnit
        }
}

case class Vector3(x : Double, y : Double, z : Double) {

    def apply(i : Int) : Double =
        i match {
            case 0 => x
            case 1 => y
            case 2 => z
        }

    def apply(ax : Axis.Type) : Double =
        ax match {
            case Axis.X => x
            case Axis.Y => y
            case Axis.Z => z
        }

    def withX(nx : Double) =
        Vector3(nx, y, z)

    def withY(ny : Double) =
        Vector3(x, ny, z)

    def withZ(nz : Double) =
        Vector3(x, y, nz)

    def unary_+ =
        this

    def unary_- =
        Vector3(-x, -y, -z)

    def +(rhs : Vector3) : Vector3 =
        Vector3(x + rhs.x, y + rhs.y, z + rhs.z)

    def -(rhs : Vector3) : Vector3 =
        Vector3(x - rhs.x, y - rhs.y, z - rhs.z)

    def *(s : Double) : Vector3 =
        Vector3(x * s, y * s, z * s)

    def /(s : Double) : Vector3 =
        Vector3(x / s, y / s, z / s)

    def dot(rhs : Vector3) : Double =
        x * rhs.x + y * rhs.y + z * rhs.z

    def cross(rhs : Vector3) : Vector3 =
        Vector3(
            y * rhs.z - z * rhs.y,
            z * rhs.x - x * rhs.z,
            x * rhs.y - y * rhs.x
        )

    def length =
        math.sqrt(lengthSquared)

    def lengthSquared =
        x * x + y * y + z * z

    def normalise = {
        val s = 1.0 / length
        Vector3(x * s, y * s, z * s)
    }

    def hasNaNs =
        x.isNaN || y.isNaN || z.isNaN
}
