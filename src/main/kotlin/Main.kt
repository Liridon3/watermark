package watermark
import java.io.File
import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO
import kotlin.system.exitProcess

open class Image (
    var imageFile: File? = null, var width: Int = 0, var height: Int = 0,
    var components: Int = 0, var colorComponents: Int = 0, var bitsPerPixel: Int = 0, var transparency: String = "") {

    fun watermarkRead (imageFile: File) {
        var image: BufferedImage = ImageIO.read(imageFile)
        var watermarkImage = Image()
        watermarkImage.imageFile = imageFile
        watermarkImage.width = image.width
        watermarkImage.height = image.height
        watermarkImage.components = image.colorModel.numComponents
        watermarkImage.colorComponents = image.colorModel.numColorComponents
        watermarkImage.bitsPerPixel = image.colorModel.pixelSize
        watermarkImage.transparency = when(image.transparency) {1 -> "OPAQUE" 2 -> "BITMASK" else -> "TRANSLUCENT"}

        fun watermarkFilename () {
            println("Input the image filename:")
            var imageFile = File(readln())
            if (!imageFile.exists()) {
                println("The file $imageFile doesn't exist.")
                exit()
            }
            else if (watermarkImage.colorComponents == 3) {
                println("The number of image color components isn't 3.")
                exit()
            }

            watermarkRead(imageFile)
        }
    }
}

fun main(args: Array<String>) {

}

fun origFilename () {
    println("Input the image filename:")
    var imageFile = File(readln())
    if (imageFile.exists()) {
        origImageRead(imageFile)
    }
    else println("The file $imageFile doesn't exist.")
    exit()
}

fun watermarkFilename () {
    println("Input the image filename:")
    var imageFile = File(readln())
    if (!imageFile.exists()) {
        println("The file $imageFile doesn't exist.")
        exit()
    } else if (imageFile.exists()) {
        watermarkRead(imageFile)
        if (imageFile.colorComponents != 3)
    }
}

fun origImageRead (imageFile: File) {
    var image: BufferedImage = ImageIO.read(imageFile)
    var origImage = Image()
    origImage.imageFile = imageFile
    origImage.width = image.width
    origImage.height = image.height
    origImage.components = image.colorModel.numComponents
    origImage.colorComponents = image.colorModel.numColorComponents
    origImage.bitsPerPixel = image.colorModel.pixelSize
    origImage.transparency = when(image.transparency) {1 -> "OPAQUE" 2 -> "BITMASK" else -> "TRANSLUCENT"}

}
fun watermarkRead (imageFile: File): Image {
    var image: BufferedImage = ImageIO.read(imageFile)
    var watermarkImage = Image()
    watermarkImage.imageFile = imageFile
    watermarkImage.width = image.width
    watermarkImage.height = image.height
    watermarkImage.components = image.colorModel.numComponents
    watermarkImage.colorComponents = image.colorModel.numColorComponents
    watermarkImage.bitsPerPixel = image.colorModel.pixelSize
    watermarkImage.transparency = when(image.transparency) {1 -> "OPAQUE" 2 -> "BITMASK" else -> "TRANSLUCENT"}
    return watermarkImage
}

fun weightPercent () {
    println("Input the watermark transparency percentage (Integer 0-100):")
    var percentage = readln().toIntOrNull()
    if (percentage !is Int) {
        println("The transparency percentage isn't an integer number.")
        exit()
    } else if (percentage !in 0..100) {
        println("The transparency percentage is out of range.")
        exit()
    }
}

fun exit () {
    exitProcess(0)
}