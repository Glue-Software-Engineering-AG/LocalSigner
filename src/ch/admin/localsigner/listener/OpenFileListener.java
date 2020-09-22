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
package ch.admin.localsigner.listener;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfReader;
import ch.admin.localsigner.main.TempFilesCleanerUtil;

/**
 * Listener to load a temporary file
 *
 * @author Rafel Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class OpenFileListener implements Listener
{

  private static final Logger LOGGER = Logger.getLogger(OpenFileListener.class);

  private final HashMap<TreeItem, OpenableFile> files;

  private final Tree tree;

  public OpenFileListener(Tree tree)
  {
    this.tree = tree;
    this.files = new HashMap<TreeItem, OpenableFile>();
  }

  @Override
  public void handleEvent(Event event)
  {
    try
    {
      Point point = new Point(event.x, event.y);

      OpenableFile of = files.get(tree.getItem(point));

      if (of == null)
      {
        // no action for this item
        return;
      }

      File tmp = createTempFile(of);

      LOGGER.debug("Open " + tmp.getAbsolutePath());
      Desktop.getDesktop().open(tmp);
    } catch (IOException e)
    {
      LOGGER.error("Cannot create temp file", e);
    }
  }

  private File createTempFile(OpenableFile of) throws IOException
  {
    String prefix = TempFilesCleanerUtil.TEMP_FILE_PREFIX + "file";
    String suffix = "_" + of.getName();
    if (!of.getType().isAttachment())
    { // attachments have the file type suffix (.pdf, .zip etc.) as part of their name. Others need it added
      suffix += of.getType().getSuffix();
    }
    File tmp = File.createTempFile(prefix, suffix);
    tmp.deleteOnExit();

    fillTempFile(tmp, of.getData());

    return tmp;
  }

  private void fillTempFile(File tmp, Object data) throws IOException
  {
    FileOutputStream fos = null;

    try
    {
      fos = new FileOutputStream(tmp);

      if (data instanceof byte[])
      {
        IOUtils.write((byte[]) data, fos);
      } else if (data instanceof InputStream)
      {
        IOUtils.copy((InputStream) data, fos);
      } else if (data instanceof PRStream)
      {
        IOUtils.write(PdfReader.getStreamBytes((PRStream) data), fos);
      } else
      {
        LOGGER.warn("Unknown object to open: " + data.getClass());
        throw new IOException("Not able to copy data of a class of type " + data.getClass());
      }
    } finally
    {
      try
      {
        if (fos != null)
        {
          fos.close();
        }
      } catch(Exception closeEx)
      {
        // What a bummer.
      }
    }
  }

  public void register(TreeItem item, String name, OpenableFile.FileType type, Object data)
  {
    files.put(item, new OpenableFile(item, name, type, data));
  }

  public static class OpenableFile
  {

    public enum FileType
    {
      PDF(".pdf"),
      CERT(".crt"),
      ATTACHMENT(null);

      private final String suffix;

      private FileType(String suffix)
      {
        this.suffix = suffix;
      }

      public String getSuffix()
      {
        return suffix;
      }

      public boolean isAttachment()
      {
        return this.equals(ATTACHMENT);
      }
    }

    final private TreeItem item;

    final private String name;

    final private FileType type;

    final private Object data;

    public OpenableFile(TreeItem item, String name, FileType type, Object data)
    {
      this.item = item;
      this.name = name;
      this.type = type;
      this.data = data;
    }

    public TreeItem getItem()
    {
      return item;
    }

    public String getName()
    {
      return name;
    }

    public FileType getType()
    {
      return type;
    }

    public Object getData()
    {
      return data;
    }

    /**
     * only considers the value of the TreeItem to be able to locate the FileType inside a list when searching for the
     * item
     *
     * @return returns true if the <b>TreeItem</b> is equal.
     */
    @Override
    public boolean equals(Object obj)
    {
      if (item == null && obj == null)
      { // both are null: ok
        return true;
      }
      else if (obj == null)
      { // only obj is null: not ok
        return false;
      }
      else if (item == null)
      { // only item is null: not ok
        return false;
      }
      else if (obj.getClass() != item.getClass())
      { // classes are different
        return false;
      }
      else
      { //
        return item.equals(obj);
      }
    }
  }
}
