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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Camera.Lens;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.plugin.PluginAction.Property;


public class CameraImportAction extends PluginAction {

	private static final int NUMBER_OF_COLUMNS = 11;
	private final CameraBagPlugin context;

	public CameraImportAction(CameraBagPlugin context) {
		this.context = context;
		putPropertyValue(Property.NAME, Local.str("CameraBag.importMenuEntry"));
		putPropertyValue(Property.MENU, Local.str("CameraBag.targetMenu"));
		setEnabled(true);
	}

	@Override
	public void execute() {
		final Home home = context.getHome();
		importCameras(home);

	}

	public void importCameras(final Home home) {

		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setSelectedFile(new File("cameras.csv"));
		chooser.setDialogTitle(Local.str("CameraBag.importDialogTitle"));
		final int returnValue = chooser.showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			final List<String> lines;

			Path inPath = Paths.get(chooser.getSelectedFile().getAbsolutePath());
			try {
				System.out.println("import from: " + inPath.toAbsolutePath());
				lines = Files.readAllLines(inPath, Charset.defaultCharset());
			} 
			catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(
						null, 
						Local.str("CameraBag.importIOError", e.getMessage()),
						Local.str("CameraBag.importDialogTitle"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			final Map<String, Camera> camerasByName = new LinkedHashMap<String, Camera>();

			for (Camera camera: home.getStoredCameras()) {
				camerasByName.put(camera.getName(),camera);
			}

			long lineCount = 0;
			for (String line: lines) {
				lineCount++;
				String name = "'not named'";
				try {
					if (!line.startsWith("#")) {
						final String[] values = line.split(",");
						if (values.length == NUMBER_OF_COLUMNS) {
							final float x, y, z;
							final float pitch, yaw, fov;
							int i = 0;
							name = values[i++];
							x = Float.parseFloat(values[i++]);
							y = Float.parseFloat(values[i++]);
							z = Float.parseFloat(values[i++]);
							pitch = degreesToRadians(Float.parseFloat(values[i++]));
							yaw = degreesToRadians(Float.parseFloat(values[i++]));
							fov = degreesToRadians(Float.parseFloat(values[i++]));
							final ZonedDateTime zdt = ZonedDateTime.parse(values[i++]);

							// Convert local zone date-time back to UTC and then extract the millis
							// The javadoc states that the conversion only happens "if possible",
							// hopefully UTC must always be possible.
							final long time = Instant.from(zdt.withZoneSameLocal(ZoneOffset.UTC)).toEpochMilli();
							System.out.println(name + " time=" + zdt);

							final Lens lens = Camera.Lens.valueOf(values[i++]);		

							final boolean isObserverCamera = values[i++].startsWith("observer");
							final Camera camera = isObserverCamera ? new ObserverCamera(x, y, z, yaw, pitch, fov) : new Camera(x, y, z, yaw, pitch, fov);

							if (isObserverCamera) {
								((ObserverCamera) camera).setFixedSize(values[i++].equals("fixedSize"));
							}
							camera.setName(name);
							camera.setTime(time);
							camera.setLens(lens);
							camerasByName.put(name, camera);
						} 
						else {
							JOptionPane.showMessageDialog(
									null, 
									Local.str("CameraBag.importNumColError", lineCount, NUMBER_OF_COLUMNS, values.length),
									Local.str("CameraBag.importDialogTitle"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				catch (NumberFormatException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							null, 
							Local.str("CameraBag.importNumericError", lineCount, name, e.getMessage()),
							Local.str("CameraBag.importDialogTitle"),
							JOptionPane.ERROR_MESSAGE);
				}
				catch (DateTimeParseException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							null, 
							Local.str("CameraBag.importTimeError", lineCount, name, e.getMessage()),
							Local.str("CameraBag.importDialogTitle"),
							JOptionPane.ERROR_MESSAGE);
				}

			}

			final ArrayList<Camera> storedCameras = new ArrayList<Camera>(camerasByName.values());
			if (storedCameras.size() > context.getUserPreferences().getStoredCamerasMaxCount()) {
				int choice = JOptionPane.showConfirmDialog(null, 
						Local.str(
								"CameraBag.importIgnoreMaxCameraLimit", 
								context.getUserPreferences().getStoredCamerasMaxCount()), 
						Local.str("CameraBag.importDialogTitle"),
						JOptionPane.YES_NO_CANCEL_OPTION);

				if (choice == JOptionPane.YES_OPTION) {
					home.setStoredCameras(storedCameras);
				}
				else if (choice == JOptionPane.NO_OPTION){
					home.setStoredCameras(new ArrayList<Camera>(storedCameras.subList(0, 50)));
					JOptionPane.showMessageDialog(
							null, 
							Local.str(
									"CameraBag.importedTooManyCameras", 
									context.getUserPreferences().getStoredCamerasMaxCount()), 
							Local.str("CameraBag.importDialogTitle"), 
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				home.setStoredCameras(storedCameras);
				JOptionPane.showMessageDialog(
						null, 
						Local.str(
								"CameraBag.importSuccess",
								storedCameras.size()), 
						Local.str("CameraBag.importDialogTitle"), 
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	private static float degreesToRadians(float d) {
		return (float) ((double) d * (Math.PI / 180.0));
	}
	
}

