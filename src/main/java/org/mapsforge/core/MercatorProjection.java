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
package org.mapsforge.core;

/**
 * An implementation of the spherical Mercator projection.
 */
public final class MercatorProjection {
	/**
	 * The circumference of the earth at the equator in meters.
	 */
	public static final double EARTH_CIRCUMFERENCE = 40075016.686;

	/**
	 * Maximum possible latitude coordinate of the map.
	 */
	public static final double LATITUDE_MAX = 85.05113;

	/**
	 * Minimum possible latitude coordinate of the map.
	 */
	public static final double LATITUDE_MIN = -85.05113;

	/**
	 * Maximum possible longitude coordinate of the map.
	 */
	public static final double LONGITUDE_MAX = 180;

	/**
	 * Minimum possible longitude coordinate of the map.
	 */
	public static final double LONGITUDE_MIN = -180;

	/**
	 * Calculates the distance on the ground that is represented by a single pixel on the map.
	 * 
	 * @param latitude
	 *            the latitude coordinate at which the resolution should be calculated.
	 * @param zoom
	 *            the zoom level at which the resolution should be calculated.
	 * @return the ground resolution at the given latitude and zoom level.
	 */
	public static double calculateGroundResolution(double latitude, byte zoom) {
		return Math.cos(latitude * (Math.PI / 180)) * EARTH_CIRCUMFERENCE / ((long) Tile.TILE_SIZE << zoom);
	}

	/**
	 * Converts a latitude coordinate (in degrees) to a pixel Y coordinate at a certain zoom level.
	 * 
	 * @param latitude
	 *            the latitude coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the pixel Y coordinate of the latitude value.
	 */
	public static double latitudeToPixelY(double latitude, byte zoom) {
		double sinLatitude = Math.sin(latitude * (Math.PI / 180));
		return (0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI))
				* ((long) Tile.TILE_SIZE << zoom);
	}

	/**
	 * Converts a latitude coordinate (in degrees) to a tile Y number at a certain zoom level.
	 * 
	 * @param latitude
	 *            the latitude coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the tile Y number of the latitude value.
	 */
	public static long latitudeToTileY(double latitude, byte zoom) {
		return pixelYToTileY(latitudeToPixelY(latitude, zoom), zoom);
	}

	/**
	 * @param latitude
	 *            the latitude value which should be checked.
	 * @return the given latitude value, limited to the possible latitude range.
	 */
	public static double limitLatitude(double latitude) {
		return Math.max(Math.min(latitude, LATITUDE_MAX), LATITUDE_MIN);
	}

	/**
	 * @param longitude
	 *            the longitude value which should be checked.
	 * @return the given longitude value, limited to the possible longitude range.
	 */
	public static double limitLongitude(double longitude) {
		return Math.max(Math.min(longitude, LONGITUDE_MAX), LONGITUDE_MIN);
	}

	/**
	 * Converts a longitude coordinate (in degrees) to a pixel X coordinate at a certain zoom level.
	 * 
	 * @param longitude
	 *            the longitude coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the pixel X coordinate of the longitude value.
	 */
	public static double longitudeToPixelX(double longitude, byte zoom) {
		return (longitude + 180) / 360 * ((long) Tile.TILE_SIZE << zoom);
	}

	/**
	 * Converts a longitude coordinate (in degrees) to the tile X number at a certain zoom level.
	 * 
	 * @param longitude
	 *            the longitude coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the tile X number of the longitude value.
	 */
	public static long longitudeToTileX(double longitude, byte zoom) {
		return pixelXToTileX(longitudeToPixelX(longitude, zoom), zoom);
	}

	/**
	 * Converts a pixel X coordinate at a certain zoom level to a longitude coordinate.
	 * 
	 * @param pixelX
	 *            the pixel X coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the longitude value of the pixel X coordinate.
	 */
	public static double pixelXToLongitude(double pixelX, byte zoom) {
		return 360 * ((pixelX / ((long) Tile.TILE_SIZE << zoom)) - 0.5);
	}

	/**
	 * Converts a pixel X coordinate to the tile X number.
	 * 
	 * @param pixelX
	 *            the pixel X coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the tile X number.
	 */
	public static long pixelXToTileX(double pixelX, byte zoom) {
		return (long) Math.min(Math.max(pixelX / Tile.TILE_SIZE, 0), Math.pow(2, zoom) - 1);
	}

	/**
	 * Converts a pixel Y coordinate at a certain zoom level to a latitude coordinate.
	 * 
	 * @param pixelY
	 *            the pixel Y coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the latitude value of the pixel Y coordinate.
	 */
	public static double pixelYToLatitude(double pixelY, byte zoom) {
		double y = 0.5 - (pixelY / ((long) Tile.TILE_SIZE << zoom));
		return 90 - 360 * Math.atan(Math.exp(-y * (2 * Math.PI))) / Math.PI;
	}

	/**
	 * Converts a pixel Y coordinate to the tile Y number.
	 * 
	 * @param pixelY
	 *            the pixel Y coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the tile Y number.
	 */
	public static long pixelYToTileY(double pixelY, byte zoom) {
		return (long) Math.min(Math.max(pixelY / Tile.TILE_SIZE, 0), Math.pow(2, zoom) - 1);
	}

	/**
	 * Converts a tile X number at a certain zoom level to a longitude coordinate.
	 * 
	 * @param tileX
	 *            the tile X number that should be converted.
	 * @param zoom
	 *            the zoom level at which the number should be converted.
	 * @return the longitude value of the tile X number.
	 */
	public static double tileXToLongitude(long tileX, byte zoom) {
		return pixelXToLongitude(tileX * Tile.TILE_SIZE, zoom);
	}

	/**
	 * Converts a tile Y number at a certain zoom level to a latitude coordinate.
	 * 
	 * @param tileY
	 *            the tile Y number that should be converted.
	 * @param zoom
	 *            the zoom level at which the number should be converted.
	 * @return the latitude value of the tile Y number.
	 */
	public static double tileYToLatitude(long tileY, byte zoom) {
		return pixelYToLatitude(tileY * Tile.TILE_SIZE, zoom);
	}

	private MercatorProjection() {
		throw new IllegalStateException();
	}
}
