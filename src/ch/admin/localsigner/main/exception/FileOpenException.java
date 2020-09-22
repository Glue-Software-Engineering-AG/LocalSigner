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
 * Allows a better distinction of the reasons why the file could not be opened.
 * <br/><br/>
 * Use FileExceptionHandler to show an appropriate error message.
 *
 * @see FileExceptionHandler
 */
public class FileOpenException extends AbstractFileException
{

  public FileOpenException(String message, Reason reason)
  {
    super(message, reason);
  }

  public FileOpenException(Throwable throwable, Reason reason)
  {
    super(throwable, reason);
  }
}