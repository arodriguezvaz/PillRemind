package com.andres.pillremind.data.remote

import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de validación del manifiesto de actualizaciones.
 */
class GitHubUpdateDataSourceTest {

    private val validJson = """
        {
          "versionCode": 42,
          "versionName": "1.2.0",
          "minimumVersionCode": 30,
          "apkUrl": "https://github.com/arodriguezvaz/PillRemind/releases/download/v1.2.0/PillRemind-1.2.0.apk",
          "releaseUrl": "https://github.com/arodriguezvaz/PillRemind/releases/tag/v1.2.0"
        }
    """.trimIndent()

    @Test
    fun `descarga y parsea un manifiesto valido`() = runTest {
        var requestedUrl: String? = null
        val dataSource = GitHubUpdateDataSource(
            object : HttpTextFetcher {
                override suspend fun fetch(url: String): String {
                    requestedUrl = url
                    return validJson
                }
            }
        )

        val info = dataSource.fetchLatest()

        assertEquals(
            "https://github.com/arodriguezvaz/PillRemind/releases/latest/download/update.json",
            requestedUrl
        )
        assertEquals(42L, info.versionCode)
        assertEquals("1.2.0", info.versionName)
        assertEquals(30L, info.minimumVersionCode)
    }

    @Test
    fun `ignora campos desconocidos`() {
        val json = validJson.replace("}", """, "notes": "novedades" }""")

        val info = parse(json)

        assertEquals(42L, info.versionCode)
    }

    @Test
    fun `minimumVersionCode ausente se interpreta como cero`() {
        val json = validJson.replace("\"minimumVersionCode\": 30,", "")

        val info = parse(json)

        assertEquals(0L, info.minimumVersionCode)
    }

    @Test
    fun `rechaza una URL que no pertenece al repositorio`() {
        val json = validJson.replace(
            "https://github.com/arodriguezvaz/PillRemind/releases/download/v1.2.0/PillRemind-1.2.0.apk",
            "https://example.com/PillRemind-1.2.0.apk"
        )

        assertThrows(UpdateFormatException::class.java) {
            parse(json)
        }
    }

    @Test
    fun `rechaza un minimo mayor que la version publicada`() {
        val json = validJson.replace("\"minimumVersionCode\": 30", "\"minimumVersionCode\": 50")

        assertThrows(UpdateFormatException::class.java) {
            parse(json)
        }
    }

    @Test
    fun `rechaza un versionCode no positivo`() {
        val json = validJson.replace("\"versionCode\": 42", "\"versionCode\": 0")

        assertThrows(UpdateFormatException::class.java) {
            parse(json)
        }
    }

    @Test
    fun `rechaza un JSON incompleto`() {
        assertThrows(UpdateFormatException::class.java) {
            parse("""{"versionCode": 42}""")
        }
    }

    @Test
    fun `propaga un fallo de red`() = runTest {
        val dataSource = GitHubUpdateDataSource(
            object : HttpTextFetcher {
                override suspend fun fetch(url: String): String = throw IOException("sin red")
            }
        )

        val result = runCatching { dataSource.fetchLatest() }

        assertTrue(result.exceptionOrNull() is IOException)
    }

    private fun parse(json: String) = dataSourceWith(json).parse(json)

    private fun dataSourceWith(json: String) = GitHubUpdateDataSource(
        object : HttpTextFetcher {
            override suspend fun fetch(url: String): String = json
        }
    )
}
