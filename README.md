# SH3D-CameraBagPlugin
## CameraBagPlugin for SweetHome3D - Export/Import Cameras (Points of View)

The SweetHome3D CameraBagPlugin (as in bag for carrying cameras) adds two menu items to the tools menu:
- Export Cameras - exports all saved points of view to a CSV file
- Import Cameras - imports points of view from a CSV file, possible overwriting existing ones with the same name

The CSV files have a self describing header with the column names:
```
name,
x,y,z,
pitch,
yaw,
fov,
time,
cameraType,
viewType,
* observerSizeType
```
Of those listed the time (date-time) is most useful. I use it to quickly generate multiple versions of the same viewpoint for different days of the year (in order to examine sun/shading issues). The date-time is exported for the local zone. Fov (field of view) is in degrees.

On import either comma or semicolon delimited CVS is accepted.  The CSV parser performs a simple split on the delimiter, it does not accept escaped delimiters

Being a new plugin it might be wise to initially treat with some caution - for example, you could refrain from saving a model that you've imported cameras into (that would be very safe).

## Acknowledgements

 - [xlance](https://sourceforge.net/u/xlance/profile/) for validation enhancements
   and the related [Camera Data Generator](https://sourceforge.net/p/sweethome3d/plug-ins/55/)
 - [Olivier](https://sourceforge.net/u/z56oo/profile/) for assistance with internationalization, fixes, and testing.

## Requirements:
- [SweetHome3D](https://www.sweethome3d.com/) > 1.5
- Java/JDK >= 1.8

## Development:
- See [SweetHome3D - Plug-in developer's guide](https://www.sweethome3d.com/pluginDeveloperGuide.jsp)

## SourceForge SweetHome3D plugin submission link

[https://sourceforge.net/p/sweethome3d/plug-ins/25/](https://sourceforge.net/p/sweethome3d/plug-ins/25/)
