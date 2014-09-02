package com.ldaniels528.verify.io

import java.io._
import java.util.zip._

import com.ldaniels528.verify.util.VxUtils._
import org.apache.commons.io.IOUtils

import scala.util.{Failure, Success, Try}

/**
 * Adds compression/decompression capability to implementing classes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait Compression {

  /**
   * Compresses the message 
   * @param message the given message
   * @return the compressed copy of the input message
   */
  def compress(message: String, encoding: String): Try[Array[Byte]] = compress(message.getBytes(encoding))

  /**
   * Compresses the message data
   * @param data the given message data
   * @return the compressed copy of the input data
   */
  def compress(data: Array[Byte]): Try[Array[Byte]] = Try {
    val memStream = new ByteArrayOutputStream(data.length)
    new GZIPOutputStream(memStream) {
      `def`.setLevel(Deflater.BEST_COMPRESSION)
    } use { gzos =>
      gzos.write(data)
      gzos.finish()
      memStream.toByteArray
    }
  }

  /**
   * Attempts to decompress the message however, if the decompression fails
   * the original message will be returned.
   */
  def deflate(message: Array[Byte]): Array[Byte] = {
    decompress(message) match {
      case Success(decompressed) => decompressed
      case Failure(e) => message
    }
  }

  /**
   * Decompresses the message data
   * @param compressedData the given message data
   * @return the decompressed copy of the input data
   */
  def decompress(compressedData: Array[Byte]): Try[Array[Byte]] = Try {
    val out = new ByteArrayOutputStream(compressedData.length * 3)
    new GZIPInputStream(new ByteArrayInputStream(compressedData)) use { in =>
      IOUtils.copy(in, out)
      out.toByteArray
    }
  }

}

/**
 * Compression Companion Object
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Compression extends Compression {
  val self = this

  /**
   * Syntactic sugar for compressing/decompressing byte arrays
   */
  implicit class ByteCompressionHelper(data: Array[Byte]) {

    def compress = self.compress(data)

    def decompress = self.decompress(data)

  }

  /**
   * Syntactic sugar for compressing/decompressing strings
   */
  implicit class StringCompressionHelper(data: String) {

    def compress(encoding: String = "UTF8") = self.compress(data, encoding)

  }

}