package t.dsl

import org.example.dsl.ConfigMarker
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.example.model.Config
import org.example.model.Image

fun config(init: @ConfigMarker ConfigBuilder.() -> Unit): Config {
    val builder = ConfigBuilder()
    init(builder)

    return builder.build()
}

class ConfigBuilder {
    private val images = mutableListOf<Image>()

    fun onlineImage(init: OnlineImageBuilder.() -> Unit) {
        val builder = OnlineImageBuilder()
        builder.init()

        images += builder.build()
    }

    fun fileSystemImage(init: FileSystemImageBuilder.() -> Unit) {
        val builder = FileSystemImageBuilder()
        builder.init()

        images += builder.build()
    }

    @ConfigMarker
    class OnlineImageBuilder {
        var url: String? = null

        fun build(): Image =
            OnlineImage(checkNotNull(url) { "no URL specified" })

        private class OnlineImage(private val url: String) : Image {
            override val contents: ByteArray

            init {
                HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build().use { httpClient ->
                    val request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build()

                    contents = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()).body()
                }
            }

            override val name: String
                get() = url.substringAfterLast("/")
        }
    }

    @ConfigMarker
    class FileSystemImageBuilder() {
        var filename: String? = null

        fun build(): Image =
            FileSystemImage(checkNotNull(filename) { "no filename specified" })

        private class FileSystemImage(private val filename: String) : Image {
            override val contents: ByteArray

            init {
                FileInputStream(filename).use { `in` ->
                    contents = `in`.readAllBytes()
                }
            }

            override val name: String
                get() = filename.substringAfterLast(File.separator)
        }
    }

    fun build() =
        Config(images)
}
