package amaiice.badapple

import java.io.File
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

class FlatFileIO {
    fun compress(rawData: ByteArray) {
        val compressedData = ByteArray(rawData.size)
        val compressor = Deflater()

        compressor.setInput(rawData)
        compressor.finish()

        val compressedDataLength = compressor.deflate(compressedData)
        val result = compressedData.copyOfRange(0, compressedDataLength)
        val output = FileOutputStream(File("output"))
        compressor.end()
        output.write(result)
        output.close()
        output.flush()
    }

    fun open(): ByteArray {
        val compressedData = File("output").readBytes()
        val originalData = ByteArray(200000000)
        val inflater = Inflater()

        inflater.setInput(compressedData)
        val originalDataLength = inflater.inflate(originalData)

        inflater.end()
        return originalData.copyOfRange(0, originalDataLength)
    }
}