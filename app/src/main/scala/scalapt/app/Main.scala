package scalapt.app

import java.awt.event.{MouseAdapter, MouseEvent, WindowAdapter, WindowEvent}
import java.awt.image.{BufferedImage, RenderedImage}
import java.awt.{Frame => JFrame, Color, Dimension, Graphics, Graphics2D, RenderingHints}
import java.io.File
import javax.imageio.ImageIO

import com.typesafe.scalalogging.Logger

import scalapt.core._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import cats.implicits._
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task

class Main(
        frameTitle : String,
        val initialSeed : Long,
        val w : Int,
        val h : Int,
        val inFile : String,
        val frames : Int,
        val outFile : Option[File],
        var closing : Boolean = false
) extends JFrame(frameTitle) {

    import Main._
    import Main.logger

    logger.info("Scene: " + inFile)
    logger.info("Width: " + w)
    logger.info("Height: " + h)
    logger.info("Frames: " + frames)
    logger.info("Seed: " + initialSeed)
    outFile.foreach(name => logger.info("Outfile: " + name))

    val scene = SceneIO.load(inFile)

    pack()

    val ins = getInsets
    val dim = new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom)
    setSize(dim)
    setResizable(false)
    addWindowListener(new WindowAdapter() {
        override def windowClosing(we : WindowEvent) = {
            closing = true
            dispose()
        }
    })

    addMouseListener(new MouseAdapter() {
        override def mouseClicked(me : MouseEvent) = {
            val sx = me.getX
            val sy = me.getY
            val x = sx - ins.left + 1
            val y = h - (sy - ins.top) - 3

            val ss = rdr.render(x, y).runA(Random.xorShift(0)).value

            logger.info(x + " : " + y + " -> " + ss)
        }
    })

    setLocationRelativeTo(null)
    setBackground(Color.RED)
    setVisible(true)

    val rdr = new MonteCarloRenderer(w, h, scene)

    val renderData = Frame(w, h)

    val image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

    val gr2d = image.getGraphics
    gr2d.setColor(Color.BLUE)
    gr2d.drawRect(0, 0, w-1, h-1)
    repaint(ins.left, ins.top, w, h)

    val ys =
        (0 until frames)
            .toList
            .traverseU(frameI => {
                for {
                    seed <- RNG.nextLong
                    rows = render(frameI, seed)
                } yield rows
            }).runA(Random.xorShift(initialSeed)).value

    logger.info("Done")

    outFile.foreach(file => saveImage(file, image))

    private def saveImage(file : File, image : RenderedImage) : Unit = {
        val name = file.getName
        val dotPos = name.lastIndexOf('.')
        val format =
            if (dotPos != -1) {
                name.substring(dotPos + 1)
            } else {
                "png"
            }
        logger.info("Saving to file '" + name + "' as format " + format)
        if (!ImageIO.write(image, format, file)) {
            logger.info("ERROR: filename prefix '" + format + " not recognised as a format")
        }
    }

    private def render(frameI : Int, frameSeed : Long) = {
        if (!closing) {
            logger.info("Frame " + frameI)

            val tasks =
                (0 until rdr.height)
                    .toList
                    .traverseU(y => for {ySeed <- RNG.nextLong} yield (y, ySeed))
                    .runA(Random.xorShift(frameSeed))
                    .value
                    .map({case (y, seed) =>
                        Task({
                            val cells = rdr.render(y) //work(rdr.width, y)
                                .runA(Random.xorShift(seed))
                                .value
                                .toArray
                            val row = renderData.merge(y, cells, frameI)
                            displayRow(y, row)
                        })
                    })
            Await.result(Task.gather(tasks).runAsync, 60.minutes)
        }
    }

    private def displayRow(y : Int, row : Frame.Row) = {
        val sy = h - y - 1
        for (sx <- 0 until w) {
            image.setRGB(sx, sy, colVecToInt(row(sx).clamp))
        }

        repaint(ins.left, ins.top + sy, w, 1)
    }

    override def paint(graphics : Graphics) = {
        val g2d = graphics.asInstanceOf[Graphics2D]
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        g2d.drawImage(image, ins.left, ins.top, null)
    }
}

object Main {
    val logger = Logger[Main]

    def main(args : Array[String]) = {
        val inFile = if (args.length > 0) args(0) else "scenes/cornell2.json"
        val seed = if (args.length > 1) Integer.parseInt(args(1)) else 0
        val width = if (args.length > 1) Integer.parseInt(args(2)) else 1024
        val height = if (args.length > 2) Integer.parseInt(args(3)) else 768
        val frames = if (args.length > 3) Integer.parseInt(args(4)) else 1024
        val outFile = if (args.length > 4) Option(new File(args(5))) else Option.empty

        val frame = new Main("ScalaPT", seed, width, height, inFile, frames, outFile)
    }

    private def merge(lhs : Array[SuperSamp], rhs: Array[SuperSamp], n : Int) {
        for (i <- lhs.indices) {
            lhs(i) = lhs(i).merge(rhs(i), n)
        }
    }

    private def colVecToInt(colour : RGB) : Int = {
        colDblToInt(colour.blue) |
            (colDblToInt(colour.green) << 8) |
            (colDblToInt(colour.red) << 16)
    }

    private def colDblToInt(d : Double) : Int = {
        val i = MathUtil.gammaCorr(d)
        val j = i * 255.0 + 0.5
        MathUtil.clamp(j, 0, 255).toInt
    }
}
