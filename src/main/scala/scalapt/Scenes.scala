package scalapt

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
