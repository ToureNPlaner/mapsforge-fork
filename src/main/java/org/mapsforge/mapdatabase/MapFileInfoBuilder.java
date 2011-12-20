/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.mapdatabase;

import org.mapsforge.core.BoundingBox;
import org.mapsforge.core.GeoPoint;
import org.mapsforge.core.Tag;

class MapFileInfoBuilder {
	BoundingBox boundingBox;
	String commentText;
	long fileSize;
	int fileVersion;
	boolean hasStartPosition;
	boolean isDebugFile;
	String languagePreference;
	long mapDate;
	byte numberOfSubFiles;
	Tag[] poiTags;
	String projectionName;
	GeoPoint startPosition;
	int tilePixelSize;
	Tag[] wayTags;

	MapFileInfo build() {
		return new MapFileInfo(this);
	}
}
