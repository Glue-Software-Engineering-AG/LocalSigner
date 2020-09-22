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

import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.MainGUI.GuiMode;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import ch.glue.securitytools.pdf.PdfAttacher;
import com.lowagie.text.pdf.PdfReader;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.IOException;

/**
 * This class deletes a page from a PDF document.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class DeletePageListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(DeletePageListener.class);

  private final MainGUI maingui;

  /**
   * Constructor
   * 
   * @param maingui
   *          The main GUI, needed to access the current file to sign and to
   *          display the signed file
   * @param maingui
   *          Parent shell
   */
  public DeletePageListener(final MainGUI maingui)
  {
    this.maingui = maingui;
  }

  @Override
  public void handleEvent(final Event event)
  {
    MenuItem item = (MenuItem) event.widget;
    if (!item.getSelection())
    {
      // deselect menu item, don't do it again!
      return;
    }

    maingui.switchMode(GuiMode.deletePage);
    try
    {
      final PdfReader reader = new PdfReader(maingui.getInputFile());

      final Shell shell = new Shell(maingui.getMainshell(), SWT.TITLE
              | SWT.BORDER | SWT.APPLICATION_MODAL);
      shell.setText(LocalSigner.i18n("deletePage"));
      shell.setLayout(new GridLayout(3, false));

      Label title = new Label(shell, SWT.NONE);
      title.setText(LocalSigner.i18n("deletePageText"));
      title.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

      Label pageLabel = new Label(shell, SWT.NONE);
      pageLabel.setText(LocalSigner.i18n("page"));

      final Text input = new Text(shell, SWT.BORDER);
      input.setFocus();
      GridData gd = new GridData();
      gd.widthHint = 20;
      input.setLayoutData(gd);

      Label totalLabel = new Label(shell, SWT.NONE);
      totalLabel.setText(LocalSigner.i18n("of") + " "
              + reader.getNumberOfPages());

      Composite buttons = new Composite(shell, SWT.NONE);
      buttons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 3,
              1));
      buttons.setLayout(new GridLayout(2, false));

      Button cancel = new Button(buttons, SWT.NONE);
      cancel.setText(LocalSigner.i18n("cancel"));
      cancel.addSelectionListener(new SelectionListener()
      {
        @Override
        public void widgetSelected(SelectionEvent e)
        {
          shell.dispose();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e)
        {
          widgetSelected(e);
        }

      });

      Button delete = new Button(buttons, SWT.NONE);
      delete.setText(LocalSigner.i18n("delete"));
      // delete.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false,
      // 2, 1));
      shell.setDefaultButton(delete);
      delete.addSelectionListener(new SelectionListener()
      {
        @Override
        public void widgetSelected(SelectionEvent e)
        {
          int page = -1;
          try
          {
            page = Integer.parseInt(input.getText());
            if (page < 1)
            {
              page = -1;
            }
            if (page > reader.getNumberOfPages())
            {
              page = -1;
            }
          } catch (NumberFormatException nfe)
          {
            // not a valid number
          }

          if (page == -1)
          {
            Message.warning(shell, LocalSigner.i18n("invalidPageNumber") + ": "
                    + input.getText());
            // show warning
          }
          else
          {
            deletePage(reader, page);
            shell.dispose();
          }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e)
        {
          widgetSelected(e);
        }

      });

      // pack
      shell.pack();

      // size
      final Rectangle parentsize = maingui.getMainshell().getBounds();
      // place in the middle
      final Point boxSize = shell.getSize();
      shell.setBounds(parentsize.x + parentsize.width / 2 - boxSize.x / 2,
              parentsize.y + parentsize.height / 2 - boxSize.y / 2, boxSize.x,
              boxSize.y);
      shell.open();
      Display display = maingui.getMainshell().getDisplay();
      while (!shell.isDisposed())
      {
        if (!display.readAndDispatch())
        {
          display.sleep();
        }
      }
    } catch (IOException e)
    {
      LOGGER.error("Cannot add empty page", e);
    }
  }

  private void deletePage(PdfReader reader, int page)
  {
    LOGGER.debug("Delete page " + page);
    PdfAttacher attacher = new PdfAttacher();
    byte[] output = attacher.deletePage(reader, page);

    maingui.getDocument().getInputFile().setTemporaryFile(output);
    maingui.reloadInputFile(true);
  }

}
