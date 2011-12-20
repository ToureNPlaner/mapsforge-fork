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
package org.mapsforge.android.maps.mapgenerator;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.android.maps.mapgenerator.tiledownloader.MapnikTileDownloader;
import org.mapsforge.android.maps.mapgenerator.tiledownloader.OpenCycleMapTileDownloader;
import org.mapsforge.android.maps.mapgenerator.tiledownloader.OsmarenderTileDownloader;

/**
 * A factory class to create MapGenerator instances.
 */
public final class MapGeneratorFactory {
	/**
	 * @param mapView
	 *            the MapView for which a MapGenerator should be created.
	 * @return a MapGenerator which fits to the MapViewMode of the MapView.
	 */
	public static MapGenerator createMapGenerator(MapView mapView) {
		MapViewMode mapViewMode = mapView.getMapViewMode();
		switch (mapViewMode) {
			case CANVAS_RENDERER:
				return new DatabaseRenderer(mapView.getMapDatabase());
			case MAPNIK_TILE_DOWNLOAD:
				return new MapnikTileDownloader();
			case OPENCYCLEMAP_TILE_DOWNLOAD:
				return new OpenCycleMapTileDownloader();
			case OSMARENDER_TILE_DOWNLOAD:
				return new OsmarenderTileDownloader();
		}

		throw new IllegalArgumentException("invalid MapViewMode: " + mapViewMode);
	}

	private MapGeneratorFactory() {
		throw new IllegalStateException();
	}
}
