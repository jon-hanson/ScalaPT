package scalapt.app

import scopt.Read

case class Config(
    sceneFile : String = "",
    width : Integer = -1,
    height : Integer = -1,
    frames : Integer = -1,
    seed : Long = 0,
    display : Boolean = false,
    imageFile : Option[String] = None,
    framesDir : Option[String] = None
) {
    import Config._

    override def toString: String = {
            EOL +
            "\tsceneFile=" + sceneFile + EOL +
            "\twidth=" + width + EOL +
            "\theight=" + height + EOL +
            "\tframes=" + frames + EOL +
            "\tseed=" + seed + EOL +
            "\tdisplay=" + display + EOL +
            "\timageFile=" + imageFile + EOL +
            "\tframesDir=" + framesDir
    }
}

object Config {
    val EOL = System.lineSeparator()

    implicit val optionRead : Read[Option[String]] = Read.reads { Some(_) }

    val parser = new scopt.OptionParser[Config]("scalpt") {
        head("scalpt", "1.0-SNAPSHOT")

        opt[String]('i', "sceneFile")
            .required()
            .valueName("filename")
            .action((x, cfg) => cfg.copy(sceneFile = x))
            .text("input scene description file")

        opt[Int]('w', "width")
            .required()
            .valueName("pixels")
            .action((x, cfg) => cfg.copy(width = x))
            .text("image width")

        opt[Int]('h', "height")
            .required()
            .valueName("pixels")
            .action((x, cfg) => cfg.copy(height = x))
            .text("image height")

        opt[Int]('n', "frames")
            .required()
            .valueName("count")
            .action((x, cfg) => cfg.copy(frames = x))
            .text("number of frames to render")

        opt[Long]('s', "seed")
            .required()
            .valueName("number")
            .action((x, cfg) => cfg.copy(seed = x))
            .text("random number seed")

        opt[Unit]('d', "display")
            .action((_, cfg) => cfg.copy(display = true))
            .text("display the image as it renders")

        opt[Option[String]]('o', "imageFile")
            .valueName("filename")
            .action((x, cfg) => cfg.copy(imageFile = x))
            .text("image output file name")

        opt[Option[String]]('f', "framesDir")
            .valueName("filename")
            .action((x, cfg) => cfg.copy(framesDir = x))
            .text("frame output directory")
    }

    def parse(args : Array[String]) : Option[Config] =
        parser.parse(args, Config())
}