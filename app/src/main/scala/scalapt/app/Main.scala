package scalapt.app

import java.awt.image.{BufferedImage, RenderedImage}
import java.io.File
import javax.imageio.ImageIO

import com.typesafe.scalalogging.Logger

import scalapt.core._
import cats.implicits._

class Main(cfg : Config) {

    import Main._
    import Main.logger

    logger.info("Config: " + cfg)

    val w = cfg.width
    val h = cfg.height

    val scene = SceneIO.load(cfg.sceneFile)
    val rdr = new MonteCarloRenderer(w, h, scene)
    val renderData = Frame(cfg.seed, w, h)
    val image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

    def render() : Unit = {

        val ys =
            (0 until cfg.frames)
                .toList
                .traverseU(frameI => {
                    for {
                        seed <- RNG.nextLong
                        rows = render(frameI, seed)
                    } yield rows
                }).runA(Random.xorShift(cfg.seed)).value

        logger.info("Done")

        cfg.imageFile.foreach(file => saveImage(file, image))
    }

    protected def render(frameI : Int, frameSeed : Long) = {
        rdr.render(frameI, frameSeed, (y : Int, rowSeed : Long, cells : Array[SuperSamp]) => {
            val row = renderData.merge(y, cells, frameI)
            writeRow(y, row)
        })
    }

    protected def writeRow(y : Int, row : Frame.Row) = {
        val sy = h - y - 1
        for (sx <- 0 until w) {
            image.setRGB(sx, sy, ColourUtils.colVecToInt(row(sx).clamp))
        }
    }
}

object Main {
    val logger = Logger[Main]

    def main(args : Array[String]) : Unit = {
        Config.parse(args).map(cfg => {
            (if (cfg.display)
                new WndMain(cfg)
            else
                new Main(cfg)
            ).render()
        })
    }

    protected def merge(lhs : Array[SuperSamp], rhs: Array[SuperSamp], n : Int) : Unit = {
        for (i <- lhs.indices) {
            lhs(i) = lhs(i).merge(rhs(i), n)
        }
    }

    protected def saveImage(name : String, image : RenderedImage) : Unit = {
        val file = new File(name)
        val dotPos = name.lastIndexOf('.')
        val format =
            if (dotPos != -1) {
                name.substring(dotPos + 1)
            } else {
                "png"
            }

        logger.info("Saving to file '" + name + "' as format " + format)
        if (!ImageIO.write(image, format, file)) {
            logger.info("ERROR: filename prefix '" + format + " not recognised by ImageIO as a format")
        }
    }
}




