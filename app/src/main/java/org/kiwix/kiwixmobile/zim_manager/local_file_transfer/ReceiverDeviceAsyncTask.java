package org.kiwix.kiwixmobile.zim_manager.local_file_transfer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.kiwix.kiwixmobile.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static org.kiwix.kiwixmobile.zim_manager.local_file_transfer.DeviceListFragment.FILE_TRANSFER_PORT;
import static org.kiwix.kiwixmobile.zim_manager.local_file_transfer.DeviceListFragment.copyToOutputStream;

/**
 * Helper class for the local file sharing module, used in {@link DeviceListFragment}.
 *
 * Once the handshake has successfully taken place, this async-task is used to receive files from
 * the sender device on the FILE_TRANSFER_PORT port. No. of files to be received (and their names)
 * are learnt beforehand during the handshake.
 *
 * A single Task is used for the entire file transfer (the server socket accepts connections as
 * many times as the no. of files).
 * */
class ReceiverDeviceAsyncTask extends AsyncTask<Void, Short, Boolean> {

  private static final String TAG = "ReceiverDeviceAsyncTask";

  private DeviceListFragment deviceListFragment;
  private TransferProgressFragment transferProgressFragment;
  private int fileItemIndex;

  public ReceiverDeviceAsyncTask(DeviceListFragment deviceListFragment, TransferProgressFragment transferProgressFragment) {
    this.deviceListFragment = deviceListFragment;
    this.transferProgressFragment = transferProgressFragment;
  }

  @Override
  protected Boolean doInBackground(Void... voids) {
    try {
      ServerSocket serverSocket = new ServerSocket(FILE_TRANSFER_PORT);
      if(BuildConfig.DEBUG) Log.d(TAG, "Server: Socket opened at " + FILE_TRANSFER_PORT);

      final String KIWIX_ROOT = deviceListFragment.getZimStorageRootPath();

      int totalFileCount = deviceListFragment.getTotalFilesForTransfer();
      for(int currentFile = 1; currentFile <= totalFileCount; currentFile++) {

        Socket client = serverSocket.accept();
        if(BuildConfig.DEBUG) Log.d(TAG, "Server: Client connected for file " + currentFile);

        fileItemIndex = currentFile-1;
        publishProgress(FileItem.SENDING);

        ArrayList<FileItem> fileItems = deviceListFragment.getFileItems();
        String incomingFileName = fileItems.get(fileItemIndex).getFileName();

        final File clientNoteFileLocation = new File(KIWIX_ROOT + incomingFileName);
        File dirs = new File(clientNoteFileLocation.getParent());
        if(!dirs.exists() && !dirs.mkdirs()) {
          Log.d(TAG, "ERROR: Required parent directories couldn't be created");
          return false;
        }

        boolean fileCreated = clientNoteFileLocation.createNewFile();
        if(BuildConfig.DEBUG) Log.d(TAG, "File creation: "+ fileCreated);

        copyToOutputStream(client.getInputStream(), new FileOutputStream(clientNoteFileLocation));

        publishProgress(FileItem.SENT);
        deviceListFragment.incrementTotalFilesSent();
      }
      serverSocket.close();

      return true;  // Returned in case of a succesful file transfer

    } catch (IOException e) {
      Log.e(TAG, e.getMessage());
      return false; // Returned when an error was encountered during transfer
    }
  }

  @Override
  protected void onProgressUpdate(Short... values) {
    short fileStatus = values[0];
    transferProgressFragment.changeStatus(fileItemIndex, fileStatus);
  }

  @Override
  protected void onPostExecute(Boolean allFilesReceived) {
    if(BuildConfig.DEBUG) Log.d(TAG, "File transfer complete");

    if(allFilesReceived) {
      Toast.makeText(deviceListFragment.getActivity(), "File transfer complete", Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(deviceListFragment.getActivity(), "An error was encountered during transfer", Toast.LENGTH_LONG).show();
    }

    ((LocalFileTransferActivity) deviceListFragment.getActivity()).closeLocalFileTransferActivity();
  }
}