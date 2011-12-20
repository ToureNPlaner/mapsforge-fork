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

/**
 * Contains the immutable metadata of a map file.
 * 
 * @see MapDatabase#getMapFileInfo()
 */
public class MapFileInfo {
	final BoundingBox boundingBox;
	final String commentText;
	final boolean debugFile;
	final long fileSize;
	final int fileVersion;
	final String languagePreference;
	final GeoPoint mapCenter;
	final long mapDate;
	final byte numberOfSubFiles;
	final Tag[] poiTags;
	final String projectionName;
	final GeoPoint startPosition;
	final int tilePixelSize;
	final Tag[] wayTags;

	MapFileInfo(MapFileInfoBuilder mapFileInfoBuilder) {
		this.commentText = mapFileInfoBuilder.commentText;
		this.debugFile = mapFileInfoBuilder.isDebugFile;
		this.fileSize = mapFileInfoBuilder.fileSize;
		this.fileVersion = mapFileInfoBuilder.fileVersion;
		this.languagePreference = mapFileInfoBuilder.languagePreference;
		this.boundingBox = mapFileInfoBuilder.boundingBox;
		this.mapCenter = this.boundingBox.getCenterPoint();
		this.mapDate = mapFileInfoBuilder.mapDate;
		this.numberOfSubFiles = mapFileInfoBuilder.numberOfSubFiles;
		this.poiTags = mapFileInfoBuilder.poiTags;
		this.projectionName = mapFileInfoBuilder.projectionName;
		this.startPosition = mapFileInfoBuilder.startPosition;
		this.tilePixelSize = mapFileInfoBuilder.tilePixelSize;
		this.wayTags = mapFileInfoBuilder.wayTags;
	}

	/**
	 * @return the bounding box of the map file.
	 */
	public BoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	/**
	 * @return the comment text of the map file (may be null).
	 */
	public String getCommentText() {
		return this.commentText;
	}

	/**
	 * @return the size of the map file, measured in bytes.
	 */
	public long getFileSize() {
		return this.fileSize;
	}

	/**
	 * @return the file version number of the map file.
	 */
	public int getFileVersion() {
		return this.fileVersion;
	}

	/**
	 * @return the preferred language for names as defined in ISO 3166-1 (may be null).
	 */
	public String getLanguagePreference() {
		return this.languagePreference;
	}

	/**
	 * @return the center point of the map file.
	 */
	public GeoPoint getMapCenter() {
		return this.mapCenter;
	}

	/**
	 * @return the date of the map data in milliseconds since January 1, 1970.
	 */
	public long getMapDate() {
		return this.mapDate;
	}

	/**
	 * @return the number of sub-files in the map file.
	 */
	public byte getNumberOfSubFiles() {
		return this.numberOfSubFiles;
	}

	/**
	 * @return the POI tags.
	 */
	public Tag[] getPoiTags() {
		return this.poiTags.clone();
	}

	/**
	 * @return the name of the projection used in the map file.
	 */
	public String getProjectionName() {
		return this.projectionName;
	}

	/**
	 * @return the map start position from the file header (may be null).
	 */
	public GeoPoint getStartPosition() {
		return this.startPosition;
	}

	/**
	 * @return the size of the tiles in pixels.
	 */
	public int getTilePixelSize() {
		return this.tilePixelSize;
	}

	/**
	 * @return the way tags.
	 */
	public Tag[] getWayTags() {
		return this.wayTags.clone();
	}

	/**
	 * @return true if the map file includes debug information, false otherwise.
	 */
	public boolean isDebugFile() {
		return this.debugFile;
	}
}
