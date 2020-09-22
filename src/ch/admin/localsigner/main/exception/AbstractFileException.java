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

/**
 * Allows a better distinction of the reasons why the file could not be read or written.
 * FileOpenException and FileWriteException extend this class.
 * <br/><br/>
 * Use FileExceptionHandler to show an appropriate error message.
 *
 * @see FileOpenException
 * @see FileWriteException
 *
 * @see FileExceptionHandler
 */
public abstract class AbstractFileException extends Exception
{

  private Reason reason;

  public enum Reason
  {
    UNSPECIFIED, FILE_IS_LOCKED, FILE_NOT_FOUND, ACCESS_DENIED
  }

  public Reason getReason()
  {
    return reason;
  }

  public AbstractFileException(String message, Reason reason)
  {
    super(message);
    this.reason = reason;
  }

  /**
   * If the Throwable contains a StackTraceElement with 'ChannelInputStream' in its name, <code>FILE_IS_LOCKED</code> is
   * always assumed as Reason regardless of the value in <i>reason</i>.
   */
  public AbstractFileException(Throwable throwable, Reason reason)
  {
    super(throwable);

    for (StackTraceElement ste : throwable.getStackTrace())
    {
      if (ste.getClassName().contains("ChannelInputStream"))
      {
        this.reason = Reason.FILE_IS_LOCKED;
        return;
      }
    }
    this.reason = reason;
  }
}
