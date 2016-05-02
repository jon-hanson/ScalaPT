package scalapt

object GenerateScenes {

    def main(args : Array[String]) : Unit = {
        SceneIO.save(Cornell.scene, "scenes/cornell.json")
        SceneIO.save(Cornell2.scene, "scenes/cornell2.json")
        SceneIO.save(Horizon.scene, "scenes/horizon.json")
    }
}

/**
  * Original smallpt Cornell box scene.
  */
object Cornell {

    val objects : List[Shape] =
        List(
            Plane("left", Material.diffuse(0.75, 0.25, 0.25), Axis.X, true, 1),
            Plane("right", Material.diffuse(0.25, 0.25, 0.75), Axis.X, false, 99),
            Plane("back", Material.diffuse(0.75, 0.75, 0.75), Axis.Z, true, 0),
            Plane("front", Material.diffuse(RGB.black), Axis.Z, false, 170),
            Plane("bottom", Material.diffuse(0.75, 0.75, 0.75), Axis.Y, true, 0),
            Plane("top", Material.diffuse(0.75, 0.75, 0.75), Axis.Y, false, 81.6),

            Sphere("mirror", Material.reflective(RGB.white * 0.999), Point3(27, 16.5, 47), 16.5),
            Sphere("glass", Material.refractive(RGB.white * 0.999), Point3(73, 16.5, 78), 16.5),

            Sphere("light", Material.emissive(RGB.white * 12), Point3(50, 681.6 - 0.27, 81.6), 600.0)
        )
    val scene = Scene(
        Camera(
            Ray(
                Point3(50, 52, 295.6),
                Vector3(0, -0.042612, -1)
            ), 0.5135
        ), objects)
}


object Cornell2 {

    val objects : List[Shape] =
        List(
            Plane("left", Material.diffuse(0.75, 0.25, 0.25), Axis.X, true, 1),
            Plane("right", Material.diffuse(0.25, 0.25, 0.75), Axis.X, false, 99),
            Plane("back", Material.diffuse(0.75, 0.75, 0.75), Axis.Z, true, 0),
            Plane("front", Material.diffuse(RGB.black), Axis.Z, false, 170),
            Plane("bottom", Material.diffuse(0.75, 0.75, 0.75), Axis.Y, true, 0),
            Plane("top", Material.diffuse(0.75, 0.75, 0.75), Axis.Y, false, 81.6),

            Sphere("mirror", Material.reflective(RGB.white * 0.999), Point3(27, 60, 47), 16.5),
            Sphere("glass", Material.refractive(RGB.white * 0.999), Point3(73, 16.5, 78), 16.5),

            Sphere("diff", Material.diffuse(0.75, 0.75, 0.25), Point3(27, 16.5, 100), 16.5),

            Sphere("light", Material.emissive(RGB.white * 12), Point3(50, 681.6 - 0.27, 81.6), 600.0)
        )
    val scene = Scene(
        Camera(
            Ray(
                Point3(50, 52, 295.6),
                Vector3(0, -0.042612, -1)
            ), 0.5135
        ), objects)
}

object Horizon {
    val W = 100.0
    val W2 = W * 2.0 / 3.0
    val D = W / 2.0
    val R = W / 4.0
    val R2 = W * 16.0
    val centre = Point3(0.0, R, 0.0)

    val sky = RGB(135.0/256, 206.0/256, 250.0/256)

    val objects : List[Shape] =
        List(
            Plane("ground", Material.diffuse(RGB.white * 0.999), Axis.Y, true, 0.0),
            Sphere("refl", Material.reflective(RGB.white * 0.999), Point3(0.0, W2, -W2), W2),
            Sphere("lglass", Material.refractive(0.75, 0.25, 0.25), Point3(-W2, R, W), R),
            Sphere("rglass", Material.refractive(0.25, 0.75, 0.25), Point3(W2, R, W), R),
            Sphere("light", Material.emissive(RGB.white * 12.0), centre + Vector3.YUnit * W * 2, R),
            Sphere("sky", Material.diffuse(sky), Point3(0.0, W * 3 + R2, 0.0), R2)
        )

    val cam = Point3(0.0, W * 1.5, 4 * W)
    val lookAt = Point3(0.0, R, 0.0)

    val scene = Scene(
        Camera(
            Ray(
                cam,
                lookAt - cam
            ), 0.6
        ), objects)
}
