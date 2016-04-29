package scalapt

/**
  * A triple of colour coefficients.
  */
object RGB {
    final val black : RGB = RGB(0.0, 0.0, 0.0)
    final val white = RGB(1.0, 1.0, 1.0)

    final val red = RGB(1.0, 0.0, 0.0)
    final val green = RGB(0.0, 1.0, 0.0)
    final val blue = RGB(0.0, 0.0, 1.0)

    def apply() =
        black
}

case class RGB(red : Double, green : Double, blue : Double) {

    override def toString = "{R : " + red + ", G : " + green + ", B : " + blue + "}"

    def apply(i : Int) : Double = {
        i match {
            case 0 => red
            case 1 => green
            case 2 => blue
        }
    }

    def unary_+ =
        this

    def unary_- =
        Vector3(-red, -green, -blue)

    def +(rhs : RGB) =
        RGB(red + rhs.red, green + rhs.green, blue + rhs.blue)

    def -(rhs : RGB) =
        RGB(red - rhs.red, green - rhs.green, blue - rhs.blue)

    def *(s : Double) =
        RGB(red * s, green * s, blue * s)

    def /(d : Double) = {
        val s = 1.0 / d
        this * s
    }

    def *(rhs : RGB) =
        RGB(red * rhs.red, green * rhs.green, blue * rhs.blue)

    def length =
        math.sqrt(lengthSquared)

    def lengthSquared =
        red * red + green * green + blue * blue

    def normalise = {
        val s = 1.0 / length
        RGB(red * s, green * s, blue * s)
    }

    def hasNaNs =
        red.isNaN || green.isNaN || blue.isNaN

    def clamp =
        RGB(MathUtil.clamp(red), MathUtil.clamp(green), MathUtil.clamp(blue))

    def max =
        math.max(math.max(red, green), blue)
}

/**
  * SuperSamp is a 2x2 grid of colous, used for super-sampling
  * in order improve anti-aliasing.
  */
object SuperSamp {
    final val black = SuperSamp(RGB.black, RGB.black, RGB.black, RGB.black)
}

case class SuperSamp(c00 : RGB, c10 : RGB, c01 : RGB, c11 : RGB) {

    def apply(x : Int, y : Int) : RGB =
        (x, y) match {
            case (0, 0) => c00
            case (0, 1) => c01
            case (1, 0) => c10
            case (1, 1) => c11
        }

    def merge(rhs : SuperSamp, n : Int) : SuperSamp =
        SuperSamp(
            (c00 * n + rhs.c00) / (n + 1),
            (c10 * n + rhs.c10) / (n + 1),
            (c01 * n + rhs.c01) / (n + 1),
            (c11 * n + rhs.c11) / (n + 1)
        )

    def clamp : RGB =
        (c00.clamp + c10.clamp + c01.clamp + c11.clamp) * 0.25

    def +(rhs : SuperSamp) : SuperSamp =
        SuperSamp(c00 + rhs.c00, c10 + rhs.c10, c01 + rhs.c01, c11 + rhs.c11)
}

