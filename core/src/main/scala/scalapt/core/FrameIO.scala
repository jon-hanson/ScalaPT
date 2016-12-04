package scalapt.core

import java.io.File

import com.sksamuel.avro4s._

object FrameIO {

    def save(frame : Frame, fileName : String) : Unit = {
        val os = AvroOutputStream.data[Frame](new File(fileName))
        os.write(frame)
        os.close()
    }

    def load(fileName : String) : Frame = {
        val is = AvroInputStream.data[Frame](new File(fileName))
        val frame = is.iterator.next
        is.close()
        frame
    }
}
