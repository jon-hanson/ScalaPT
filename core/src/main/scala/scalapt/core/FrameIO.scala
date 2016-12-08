package scalapt.core

import java.io.File

import com.sksamuel.avro4s._
import com.typesafe.scalalogging.Logger

object FrameIO {
    val logger = Logger[FrameIO.type ]

    val EXTN = ".avro"

    def save(frame : Frame, fileName : String) : Unit = {
        save(frame, new File(fileName))
    }

    def save(frame : Frame, file : File) : Unit = {
        logger.info("Writing frame to " + file)
        val os = AvroOutputStream.data[Frame](file)
        os.write(frame)
        os.close()
    }

    def load(fileName : String) : Frame = {
        logger.info("Loading frame from " + fileName)
        val is = AvroInputStream.data[Frame](new File(fileName))
        val frame = is.iterator.next
        is.close()
        frame
    }
}
