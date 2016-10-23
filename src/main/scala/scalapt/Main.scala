package scalapt

import java.awt._
import java.awt.{Frame => JFrame}
import java.awt.event.{MouseAdapter, MouseEvent, WindowAdapter, WindowEvent}
import java.awt.image.{BufferedImage, RenderedImage}
import java.io.File
import java.time.LocalDateTime
import javax.imageio.ImageIO

import com.typesafe.scalalogging.Logger

class Main(
   frameTitle : String,
   val w : Int,
   val h : Int,
   val inFile : String,
   val frames : Int,
   val outFile : Option[File],
   var closing : Boolean = false
) extends JFrame(frameTitle) {

    import Main.logger

    logger.info("Scene: " + inFile)
    logger.info("Width: " + w)
    logger.info("Height: " + h)
    logger.info("Frames: " + frames)
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

            System.out.print(x + " : " + y + " -> ")

            val ss = rdr.render(x, y).runA(Random.randDouble(x+y*rdr.width)).value
            logger.info(ss.toString)
        }
    })

    setLocationRelativeTo(null)
    setBackground(Color.BLACK)
    setVisible(true)

    val rdr = new MonteCarloRenderer(w, h, scene)

    val renderData = Frame(w, h)

    val image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

    val gr2d = image.getGraphics
    gr2d.setColor(Color.RED)
    gr2d.drawRect(0, 0, w-1, h-1)
    repaint(ins.left, ins.top, w, h)

    for (i <- 0 until frames) {
        if (!closing) {
            render(i)
        }
    }

    logger.info(LocalDateTime.now() + " : Done")

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

    private def render(frameI : Int) = {
        logger.info(LocalDateTime.now() + " : Frame " + frameI)
        ConcurrentUtils.parallelFor (0 until rdr.height) { y =>
            val seed = (y * rdr.width) * (frameI + 1)
            val row = rdr.render(y).runA(Random.randDouble(seed)).value

            val mergedRow =
                if (frameI == 0) {
                    renderData.data(y) = row
                    row
                } else {
                    renderData.merge(y, row, frameI)
                }

            val sy = h - y - 1
            for (sx <- 0 until w) {
                image.setRGB(sx, sy, colVecToInt(mergedRow(sx).clamp))
            }

            repaint(ins.left, ins.top + sy, w, 1)
        }
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

    override def paint(graphics : Graphics) = {
        val g2d = graphics.asInstanceOf[Graphics2D]
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        g2d.drawImage(image, ins.left, ins.top, null)
    }
}

object Main {
    val logger = Logger[Main]

    def main(args : Array[String]) : Unit = {
        val inFile = if (args.length > 0) args(0) else "scenes/cornell2.json"
        val width = if (args.length > 1) Integer.parseInt(args(1)) else 1024
        val height = if (args.length > 2) Integer.parseInt(args(2)) else 768
        val frames = if (args.length > 3) Integer.parseInt(args(3)) else 1024
        val outFile = if (args.length > 4) Option(new File(args(4))) else Option.empty

        val frame = new Main("ScalaPT", width, height, inFile, frames, outFile)
    }
}
