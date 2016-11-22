package scalapt.core

import cats._

/**
  * A triple of colour coefficients.
  */

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
        this * (1.0 / d)
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

object RGB {
    final val black : RGB = RGB(0.0, 0.0, 0.0)
    final val white = RGB(1.0, 1.0, 1.0)

    final val red = RGB(1.0, 0.0, 0.0)
    final val green = RGB(0.0, 1.0, 0.0)
    final val blue = RGB(0.0, 0.0, 1.0)

    def apply() =
        black

    implicit val rgbMonoid : Monoid[RGB] = new Monoid[RGB] {
        override def empty : RGB =
            black

        override def combine(x : RGB, y : RGB) : RGB =
            x + y
    }
}

/**
  * SuperSamp is a 2x2 grid of colours, used for super-sampling
  * in order improve anti-aliasing.
  */

class SuperSamp(var c00 : RGB, var c10 : RGB, var c01 : RGB, var c11 : RGB) {

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

    def mergeFrom(rhs : SuperSamp, n : Int) : Unit = {
        c00 = (c00 * n + rhs.c00) / (n + 1)
        c10 = (c10 * n + rhs.c10) / (n + 1)
        c01 = (c01 * n + rhs.c01) / (n + 1)
        c11 = (c11 * n + rhs.c11) / (n + 1)
    }

    def clamp : RGB =
        (c00.clamp + c10.clamp + c01.clamp + c11.clamp) * 0.25

    def +(rhs : SuperSamp) : SuperSamp =
        SuperSamp(c00 + rhs.c00, c10 + rhs.c10, c01 + rhs.c01, c11 + rhs.c11)
}

object SuperSamp {
    final val black = SuperSamp(RGB.black, RGB.black, RGB.black, RGB.black)

    def apply(c00 : RGB, c10 : RGB, c01 : RGB, c11 : RGB) : SuperSamp = {
        new SuperSamp(c00, c10, c01, c11)
    }

    implicit val rgbMonoid : Monoid[SuperSamp] = new Monoid[SuperSamp] {
        override def empty : SuperSamp =
            black

        override def combine(x : SuperSamp, y : SuperSamp) : SuperSamp =
            x + y
    }
}
