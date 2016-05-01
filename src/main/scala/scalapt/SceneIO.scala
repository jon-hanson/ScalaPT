package scalapt

import java.nio.file.{Files, Paths}
import java.util.NoSuchElementException

import cats.data.Xor
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object Codecs {

    final val TypeName = "type"

    final val DiffuseName = "Diffuse"
    final val ReflectiveName = "Reflective"
    final val RefractiveName = "Refractive"

    final val PlaneName = "Plane"
    final val SphereName = "Sphere"

    def withType(json : Json, name : String) : Json =
        json.mapObject(jo => jo.+:(TypeName, name.asJson))

    implicit val decodeAxis : Decoder[Axis.Type] =
        Decoder.instance(c =>
            c.focus.asString match {
                case Some(s) =>
                    try
                        Xor.right(Axis.withName(s))
                    catch {
                        case ex : NoSuchElementException =>
                            Xor.left(DecodingFailure(ex.getMessage, c.history))
                    }
                case None => Xor.left(DecodingFailure("String", c.history))
            }
        )

    implicit val encodeAxis : Encoder[Axis.Type] =
        Encoder.instance(axis =>
            axis.toString.asJson
        )

    implicit val decodeMaterial : Decoder[Material] =
        Decoder.instance(c =>
            c.downField(TypeName).as[String].flatMap {
                case DiffuseName => c.as[Diffuse]
                case ReflectiveName => c.as[Reflective]
                case RefractiveName => c.as[Refractive]
            }
        )

    implicit val encodeMaterial : Encoder[Material] =
        Encoder.instance {
            case (d: Diffuse) => withType(d.asJson, DiffuseName)
            case (r: Reflective) => withType(r.asJson, ReflectiveName)
            case (r: Refractive) => withType(r.asJson, RefractiveName)
        }

    implicit val decodeShape : Decoder[Shape] =
        Decoder.instance(c =>
            c.downField(TypeName).as[String].flatMap {
                case PlaneName => c.as[Plane]
                case SphereName => c.as[Sphere]
            }
        )

    implicit val encodeShape : Encoder[Shape] =
        Encoder.instance {
            case (p: Plane) => withType(p.asJson, PlaneName)
            case (s: Sphere) => withType(s.asJson, SphereName)
        }
}

object SceneIO {

    import Codecs._

    def load(fileName : String) : Scene = {
        val encoded = new String(Files.readAllBytes(Paths.get(fileName)))
        decode[Scene](encoded).valueOr(err => throw err)
    }

    def save(scene : Scene, fileName : String) = {
        val encoded = scene.asJson.spaces4
        Files.write(Paths.get(fileName), encoded.getBytes)
    }
}
