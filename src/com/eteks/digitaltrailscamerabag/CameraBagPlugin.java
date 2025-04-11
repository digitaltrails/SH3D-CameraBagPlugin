/*
 * MetaTextPlugin 26 March 2019
 *
 * Sweet Home 3D, Copyright (c) 2019 Michael Hamilton / michael at actrix.gen.nz
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.digitaltrailscamerabag;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.viewcontroller.ContentManager;

public class CameraBagPlugin extends Plugin {

    private ContentManager csvContentManager = null;
    private String lastCsvFilename;

    public CameraBagPlugin() {
        super();
    }

    public ContentManager getCsvContentManager() {
        if (this.csvContentManager == null) {
            this.csvContentManager = new FileContentManager(this.getUserPreferences()) {
                private final String CSV_EXTENSION = ".csv";
                private final FileFilter CSV_FILE_FILTER = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        // Accept directories and ZIP files
                        return file.isDirectory()
                                || file.getName().toLowerCase().endsWith(CSV_EXTENSION);
                    }

                    @Override
                    public String getDescription() {
                        return "csv";
                    }
                };

                @Override
                public String getDefaultFileExtension(ContentType contentType) {
                    if (contentType == ContentType.USER_DEFINED) {
                        return CSV_EXTENSION;
                    } else {
                        return super.getDefaultFileExtension(contentType);
                    }
                }

                @Override
                protected String [] getFileExtensions(ContentType contentType) {
                    if (contentType == ContentType.USER_DEFINED) {
                        return new String [] {CSV_EXTENSION};
                    } else {
                        return super.getFileExtensions(contentType);
                    }
                }

                @Override
                protected FileFilter [] getFileFilter(ContentType contentType) {
                    if (contentType == ContentType.USER_DEFINED) {
                        return new FileFilter [] {CSV_FILE_FILTER};
                    } else {
                        return super.getFileFilter(contentType);
                    }
                }
            };
        }
        return this.csvContentManager;
    }

    @Override
    public PluginAction[] getActions() {
        return new PluginAction[] { new CameraExportAction(this), new CameraImportAction(this) };
    }

    public String askCsvImportFilename(final String title) {
        return this.getCsvContentManager().showOpenDialog(this.getHomeController().getView(),
                title,
                ContentManager.ContentType.USER_DEFINED);
    }

    public String askCsvExportFilename(final String title) {
        this.lastCsvFilename = this.getCsvContentManager().showSaveDialog(
                this.getHomeController().getView(),
                title, 
                ContentManager.ContentType.USER_DEFINED, this.lastCsvFilename != null ? this.lastCsvFilename : "cameras.csv" );
        return this.lastCsvFilename;
    }

}
