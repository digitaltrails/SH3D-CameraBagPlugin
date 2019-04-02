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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.plugin.PluginAction;


public class CameraExportAction extends PluginAction {

	private final CameraBagPlugin context;

	public CameraExportAction(CameraBagPlugin context) {
		this.context = context;
		putPropertyValue(Property.NAME, Local.str("CameraBag.exportMenuEntry"));
		putPropertyValue(Property.MENU, "Tools");
		setEnabled(true);
	}

	@Override
	public void execute() {
		final Home home = context.getHome();
		try {
			exportCameras(home);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					null, 
					Local.str("CameraBag.exportError", e.getMessage()),
					Local.str("CameraBag.exportDialogTitle"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void exportCameras(final Home home) throws IOException {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setSelectedFile(new File("cameras.csv"));
		chooser.setDialogTitle(Local.str("CameraBag.exportDialogTitle"));
		final int returnValue = chooser.showSaveDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			Path out = Paths.get(chooser.getSelectedFile().getAbsolutePath());
			final List<String> csvTextList = new ArrayList<String>();
			csvTextList.add("#name,x,y,z,pitch,yaw,fov,time");
			for (Camera storedCamera: home.getStoredCameras()) {
				final Instant instant = Instant.ofEpochMilli ( storedCamera.getTime());
				final ZonedDateTime zdt = ZonedDateTime.ofInstant ( instant , ZoneOffset.systemDefault());
				final boolean isObsever = storedCamera instanceof ObserverCamera;
				final String line = 
						storedCamera.getName() + "," +
								storedCamera.getX() + "," +
								storedCamera.getY() + "," +
								storedCamera.getZ() + "," +
								radiansToDegrees(storedCamera.getPitch()) + "," +
								radiansToDegrees(storedCamera.getYaw()) + "," +
								radiansToDegrees(storedCamera.getFieldOfView()) + "," +
								CameraImportAction.TIME_FORMAT.format(zdt) + "," +
								storedCamera.getLens() + "," +
								(isObsever ? "observer" : "topview");
				csvTextList.add(line);
			}
			Files.write(out,csvTextList,Charset.defaultCharset());
			JOptionPane.showMessageDialog(
					null, 
					Local.str(
							"CameraBag.exportSuccess",
							csvTextList.size() - 1), 
					Local.str("CameraBag.importDialogTitle"), 
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private static float radiansToDegrees(float r) {
		return radiansToDegrees((double) r);
	}

	private static float radiansToDegrees(double r) {
		return Math.round(Math.toDegrees(r)) % 360;
	}

}

