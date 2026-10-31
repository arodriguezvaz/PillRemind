package com.andres.pillremind.data.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Pruebas del empaquetado ZIP de la copia de seguridad.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BackupArchiveTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `escribe y lee el json y las imagenes`() {
        val image = temporaryFolder.newFile("foto.jpg")
        image.writeBytes(byteArrayOf(1, 2, 3, 4))
        val json = """{"format":"pillremind-backup"}"""

        val output = ByteArrayOutputStream()
        BackupArchive.write(
            output = output,
            json = json,
            images = mapOf("images/img_0001.jpg" to image)
        )

        val contents = BackupArchive.read(ByteArrayInputStream(output.toByteArray()))

        assertEquals(json, contents.json)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), contents.images.getValue("images/img_0001.jpg"))
    }

    @Test
    fun `rechaza una entrada fuera de la estructura`() {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(BackupArchive.JSON_ENTRY))
            zip.write("{}".toByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("../evil.txt"))
            zip.write("malicioso".toByteArray())
            zip.closeEntry()
        }

        assertThrows(BackupException::class.java) {
            BackupArchive.read(ByteArrayInputStream(output.toByteArray()))
        }
    }

    @Test
    fun `rechaza una copia sin backup json`() {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("images/img_0001.jpg"))
            zip.write(byteArrayOf(1))
            zip.closeEntry()
        }

        assertThrows(BackupException::class.java) {
            BackupArchive.read(ByteArrayInputStream(output.toByteArray()))
        }
    }

    @Test
    fun `asigna rutas de imagen deterministas`() {
        val first = temporaryFolder.newFile("primera.png")
        val second = temporaryFolder.newFile("segunda.jpg")
        val store = BackupImageStore(
            RuntimeEnvironment.getApplication()
        )

        val paths = store.buildArchivePaths(
            linkedMapOf(
                first.absolutePath to first,
                second.absolutePath to second
            )
        )

        assertEquals("images/img_0001.png", paths[first.absolutePath])
        assertEquals("images/img_0002.jpg", paths[second.absolutePath])
    }
}
