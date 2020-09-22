/*
 * Copyright 2020 The Federal Authorities of the Swiss Confederation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.admin.localsigner.main.exception;

import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;

/**
 * Shows an appropriate exception to the user for FileOpenExceptions and FileWriteExceptions.
 *
 * @see FileOpenException
 * @see FileWriteException
 */
public class FileExceptionHandler
{

  /**
   * Shows the user an approprate error message if the file could not be written.
   *
   * <ul>
   * <li>FILE_IS_LOCKED: errorTargetFileIsLocked</li>
   * <li>ACCESS_DENIED: errorTargetFileNotWritable</li>
   * <li>UNSPECIFIED and FILE_NOT_FOUND: errorCannotWrite</li>
   * </ul>
   *
   * @param e The FileWriteException that occured
   */
  public static void showAppropriateErrorMessage(FileWriteException e)
  {
    switch (e.getReason())
    {
      case FILE_IS_LOCKED:
        Message.error(LocalSigner.mainGui.getMainshell(), LocalSigner.i18n("errorTargetFileIsLocked"));
        break;
      case ACCESS_DENIED:
        Message.error(LocalSigner.mainGui.getMainshell(), LocalSigner.i18n("errorTargetFileNotWritable"));
        break;
      default: // case UNSPECIFIED || FILE_NOT_FOUND (may not occur):
        Message.error(LocalSigner.mainGui.getMainshell(), LocalSigner.i18n("errorCannotWrite"));
    }
  }

  /**
   * Shows the user an approprate error message if the file could not be opened.
   * <ul>
   * <li>FILE_IS_LOCKED: errorOpeningFileIsLocked</li>
   * <li>FILE_NOT_FOUND: errorOpeningFileNotExists</li>
   * <li>ACCESS_DENIED: errorOpeningFileAccessDenied</li>
   * <li>UNSPECIFIED: jpanelOpenFileError</li>
   * </ul>
   *
   * @param e The FileOpenException that occured
   * @param filePath The path to the file is used in certain error messages.
   */
  public static void showAppropriateErrorMessage(FileOpenException e, String filePath)
  {
    switch (e.getReason())
    {
      case FILE_IS_LOCKED:
        Message.error(LocalSigner.mainGui.getMainshell(),
            String.format(LocalSigner.i18n("errorOpeningFileIsLocked"), filePath));
        break;
      case FILE_NOT_FOUND:
        Message.error(LocalSigner.mainGui.getMainshell(),
            String.format(LocalSigner.i18n("errorOpeningFileNotExists"), filePath));
        break;
      case ACCESS_DENIED:
        Message.error(LocalSigner.mainGui.getMainshell(), String.
            format(LocalSigner.i18n("errorOpeningFileAccessDenied"), filePath));
        break;
      default: // case UNSPECIFIED
        Message.error(LocalSigner.mainGui.getMainshell(), LocalSigner.i18n("jpanelOpenFileError"));
    }
  }
}
