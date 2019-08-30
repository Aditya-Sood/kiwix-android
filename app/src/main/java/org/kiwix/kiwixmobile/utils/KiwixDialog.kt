package org.kiwix.kiwixmobile.utils

import org.kiwix.kiwixmobile.R
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.adapter.BooksOnDiskListItem.BookOnDisk

sealed class KiwixDialog(
  val title: Int?,
  val message: Int,
  val positiveMessage: Int,
  val negativeMessage: Int
) {

  data class DeleteZim(override val args: List<Any>) : KiwixDialog(
    null, R.string.delete_zim_body, R.string.delete, R.string.no
  ), HasBodyFormatArgs {
    constructor(bookOnDisk: BookOnDisk) : this(listOf(bookOnDisk.book.title))
  }

  object LocationPermissionRationale : KiwixDialog(
    null, R.string.permission_rationale_location, android.R.string.yes, android.R.string.cancel
  )

  object StoragePermissionRationale : KiwixDialog(
    null, R.string.request_storage, android.R.string.yes, android.R.string.cancel
  )

  object EnableWifiP2pServices : KiwixDialog(
    null, R.string.request_enable_wifi, R.string.yes, android.R.string.no
  )

  object EnableLocationServices : KiwixDialog(
    null, R.string.request_enable_location, R.string.yes, android.R.string.no
  )

  data class FileTransferConfirmation(override val args: List<Any>) : KiwixDialog(
    null, R.string.transfer_to, R.string.yes, android.R.string.cancel
  ), HasBodyFormatArgs {
    constructor(selectedPeerDeviceName: String) : this(listOf(selectedPeerDeviceName))
  }

  open class YesNoDialog(
    title: Int,
    message: Int
  ) : KiwixDialog(title, message, R.string.yes, R.string.no) {
    object StopDownload : YesNoDialog(
      R.string.confirm_stop_download_title, R.string.confirm_stop_download_msg
    )

    object WifiOnly : YesNoDialog(
      R.string.wifi_only_title, R.string.wifi_only_msg
    )
  }
}

interface HasBodyFormatArgs {
  val args: List<Any>
}
