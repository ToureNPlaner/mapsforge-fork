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

import java.io.IOException;

import org.mapsforge.core.BoundingBox;
import org.mapsforge.core.GeoPoint;
import org.mapsforge.core.Tag;
import org.mapsforge.core.Tile;

/**
 * Reads and validates the header data from a binary map file.
 */
class MapFileHeader {
	/**
	 * Maximum valid base zoom level of a sub-file.
	 */
	private static final int BASE_ZOOM_LEVEL_MAX = 20;

	/**
	 * Magic byte at the beginning of a valid binary map file.
	 */
	private static final String BINARY_OSM_MAGIC_BYTE = "mapsforge binary OSM";

	/**
	 * Bitmask for the debug flag in the file header.
	 */
	private static final int HEADER_BITMASK_DEBUG = 0x80;

	/**
	 * Bitmask for the start position in the file header.
	 */
	private static final int HEADER_BITMASK_START_POSITION = 0x40;

	/**
	 * Maximum size of the file header in bytes.
	 */
	private static final int HEADER_SIZE_MAX = 1000000;

	/**
	 * Minimum size of the file header in bytes.
	 */
	private static final int HEADER_SIZE_MIN = 70;

	/**
	 * The maximum latitude values in microdegrees.
	 */
	private static final int LATITUDE_MAX = 90000000;

	/**
	 * The minimum latitude values in microdegrees.
	 */
	private static final int LATITUDE_MIN = -90000000;

	/**
	 * The maximum longitude values in microdegrees.
	 */
	private static final int LONGITUDE_MAX = 180000000;

	/**
	 * The minimum longitude values in microdegrees.
	 */
	private static final int LONGITUDE_MIN = -180000000;

	/**
	 * The name of the Mercator projection as stored in the file header.
	 */
	private static final String MERCATOR = "Mercator";

	/**
	 * Length of the debug signature at the beginning of the index.
	 */
	private static final byte SIGNATURE_LENGTH_INDEX = 16;

	/**
	 * A single whitespace character.
	 */
	private static final char SPACE = ' ';

	/**
	 * Version of the map file format which is supported by this implementation.
	 */
	private static final int SUPPORTED_FILE_VERSION = 3;

	private static FileOpenResult readBoundingBox(ReadBuffer readBuffer, MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the minimum latitude (4 bytes)
		int minLatitude = readBuffer.readInt();
		if (minLatitude < LATITUDE_MIN || minLatitude > LATITUDE_MAX) {
			return new FileOpenResult("invalid minimum latitude: " + minLatitude);
		}

		// get and check the minimum longitude (4 bytes)
		int minLongitude = readBuffer.readInt();
		if (minLongitude < LONGITUDE_MIN || minLongitude > LONGITUDE_MAX) {
			return new FileOpenResult("invalid minimum longitude: " + minLongitude);
		}

		// get and check the maximum latitude (4 bytes)
		int maxLatitude = readBuffer.readInt();
		if (maxLatitude < LATITUDE_MIN || maxLatitude > LATITUDE_MAX) {
			return new FileOpenResult("invalid maximum latitude: " + maxLatitude);
		}

		// get and check the maximum longitude (4 bytes)
		int maxLongitude = readBuffer.readInt();
		if (maxLongitude < LONGITUDE_MIN || maxLongitude > LONGITUDE_MAX) {
			return new FileOpenResult("invalid maximum longitude: " + maxLongitude);
		}

		// check latitude and longitude range
		if (minLatitude > maxLatitude) {
			return new FileOpenResult("invalid latitude range: " + minLatitude + SPACE + maxLatitude);
		} else if (minLongitude > maxLongitude) {
			return new FileOpenResult("invalid longitude range: " + minLongitude + SPACE + maxLongitude);
		}

		mapFileInfoBuilder.boundingBox = new BoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude);
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readFileSize(ReadBuffer readBuffer, long fileSize,
			MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the file size (8 bytes)
		long headerFileSize = readBuffer.readLong();
		if (headerFileSize != fileSize) {
			return new FileOpenResult("invalid file size: " + headerFileSize);
		}
		mapFileInfoBuilder.fileSize = fileSize;
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readFileVersion(ReadBuffer readBuffer, MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the file version (4 bytes)
		int fileVersion = readBuffer.readInt();
		if (fileVersion != SUPPORTED_FILE_VERSION) {
			return new FileOpenResult("unsupported file version: " + fileVersion);
		}
		mapFileInfoBuilder.fileVersion = fileVersion;
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readMagicByte(ReadBuffer readBuffer) throws IOException {
		// read the the magic byte and the file header size into the buffer
		int magicByteLength = BINARY_OSM_MAGIC_BYTE.length();
		if (!readBuffer.readFromFile(magicByteLength + 4)) {
			return new FileOpenResult("reading magic byte has failed");
		}

		// get and check the magic byte
		String magicByte = readBuffer.readUTF8EncodedString(magicByteLength);
		if (!BINARY_OSM_MAGIC_BYTE.equals(magicByte)) {
			return new FileOpenResult("invalid magic byte: " + magicByte);
		}
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readMapDate(ReadBuffer readBuffer, MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the the map date (8 bytes)
		long mapDate = readBuffer.readLong();
		// is the map date before 2010-01-10 ?
		if (mapDate < 1200000000000L) {
			return new FileOpenResult("invalid map date: " + mapDate);
		}
		mapFileInfoBuilder.mapDate = mapDate;
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readMapStartPosition(ReadBuffer readBuffer,
			MapFileInfoBuilder mapFileInfoBuilder) {
		if (mapFileInfoBuilder.hasStartPosition) {
			// get and check the start position latitude (4 byte)
			int mapStartLatitude = readBuffer.readInt();
			if (mapStartLatitude < LATITUDE_MIN || mapStartLatitude > LATITUDE_MAX) {
				return new FileOpenResult("invalid map start latitude: " + mapStartLatitude);
			}

			// get and check the start position longitude (4 byte)
			int mapStartLongitude = readBuffer.readInt();
			if (mapStartLongitude < LONGITUDE_MIN || mapStartLongitude > LONGITUDE_MAX) {
				return new FileOpenResult("invalid map start longitude: " + mapStartLongitude);
			}

			mapFileInfoBuilder.startPosition = new GeoPoint(mapStartLatitude, mapStartLongitude);
		}
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readPoiTags(ReadBuffer readBuffer, MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the number of POI tags (2 bytes)
		int numberOfPoiTags = readBuffer.readShort();
		if (numberOfPoiTags < 0) {
			return new FileOpenResult("invalid number of POI tags: " + numberOfPoiTags);
		}

		Tag[] poiTags = new Tag[numberOfPoiTags];
		for (int currentTagId = 0; currentTagId < numberOfPoiTags; ++currentTagId) {
			// get and check the POI tag
			String tag = readBuffer.readUTF8EncodedString();
			if (tag == null) {
				return new FileOpenResult("POI tag must not be null: " + currentTagId);
			}
			poiTags[currentTagId] = new Tag(tag);
		}
		mapFileInfoBuilder.poiTags = poiTags;
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readProjectionName(ReadBuffer readBuffer,
			MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the projection name
		String projectionName = readBuffer.readUTF8EncodedString();
		if (!MERCATOR.equals(projectionName)) {
			return new FileOpenResult("unsupported projection: " + projectionName);
		}
		mapFileInfoBuilder.projectionName = projectionName;
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readRemainingHeader(ReadBuffer readBuffer) throws IOException {
		// get and check the size of the remaining file header (4 bytes)
		int remainingHeaderSize = readBuffer.readInt();
		if (remainingHeaderSize < HEADER_SIZE_MIN || remainingHeaderSize > HEADER_SIZE_MAX) {
			return new FileOpenResult("invalid remaining header size: " + remainingHeaderSize);
		}

		// read the header data into the buffer
		if (!readBuffer.readFromFile(remainingHeaderSize)) {
			return new FileOpenResult("reading header data has failed: " + remainingHeaderSize);
		}
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readTilePixelSize(ReadBuffer readBuffer, MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the tile pixel size (2 bytes)
		int tilePixelSize = readBuffer.readShort();
		if (tilePixelSize != Tile.TILE_SIZE) {
			return new FileOpenResult("unsupported tile pixel size: " + tilePixelSize);
		}
		mapFileInfoBuilder.tilePixelSize = tilePixelSize;
		return FileOpenResult.SUCCESS;
	}

	private static FileOpenResult readWayTags(ReadBuffer readBuffer, MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the number of way tags (2 bytes)
		int numberOfWayTags = readBuffer.readShort();
		if (numberOfWayTags < 0) {
			return new FileOpenResult("invalid number of way tags: " + numberOfWayTags);
		}

		Tag[] wayTags = new Tag[numberOfWayTags];

		for (int currentTagId = 0; currentTagId < numberOfWayTags; ++currentTagId) {
			// get and check the way tag
			String tag = readBuffer.readUTF8EncodedString();
			if (tag == null) {
				return new FileOpenResult("way tag must not be null: " + currentTagId);
			}
			wayTags[currentTagId] = new Tag(tag);
		}
		mapFileInfoBuilder.wayTags = wayTags;
		return FileOpenResult.SUCCESS;
	}

	private MapFileInfo mapFileInfo;
	private SubFileParameter[] subFileParameters;
	private byte zoomLevelMaximum;
	private byte zoomLevelMinimum;

	private FileOpenResult readSubFileParameters(ReadBuffer readBuffer, long fileSize,
			MapFileInfoBuilder mapFileInfoBuilder) {
		// get and check the number of sub-files (1 byte)
		byte numberOfSubFiles = readBuffer.readByte();
		if (numberOfSubFiles < 1) {
			return new FileOpenResult("invalid number of sub-files: " + numberOfSubFiles);
		}
		mapFileInfoBuilder.numberOfSubFiles = numberOfSubFiles;

		SubFileParameter[] tempSubFileParameters = new SubFileParameter[numberOfSubFiles];
		this.zoomLevelMinimum = Byte.MAX_VALUE;
		this.zoomLevelMaximum = Byte.MIN_VALUE;

		// get and check the information for each sub-file
		for (byte currentSubFile = 0; currentSubFile < numberOfSubFiles; ++currentSubFile) {
			// get and check the base zoom level (1 byte)
			byte baseZoomLevel = readBuffer.readByte();
			if (baseZoomLevel < 0 || baseZoomLevel > BASE_ZOOM_LEVEL_MAX) {
				return new FileOpenResult("invalid base zooom level: " + baseZoomLevel);
			}

			// get and check the minimum zoom level (1 byte)
			byte zoomLevelMin = readBuffer.readByte();
			if (zoomLevelMin < 0 || zoomLevelMin > 22) {
				return new FileOpenResult("invalid minimum zoom level: " + zoomLevelMin);
			}

			// get and check the maximum zoom level (1 byte)
			byte zoomLevelMax = readBuffer.readByte();
			if (zoomLevelMax < 0 || zoomLevelMax > 22) {
				return new FileOpenResult("invalid maximum zoom level: " + zoomLevelMax);
			}

			// check for valid zoom level range
			if (zoomLevelMin > zoomLevelMax) {
				return new FileOpenResult("invalid zoom level range: " + zoomLevelMin + SPACE + zoomLevelMax);
			}

			// get and check the start address of the sub-file (8 bytes)
			long startAddress = readBuffer.readLong();
			if (startAddress < HEADER_SIZE_MIN || startAddress >= fileSize) {
				return new FileOpenResult("invalid start address: " + startAddress);
			}

			long indexStartAddress = startAddress;
			if (mapFileInfoBuilder.isDebugFile) {
				// the sub-file has an index signature before the index
				indexStartAddress += SIGNATURE_LENGTH_INDEX;
			}

			// get and check the size of the sub-file (8 bytes)
			long subFileSize = readBuffer.readLong();
			if (subFileSize < 1) {
				return new FileOpenResult("invalid sub-file size: " + subFileSize);
			}

			// add the current sub-file to the list of sub-files
			tempSubFileParameters[currentSubFile] = new SubFileParameter(startAddress, indexStartAddress,
					subFileSize, baseZoomLevel, zoomLevelMin, zoomLevelMax, mapFileInfoBuilder.boundingBox);

			updateZoomLevelInformation(tempSubFileParameters[currentSubFile]);
		}

		// create and fill the lookup table for the sub-files
		this.subFileParameters = new SubFileParameter[this.zoomLevelMaximum + 1];
		for (int currentMapFile = 0; currentMapFile < numberOfSubFiles; ++currentMapFile) {
			SubFileParameter subFileParameter = tempSubFileParameters[currentMapFile];
			for (byte zoomLevel = subFileParameter.zoomLevelMin; zoomLevel <= subFileParameter.zoomLevelMax; ++zoomLevel) {
				this.subFileParameters[zoomLevel] = subFileParameter;
			}
		}
		return FileOpenResult.SUCCESS;
	}

	private void updateZoomLevelInformation(SubFileParameter subFileParameter) {
		// update the global minimum and maximum zoom level information
		if (this.zoomLevelMinimum > subFileParameter.zoomLevelMin) {
			this.zoomLevelMinimum = subFileParameter.zoomLevelMin;
		}
		if (this.zoomLevelMaximum < subFileParameter.zoomLevelMax) {
			this.zoomLevelMaximum = subFileParameter.zoomLevelMax;
		}
	}

	MapFileInfo getMapFileInfo() {
		return this.mapFileInfo;
	}

	byte getQueryZoomLevel(byte zoomLevel) {
		if (zoomLevel > this.zoomLevelMaximum) {
			return this.zoomLevelMaximum;
		} else if (zoomLevel < this.zoomLevelMinimum) {
			return this.zoomLevelMinimum;
		}
		return zoomLevel;
	}

	SubFileParameter getSubFileParameter(int queryZoomLevel) {
		return this.subFileParameters[queryZoomLevel];
	}

	/**
	 * Reads and validates the header block from the map file.
	 * 
	 * @param readBuffer
	 *            the ReadBuffer for the file data.
	 * @param fileSize
	 *            the size of the map file in bytes.
	 * @return a FileOpenResult containing an error message in case of a failure.
	 * @throws IOException
	 *             if an error occurs while reading the file.
	 */
	FileOpenResult readHeader(ReadBuffer readBuffer, long fileSize) throws IOException {
		FileOpenResult fileOpenResult = readMagicByte(readBuffer);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readRemainingHeader(readBuffer);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		MapFileInfoBuilder mapFileInfoBuilder = new MapFileInfoBuilder();

		fileOpenResult = readFileVersion(readBuffer, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readFileSize(readBuffer, fileSize, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readMapDate(readBuffer, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readBoundingBox(readBuffer, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readTilePixelSize(readBuffer, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readProjectionName(readBuffer, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		mapFileInfoBuilder.languagePreference = readBuffer.readUTF8EncodedString();

		byte metaFlags = readBuffer.readByte();
		mapFileInfoBuilder.isDebugFile = (metaFlags & HEADER_BITMASK_DEBUG) != 0;
		mapFileInfoBuilder.hasStartPosition = (metaFlags & HEADER_BITMASK_START_POSITION) != 0;

		fileOpenResult = readMapStartPosition(readBuffer, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readPoiTags(readBuffer, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readWayTags(readBuffer, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		fileOpenResult = readSubFileParameters(readBuffer, fileSize, mapFileInfoBuilder);
		if (!fileOpenResult.isSuccess()) {
			return fileOpenResult;
		}

		mapFileInfoBuilder.commentText = readBuffer.readUTF8EncodedString();

		this.mapFileInfo = mapFileInfoBuilder.build();
		return FileOpenResult.SUCCESS;
	}
}
