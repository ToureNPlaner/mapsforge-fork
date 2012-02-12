/*
 * Copyright 2010, 2011, 2012 mapsforge.org
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
package org.mapsforge.android.maps.inputhandling;

import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.core.GeoPoint;

public class DragAndDropDetector {
	private TouchEventHandler touchEventHandler;
	private Overlay activeOverlay = null;
	private GeoPoint startPoint;


	public DragAndDropDetector(TouchEventHandler touchEventHandler) {
		this.touchEventHandler = touchEventHandler;
	}

	public void rememberStartPoint(GeoPoint gp) {
		startPoint = gp;
	}

	public void onActionCancel() {
		if (activeOverlay != null) {
			activeOverlay.onDragCancel();
			activeOverlay = null;
		}
	}

	public boolean onActionMove(GeoPoint point) {
		if (activeOverlay == null) {
			return false;
		}

		activeOverlay.onDragMove(point, touchEventHandler.mapView);
		return true;
	}

	public boolean onActionDown() {
		synchronized (touchEventHandler.mapView.getOverlays()) {
			for (int i = touchEventHandler.mapView.getOverlays().size() - 1; i >= 0; --i) {
				if (touchEventHandler.mapView.getOverlays().get(i).onDragStart(startPoint, touchEventHandler.mapView)) {
					activeOverlay = touchEventHandler.mapView.getOverlays().get(i);
					return true;
				}
			}
		}
		return false;
	}

	public boolean onActionUp(GeoPoint tapPoint) {
		if (activeOverlay == null) {
			return false;
		}

		activeOverlay.onDragStop(tapPoint, touchEventHandler.mapView);
		activeOverlay = null;
		return true;
	}
}
