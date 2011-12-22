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
 * A BoundingBox represents an immutable set of two latitude and two longitude coordinates.
 */
public class BoundingBox {
	/**
	 * Conversion factor from degrees to microdegrees.
	 */
	private static final double CONVERSION_FACTOR = 1000000d;

	private static boolean isBetween(int number, int min, int max) {
		return min <= number && number <= max;
	}

	/**
	 * The maximum latitude value of this BoundingBox in microdegrees.
	 */
	public final int maxLatitudeE6;

	/**
	 * The maximum longitude value of this BoundingBox in microdegrees.
	 */
	public final int maxLongitudeE6;

	/**
	 * The minimum latitude value of this BoundingBox in microdegrees.
	 */
	public final int minLatitudeE6;

	/**
	 * The minimum longitude value of this BoundingBox in microdegrees.
	 */
	public final int minLongitudeE6;

	/**
	 * Constructs a new BoundingBox with the given coordinates in microdegrees.
	 * 
	 * @param minLatitudeE6
	 *            the minimum latitude in microdegrees.
	 * @param minLongitudeE6
	 *            the minimum longitude in microdegrees.
	 * @param maxLatitudeE6
	 *            the maximum latitude in microdegrees.
	 * @param maxLongitudeE6
	 *            the maximum longitude in microdegrees.
	 */
	public BoundingBox(int minLatitudeE6, int minLongitudeE6, int maxLatitudeE6, int maxLongitudeE6) {
		this.minLatitudeE6 = minLatitudeE6;
		this.minLongitudeE6 = minLongitudeE6;
		this.maxLatitudeE6 = maxLatitudeE6;
		this.maxLongitudeE6 = maxLongitudeE6;
	}

	/**
	 * @param geoPoint
	 *            the point whose coordinates should be checked.
	 * @return true if this BoundingBox contains the given GeoPoint, false otherwise.
	 */
	public boolean contains(GeoPoint geoPoint) {
		return isBetween(geoPoint.latitudeE6, this.minLatitudeE6, this.maxLatitudeE6)
				&& isBetween(geoPoint.longitudeE6, this.minLongitudeE6, this.maxLongitudeE6);
	}

	/**
	 * @return the GeoPoint at the horizontal and vertical center of this BoundingBox.
	 */
	public GeoPoint getCenterPoint() {
		int latitudeOffset = (this.maxLatitudeE6 - this.minLatitudeE6) / 2;
		int longitudeOffset = (this.maxLongitudeE6 - this.minLongitudeE6) / 2;
		return new GeoPoint(this.minLatitudeE6 + latitudeOffset, this.minLongitudeE6 + longitudeOffset);
	}

	/**
	 * @return the maximum latitude value of this BoundingBox in degrees.
	 */
	public double getMaxLatitude() {
		return this.maxLatitudeE6 / CONVERSION_FACTOR;
	}

	/**
	 * @return the maximum longitude value of this BoundingBox in degrees.
	 */
	public double getMaxLongitude() {
		return this.maxLongitudeE6 / CONVERSION_FACTOR;
	}

	/**
	 * @return the minimum latitude value of this BoundingBox in degrees.
	 */
	public double getMinLatitude() {
		return this.minLatitudeE6 / CONVERSION_FACTOR;
	}

	/**
	 * @return the minimum longitude value of this BoundingBox in degrees.
	 */
	public double getMinLongitude() {
		return this.minLongitudeE6 / CONVERSION_FACTOR;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("BoundingBox [minLatitudeE6=");
		stringBuilder.append(this.minLatitudeE6);
		stringBuilder.append(", minLongitudeE6=");
		stringBuilder.append(this.minLongitudeE6);
		stringBuilder.append(", maxLatitudeE6=");
		stringBuilder.append(this.maxLatitudeE6);
		stringBuilder.append(", maxLongitudeE6=");
		stringBuilder.append(this.maxLongitudeE6);
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
}
