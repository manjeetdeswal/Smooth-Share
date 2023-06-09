
package com.thenotesgiver.smooth_share.service.web

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.yanzhenjie.andserver.annotation.Controller
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.framework.body.StreamBody
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import com.yanzhenjie.andserver.util.MediaType
import dagger.hilt.EntryPoints
import kotlinx.coroutines.runBlocking
import com.thenotesgiver.smooth_share.GlideApp
import com.thenotesgiver.smooth_share.R
import com.thenotesgiver.smooth_share.activity.SharingActivity
import com.thenotesgiver.smooth_share.content.scan
import com.thenotesgiver.smooth_share.database.model.WebTransfer
import com.thenotesgiver.smooth_share.model.FileModel
import com.thenotesgiver.smooth_share.model.UTransferItem
import com.thenotesgiver.smooth_share.service.web.di.WebEntryPoint
import com.thenotesgiver.smooth_share.service.web.response.FileZipBody
import com.thenotesgiver.smooth_share.service.web.template.Templates
import com.thenotesgiver.smooth_share.service.web.template.renderContents
import com.thenotesgiver.smooth_share.service.web.template.renderHome
import com.thenotesgiver.smooth_share.util.NotificationBackend
import com.thenotesgiver.smooth_share.util.Notifications
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import com.thenotesgiver.smooth_share.framework.io.LeafFile as FilesExt

@Controller
class SharingController {
    @GetMapping("/download/{id}/{dummyName}", produces = ["application/force-download"])
    @ResponseBody
    fun download(context: Context, @PathVariable("id") hashCode: Int): StreamBody {
        val content = getContent(context, hashCode)
        val uri = getUri(content)
        return StreamBody(context.contentResolver.openInputStream(uri))
    }

    @GetMapping("/zip/{id}/{dummyName}", produces = ["application/force-download"])
    @ResponseBody
    fun downloadZip(context: Context, @PathVariable("id") hashCode: Int): com.yanzhenjie.andserver.http.ResponseBody {
        val content = getContent(context, hashCode)

        if (content is FileModel) {
            return FileZipBody(context, content.file)
        }

        throw UnsupportedOperationException("Only file models can be zipped.")
    }

    @GetMapping("/thumbnail/{id}", produces = ["image/png"])
    @ResponseBody
    fun thumbnail(context: Context, @PathVariable("id") hashCode: Int): StreamBody {
        val content = getContent(context, hashCode)
        val bitmap: Bitmap = GlideApp.with(context)
            .asBitmap()
            .load(getUri(content))
            .override(400)
            .submit()
            .get()

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        return StreamBody(ByteArrayInputStream(byteArray), byteArray.size.toLong(), MediaType.IMAGE_PNG)
    }

    @PostMapping("/upload")
    fun upload(context: Context, @RequestParam("content") content: MultipartFile): String {
        val webEntryPoint = EntryPoints.get(context, WebEntryPoint::class.java)
        val services = webEntryPoint.services()
        val fileRepository = webEntryPoint.fileRepository()
        val webDataRepository = webEntryPoint.webDataRepository()
        val fileName = content.filename ?: throw IllegalStateException("File name cannot be empty")
        val type = content.contentType.type
        val savePath = fileRepository.appDirectory
        val uniqueName = FilesExt.getUniqueFileName(context, savePath, fileName)

        val file = FilesExt.createFileWithNestedPaths(context, savePath, null, type, uniqueName, true)
        val inputStream: InputStream = content.stream

        val notification = services.notifications.notifyReceivingOnWeb(file)

        try {
            context.contentResolver.openOutputStream(file.getUri())?.use {
                inputStream.copyTo(it)
            }

            runBlocking {
                try {
                    file.sync(context)

                    // We need to get the ID in order to use it in the UI, so this doesn't use the in!
                    webDataRepository.insert(
                        WebTransfer(0, file.getUri(), file.getName(), file.getType(), file.getLength())
                    )
                    webDataRepository.getReceivedContent(file.getUri())?.let { transfer ->
                        val transferDetail: Intent = Intent(context, SharingActivity::class.java)
                            .setAction(SharingActivity.ACTION_OPEN_WEB_TRANSFER)
                            .putExtra(SharingActivity.EXTRA_WEB_TRANSFER, transfer)

                        notification.setContentIntent(
                            PendingIntent.getActivity(
                                context,
                                transfer.listId.hashCode() + Notifications.REQUEST_CODE_NEUTRAL,
                                transferDetail,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                    }
                } catch (ignored: Throwable) {
                }
            }

            notification.setAutoCancel(true)
                .setChannelId(NotificationBackend.NOTIFICATION_CHANNEL_HIGH)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(context.getString(R.string.received_using_web_share))

            notification.show()

            services.mediaScannerConnection.scan(file)
        } catch (e: Exception) {
            file.delete(context)
        }

        return "redirect:/index.html?uploaded=${content.filename}#uploader"
    }

    @GetMapping("/verify", produces = ["text/plain"])
    @ResponseBody
    fun verify(): String = ((0x76 shl 24) or (0x65 shl 16) or (0x6c shl 8) or 0x69).toString()

    @GetMapping("/")
    fun index(): String {
        return "redirect:/index.html"
    }

    @GetMapping(path = ["/index.html"], produces = ["text/html"])
    @ResponseBody
    fun index(context: Context, @RequestParam("uploaded", required = false) uploadedContent: String): String {
        val webEntryPoint = EntryPoints.get(context, WebEntryPoint::class.java)
        val repository = webEntryPoint.webDataRepository()
        val templates = Templates(context)

        return renderHome(
            templates,
            pageTitle = context.getString(R.string.web_share),
            contentBody = renderContents(templates, repository.getList()),
            uploadedContent = uploadedContent
        )
    }
}

private fun getContent(context: Context, hashCode: Int): Any {
    val webEntryPoint = EntryPoints.get(context, WebEntryPoint::class.java)
    val repository = webEntryPoint.webDataRepository()
    val contents = repository.getList()

    for (content in contents) {
        if (hashCode == content.hashCode()) return content
    }

    throw IllegalArgumentException("Requested an unknown hash code: $hashCode. Check if the content is being served.")
}

private fun getUri(content: Any): Uri {
    return when (content) {
        is FileModel -> content.file.getUri()
        is UTransferItem -> Uri.parse(content.location)
        else -> throw IllegalStateException("An unknown item is provided by the repository.")
    }
}
