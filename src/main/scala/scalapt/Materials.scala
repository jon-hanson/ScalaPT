package scalapt

/**
  * Diffuse
  */
case class Diffuse(colour : RGB, emColour : RGB, emis : Boolean = false) extends Material {

    override def emission : RGB =
        emColour

    override def radiance(
        rdr : Renderer,
        ray : Ray, depth: Integer,
        p : Point3,
        n : Vector3,
        nl : Vector3
    ) : RNG.Type[RGB] = {
        RNG.nextDouble.flatMap(d1 => {
            RNG.nextDouble.flatMap(r2 => {
                val r1 = 2.0 * math.Pi * d1
                val r2s = math.sqrt(r2)
                val w = nl
                val u = (if (math.abs(w.x) > 0.1) Vector3.YUnit else Vector3.XUnit).cross(w).normalise
                val v = w.cross(u)
                val d =
                    (u * math.cos(r1) * r2s
                        + v * math.sin(r1) * r2s
                        + w * math.sqrt(1.0 - r2)
                        ).normalise
                rdr.radiance(Ray(p, d), depth)
            })
        })
    }
}

/**
  * Refractive
  */
case class Refractive(colour : RGB, emColour : RGB) extends Material {

    override def radiance(
        rdr : Renderer,
        ray : Ray, depth: Integer,
        p : Point3,
        n : Vector3,
        nl : Vector3
    ) : RNG.Type[RGB] = {
        val nc = 1.0
        val nt = 1.5
        val reflRay = Ray(p, ray.dir - n * 2.0 * n.dot(ray.dir))
        val into = n.dot(nl) > 0.0
        val nnt = if (into) nc / nt else nt / nc
        val ddn = ray.dir.dot(nl)
        val cos2t = 1.0 - nnt * nnt * (1.0 - ddn * ddn)
        if (cos2t < 0.0) {
            // Total internal reflection.
            rdr.radiance(reflRay, depth)
        } else {
            val sign = if (into) 1.0 else -1.0
            val tdir = (ray.dir * nnt - n * (sign * (ddn * nnt + Math.sqrt(cos2t)))).normalise
            val a = nt - nc
            val b = nt + nc
            val r0 = a * a / (b * b)
            val c = 1.0 - (if (into) -ddn else tdir.dot(n))
            val re = r0 + (1.0 - r0) * c * c * c * c * c
            val tr = 1.0 - re
            val q = 0.25 + re / 2.0
            val rp = re / q
            val tp = tr / (1.0 - q)

            RNG.nextDouble.flatMap(rnd => {
                if (rnd < q) {
                    rdr.radiance(reflRay, depth).map(rad => rad * rp)
                } else {
                    rdr.radiance(Ray(p, tdir), depth).map(rad => rad * tp)
                }
            })
        }
    }
}

/**
  * Reflective
  */
case class Reflective(colour : RGB, emColour : RGB) extends Material {
    override def radiance(
        rdr : Renderer,
        ray : Ray, depth: Integer,
        p : Point3,
        n : Vector3,
        nl : Vector3
    ) : RNG.Type[RGB] = {
        val d = ray.dir - n * 2 * n.dot(ray.dir)
        rdr.radiance(Ray(p, d), depth)
    }
}
