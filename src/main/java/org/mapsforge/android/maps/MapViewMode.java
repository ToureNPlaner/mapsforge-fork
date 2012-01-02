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
package org.mapsforge.android.maps;

/**
 * The MapViewMode enumeration lists all possible {@link MapView} operating modes. To check if a MapViewMode requires an
 * Internet connection, use the {@link #requiresInternetConnection()} method.
 */
public enum MapViewMode {
	/**
	 * Map tiles are rendered using the {@link android.graphics} package (Skia library).
	 * 
	 * @see <a href="http://code.google.com/p/skia/">Skia Graphics Engine</a>
	 */
	CANVAS_RENDERER,

	/**
	 * Map tiles are downloaded from the Mapnik server. Requires an Internet connection.
	 * 
	 * @see <a href="http://wiki.openstreetmap.org/wiki/Mapnik">Mapnik</a>
	 */
	MAPNIK_TILE_DOWNLOAD,

	/**
	 * Map tiles are downloaded from the OpenCycleMap server. Requires an Internet connection.
	 * 
	 * @see <a href="http://opencyclemap.org/">OpenCycleMap</a>
	 */
	OPENCYCLEMAP_TILE_DOWNLOAD,

	/**
	 * Map tiles are downloaded from the Osmarender server. Requires an Internet connection.
	 * 
	 * @see <a href="http://wiki.openstreetmap.org/wiki/Osmarender">Osmarender</a>
	 */
	OSMARENDER_TILE_DOWNLOAD;

	/**
	 * @return true if this MapViewMode requires an Internet connection, false otherwise.
	 */
	public boolean requiresInternetConnection() {
		switch (this) {
			case CANVAS_RENDERER:
				return false;
			case MAPNIK_TILE_DOWNLOAD:
				return true;
			case OPENCYCLEMAP_TILE_DOWNLOAD:
				return true;
			case OSMARENDER_TILE_DOWNLOAD:
				return true;
		}
		throw new IllegalStateException();
	}
}
