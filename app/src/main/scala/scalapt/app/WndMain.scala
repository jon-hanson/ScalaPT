package scalapt.app

import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.{Color, Dimension, Graphics, Graphics2D, RenderingHints, Frame => JFrame}

import scalapt.core.Frame

class WndMain(cfg : Config) extends Main(cfg)  {

    var closing : Boolean = false

    val wnd = new JFrame("ScalaPT") {

        pack()

        val ins = getInsets
        val dim = new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom)
        setSize(dim)
        setResizable(false)
        addWindowListener(new WindowAdapter() {
            override def windowClosing(we: WindowEvent) = {
                closing = true
                dispose()
            }
        })

        setLocationRelativeTo(null)
        setBackground(Color.RED)
        setVisible(true)

        val gr2d = image.getGraphics
        gr2d.setColor(Color.BLUE)
        gr2d.drawRect(0, 0, w - 1, h - 1)
        repaint(ins.left, ins.top, w, h)

        override def paint(graphics : Graphics) = {
            val g2d = graphics.asInstanceOf[Graphics2D]
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
            g2d.drawImage(image, ins.left, ins.top, null)
        }
    }

    val ins = wnd.getInsets

    override protected def isClosing() : Boolean = {
        closing
    }

    override protected def writeRow(y : Int, row : Frame.Row) : Unit = {
        val sy = h - y - 1
        super.writeRow(y, row)

        wnd.repaint(ins.left, ins.top + sy, w, 1)
    }
}
