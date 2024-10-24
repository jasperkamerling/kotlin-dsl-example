package org.example

import t.dsl.config

fun main() {
    val config = config {
        onlineImage {
            url = "https://openjdk.org/images/duke-thinking.png"
        }
        fileSystemImage {
            filename = "/home/jasper/duke-plug.png"
        }
    }

    println("number of images: ${config.images.size}")

    for (image in config.images) {
        println("${image.name}: ${image.contents.size} bytes")
    }
}
