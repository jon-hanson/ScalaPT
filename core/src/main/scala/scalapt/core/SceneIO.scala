package scalapt.core

import java.nio.file.{Files, Paths}
import java.util.NoSuchElementException

import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object Codecs {

    final val TYPE_NAME = "type"

    final val DIFFUSE_NAME = "Diffuse"
    final val REFLECTIVE_NAME = "Reflective"
    final val REFRACTIVE_NAME = "Refractive"

    final val PLANE_NAME = "Plane"
    final val SPHERE_NAME = "Sphere"

    def withType(json : Json, name : String) : Json =
        json.mapObject(jo => jo.+:(TYPE_NAME, name.asJson))

    implicit val decodeAxis : Decoder[Axis.Type] =
        Decoder.instance(c =>
            c.focus.asString match {
                case Some(s) =>
                    try
                        Either.right(Axis.withName(s))
                    catch {
                        case ex : NoSuchElementException =>
                            Either.left(DecodingFailure(ex.getMessage, c.history))
                    }
                case None => Either.left(DecodingFailure("String", c.history))
            }
        )

    implicit val encodeAxis : Encoder[Axis.Type] =
        Encoder.instance(axis =>
            axis.toString.asJson
        )

    implicit val decodeMaterial : Decoder[Material] =
        Decoder.instance(c =>
            c.downField(TYPE_NAME).as[String].flatMap {
                case DIFFUSE_NAME => c.as[Diffuse]
                case REFLECTIVE_NAME => c.as[Reflective]
                case REFRACTIVE_NAME => c.as[Refractive]
            }
        )

    implicit val encodeMaterial : Encoder[Material] =
        Encoder.instance {
            case (d: Diffuse) => withType(d.asJson, DIFFUSE_NAME)
            case (r: Reflective) => withType(r.asJson, REFLECTIVE_NAME)
            case (r: Refractive) => withType(r.asJson, REFRACTIVE_NAME)
        }

    implicit val decodeShape : Decoder[Shape] =
        Decoder.instance(c =>
            c.downField(TYPE_NAME).as[String].flatMap {
                case PLANE_NAME => c.as[Plane]
                case SPHERE_NAME => c.as[Sphere]
            }
        )

    implicit val encodeShape : Encoder[Shape] =
        Encoder.instance {
            case (p: Plane) => withType(p.asJson, PLANE_NAME)
            case (s: Sphere) => withType(s.asJson, SPHERE_NAME)
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
