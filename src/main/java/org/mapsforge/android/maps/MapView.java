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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.mapsforge.android.maps.inputhandling.MapMover;
import org.mapsforge.android.maps.inputhandling.TouchEventHandler;
import org.mapsforge.android.maps.inputhandling.ZoomAnimator;
import org.mapsforge.android.maps.mapgenerator.FileSystemTileCache;
import org.mapsforge.android.maps.mapgenerator.InMemoryTileCache;
import org.mapsforge.android.maps.mapgenerator.JobParameters;
import org.mapsforge.android.maps.mapgenerator.JobQueue;
import org.mapsforge.android.maps.mapgenerator.JobTheme;
import org.mapsforge.android.maps.mapgenerator.MapGenerator;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorFactory;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.MapWorker;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.ExternalRenderTheme;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.android.maps.overlay.OverlayList;
import org.mapsforge.android.maps.rendertheme.InternalRenderTheme;
import org.mapsforge.core.GeoPoint;
import org.mapsforge.core.MercatorProjection;
import org.mapsforge.core.Tile;
import org.mapsforge.map.reader.FileOpenResult;
import org.mapsforge.map.reader.MapDatabase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * A MapView shows a map on the display of the device. It handles all user input and touch gestures to move and zoom the
 * map. This MapView also includes a scale bar and zoom controls. The {@link #getController()} method returns a
 * {@link MapController} to programmatically modify the position and zoom level of the map.
 * <p>
 * This implementation supports offline map rendering as well as downloading map images (tiles) over an Internet
 * connection. All possible operation modes are listed in the {@link MapViewMode} enumeration. The operation mode of a
 * MapView can be set in the constructor and changed at runtime with the {@link #setMapViewMode(MapViewMode)} method.
 * Some MapView parameters depend on the selected operation mode.
 * <p>
 * In offline rendering mode a special database file is required which contains the map data. Map files can be stored in
 * any folder. The current map file is set by calling {@link #setMapFile(String)}. To retrieve the current
 * {@link MapDatabase}, use the {@link #getMapDatabase()} method.
 * <p>
 * {@link Overlay Overlays} can be used to display geographical data such as points and ways. To draw an overlay on top
 * of the map, add it to the list returned by {@link #getOverlays()}.
 */
public class MapView extends ViewGroup {

	/**
	 * The default MapViewMode.
	 */
	public static final MapViewMode DEFAULT_MAP_VIEW_MODE = MapViewMode.CANVAS_RENDERER;

	/**
	 * Default render theme of the MapView.
	 */
	public static final InternalRenderTheme DEFAULT_RENDER_THEME = InternalRenderTheme.OSMARENDER;

	private static final float DEFAULT_TEXT_SCALE = 1;
	private static final int DEFAULT_TILE_CACHE_SIZE_FILE_SYSTEM = 100;
	private static final int DEFAULT_TILE_CACHE_SIZE_IN_MEMORY = 20;

	private static MapViewMode extractMapViewMode(AttributeSet attributeSet) {
		String mapViewModeString = attributeSet.getAttributeValue(null, "mode");
		if (mapViewModeString == null) {
			return DEFAULT_MAP_VIEW_MODE;
		}
		return MapViewMode.valueOf(mapViewModeString);
	}

	private DebugSettings debugSettings;
	private final TileCache fileSystemTileCache;
	private final FpsCounter fpsCounter;
	private final FrameBuffer frameBuffer;
	private final TileCache inMemoryTileCache;
	private JobParameters jobParameters;
	private final JobQueue jobQueue;
	private final MapController mapController;
	private final MapDatabase mapDatabase;
	private String mapFile;
	private MapGenerator mapGenerator;
	private final MapMover mapMover;
	private final MapPosition mapPosition;
	private final MapScaleBar mapScaleBar;
	private MapViewMode mapViewMode;
	private final MapWorker mapWorker;
	private final MapZoomControls mapZoomControls;
	private final List<Overlay> overlays;
	private final Projection projection;
	private final TouchEventHandler touchEventHandler;
	private final ZoomAnimator zoomAnimator;

	/**
	 * Constructs a new MapView with the default {@link MapViewMode}.
	 * 
	 * @param context
	 *            the enclosing MapActivity instance.
	 * @throws IllegalArgumentException
	 *             if the context object is not an instance of {@link MapActivity}.
	 */
	public MapView(Context context) {
		this(context, null, DEFAULT_MAP_VIEW_MODE);
	}

	/**
	 * Constructs a new MapView. The {@link MapViewMode} can be defined via a {@code mode} attribute in the XML layout
	 * file. If no mode is specified, the default mode is used.
	 * 
	 * @param context
	 *            the enclosing MapActivity instance.
	 * @param attributeSet
	 *            a set of attributes.
	 * @throws IllegalArgumentException
	 *             if the context object is not an instance of {@link MapActivity}.
	 */
	public MapView(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, extractMapViewMode(attributeSet));
	}

	/**
	 * Constructs a new MapView with the given MapViewMode.
	 * 
	 * @param context
	 *            the enclosing MapActivity instance.
	 * @param mapViewMode
	 *            the mode in which the MapView should operate.
	 * @throws IllegalArgumentException
	 *             if the context object is not an instance of {@link MapActivity}.
	 */
	public MapView(Context context, MapViewMode mapViewMode) {
		this(context, null, mapViewMode == null ? DEFAULT_MAP_VIEW_MODE : mapViewMode);
	}

	private MapView(Context context, AttributeSet attributeSet, MapViewMode mapViewMode) {
		super(context, attributeSet);

		if (!(context instanceof MapActivity)) {
			throw new IllegalArgumentException("context is not an instance of MapActivity");
		}
		MapActivity mapActivity = (MapActivity) context;

		setBackgroundColor(FrameBuffer.MAP_VIEW_BACKGROUND);
		setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
		setWillNotDraw(false);

		this.debugSettings = new DebugSettings(false, false, false);
		this.fileSystemTileCache = new FileSystemTileCache(DEFAULT_TILE_CACHE_SIZE_FILE_SYSTEM,
				mapActivity.getMapViewId());
		this.fpsCounter = new FpsCounter();
		this.frameBuffer = new FrameBuffer(this);
		this.inMemoryTileCache = new InMemoryTileCache(DEFAULT_TILE_CACHE_SIZE_IN_MEMORY);
		this.jobParameters = new JobParameters(DEFAULT_RENDER_THEME, DEFAULT_TEXT_SCALE);
		this.jobQueue = new JobQueue(this);
		this.mapController = new MapController(this);
		this.mapDatabase = new MapDatabase();
		this.mapPosition = new MapPosition(this);
		this.mapScaleBar = new MapScaleBar(this);
		this.mapZoomControls = new MapZoomControls(mapActivity, this);
		this.overlays = new OverlayList(this);
		this.projection = new MapViewProjection(this);
		this.touchEventHandler = TouchEventHandler.getInstance(mapActivity, this);

		this.mapWorker = new MapWorker(this);
		this.mapWorker.start();

		this.mapMover = new MapMover(this);
		this.mapMover.start();

		this.zoomAnimator = new ZoomAnimator(this);
		this.zoomAnimator.start();

		setMapViewModeInternal(mapViewMode);
		mapActivity.registerMapView(this);
	}

	/**
	 * @return the MapController for this MapView.
	 */
	public MapController getController() {
		return this.mapController;
	}

	/**
	 * @return the debug settings which are used in this MapView.
	 */
	public DebugSettings getDebugSettings() {
		return this.debugSettings;
	}

	/**
	 * @return the file system tile cache which is used in this MapView.
	 */
	public TileCache getFileSystemTileCache() {
		return this.fileSystemTileCache;
	}

	/**
	 * @return the FPS counter which is used in this MapView.
	 */
	public FpsCounter getFpsCounter() {
		return this.fpsCounter;
	}

	/**
	 * @return the FrameBuffer which is used in this MapView.
	 */
	public FrameBuffer getFrameBuffer() {
		return this.frameBuffer;
	}

	/**
	 * @return the in-memory tile cache which is used in this MapView.
	 */
	public TileCache getInMemoryTileCache() {
		return this.inMemoryTileCache;
	}

	/**
	 * @return the job queue which is used in this MapView.
	 */
	public JobQueue getJobQueue() {
		return this.jobQueue;
	}

	/**
	 * @return the map database which is used for reading map files.
	 * @throws UnsupportedOperationException
	 *             if the MapViewMode works with an Internet connection.
	 */
	public MapDatabase getMapDatabase() {
		if (this.mapViewMode.requiresInternetConnection()) {
			throw new UnsupportedOperationException();
		}
		return this.mapDatabase;
	}

	/**
	 * @return the currently used map file.
	 * @throws UnsupportedOperationException
	 *             if the current MapView mode works with an Internet connection.
	 */
	public String getMapFile() {
		if (this.mapViewMode.requiresInternetConnection()) {
			throw new UnsupportedOperationException();
		}
		return this.mapFile;
	}

	/**
	 * @return the currently used MapGenerator (may be null).
	 */
	public MapGenerator getMapGenerator() {
		return this.mapGenerator;
	}

	/**
	 * @return the MapMover which is used by this MapView.
	 */
	public MapMover getMapMover() {
		return this.mapMover;
	}

	/**
	 * @return the current position and zoom level of this MapView.
	 */
	public MapPosition getMapPosition() {
		return this.mapPosition;
	}

	/**
	 * @return the scale bar which is used in this MapView.
	 */
	public MapScaleBar getMapScaleBar() {
		return this.mapScaleBar;
	}

	/**
	 * @return the current MapViewMode.
	 */
	public MapViewMode getMapViewMode() {
		return this.mapViewMode;
	}

	/**
	 * Returns a thread-safe list of overlays for this MapView. It is necessary to manually synchronize on this list
	 * when iterating over it.
	 * 
	 * @return the overlay list.
	 */
	public List<Overlay> getOverlays() {
		return this.overlays;
	}

	/**
	 * @return the currently used projection of the map. Do not keep this object for a longer time.
	 */
	public Projection getProjection() {
		return this.projection;
	}

	/**
	 * Calls either {@link #invalidate()} or {@link #postInvalidate()}, depending on the current thread.
	 */
	public void invalidateOnUiThread() {
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			invalidate();
		} else {
			postInvalidate();
		}
	}

	/**
	 * @return true if the ZoomAnimator is currently running, false otherwise.
	 */
	public boolean isZoomAnimatorRunning() {
		return this.zoomAnimator.isExecuting();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		return this.mapMover.onKeyDown(keyCode, keyEvent);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
		return this.mapMover.onKeyUp(keyCode, keyEvent);
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		int action = this.touchEventHandler.getAction(motionEvent);
		this.mapZoomControls.onMapViewTouchEvent(action);
		return this.touchEventHandler.handleTouchEvent(motionEvent);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent motionEvent) {
		return this.mapMover.onTrackballEvent(motionEvent);
	}

	/**
	 * Calculates all necessary tiles and adds jobs accordingly.
	 */
	public void redraw() {
		if (this.getWidth() <= 0 || this.getHeight() <= 0) {
			return;
		}

		synchronized (this.overlays) {
			for (int i = 0, n = this.overlays.size(); i < n; ++i) {
				this.overlays.get(i).requestRedraw();
			}
		}

		MapPositionFix mapPositionFix = this.mapPosition.getMapPositionFix();
		double pixelLeft = MercatorProjection.longitudeToPixelX(mapPositionFix.longitude, mapPositionFix.zoomLevel);
		double pixelTop = MercatorProjection.latitudeToPixelY(mapPositionFix.latitude, mapPositionFix.zoomLevel);
		pixelLeft -= getWidth() >> 1;
		pixelTop -= getHeight() >> 1;

		long tileLeft = MercatorProjection.pixelXToTileX(pixelLeft, mapPositionFix.zoomLevel);
		long tileTop = MercatorProjection.pixelYToTileY(pixelTop, mapPositionFix.zoomLevel);
		long tileRight = MercatorProjection.pixelXToTileX(pixelLeft + getWidth(), mapPositionFix.zoomLevel);
		long tileBottom = MercatorProjection.pixelYToTileY(pixelTop + getHeight(), mapPositionFix.zoomLevel);

		for (long tileY = tileTop; tileY <= tileBottom; ++tileY) {
			for (long tileX = tileLeft; tileX <= tileRight; ++tileX) {
				Tile tile = new Tile(tileX, tileY, mapPositionFix.zoomLevel);
				MapGeneratorJob mapGeneratorJob = new MapGeneratorJob(tile, this.mapViewMode, this.jobParameters,
						this.debugSettings);

				if (this.inMemoryTileCache.containsKey(mapGeneratorJob)) {
					Bitmap bitmap = this.inMemoryTileCache.get(mapGeneratorJob);
					this.frameBuffer.drawBitmap(mapGeneratorJob.tile, bitmap);
				} else if (this.fileSystemTileCache.containsKey(mapGeneratorJob)) {
					Bitmap bitmap = this.fileSystemTileCache.get(mapGeneratorJob);

					if (bitmap != null) {
						this.frameBuffer.drawBitmap(mapGeneratorJob.tile, bitmap);
						this.inMemoryTileCache.put(mapGeneratorJob, bitmap);
					} else {
						// the image data could not be read from the cache
						this.jobQueue.addJob(mapGeneratorJob);
					}
				} else {
					// cache miss
					this.jobQueue.addJob(mapGeneratorJob);
				}
			}
		}

		if (this.mapScaleBar.isShowMapScaleBar()) {
			this.mapScaleBar.redrawScaleBar();
		}

		invalidateOnUiThread();

		this.jobQueue.requestSchedule();
		synchronized (this.mapWorker) {
			this.mapWorker.notify();
		}
	}

	/**
	 * Sets the visibility of the zoom controls.
	 * 
	 * @param showZoomControls
	 *            true if the zoom controls should be visible, false otherwise.
	 */
	public void setBuiltInZoomControls(boolean showZoomControls) {
		this.mapZoomControls.setShowMapZoomControls(showZoomControls);
	}

	/**
	 * Sets the center of the MapView and triggers a redraw.
	 * 
	 * @param point
	 *            the new center point of the map.
	 */
	public void setCenter(GeoPoint point) {
		setCenterAndZoom(point, this.mapPosition.getZoomLevel());
	}

	/**
	 * @param debugSettings
	 *            the new DebugSettings for this MapView.
	 */
	public void setDebugSettings(DebugSettings debugSettings) {
		this.debugSettings = debugSettings;
		clearAndRedrawMapView();
	}

	/**
	 * Sets the map file for this MapView.
	 * 
	 * @param mapFile
	 *            the path to the map file.
	 * @return true if the map file was set correctly, false otherwise.
	 * @throws UnsupportedOperationException
	 *             if the current MapView mode works with an Internet connection.
	 */
	public boolean setMapFile(String mapFile) {
		if (this.mapViewMode.requiresInternetConnection()) {
			throw new UnsupportedOperationException();
		}
		if (mapFile == null) {
			// no map file specified
			return false;
		} else if (mapFile.equals(this.mapFile)) {
			// same map file as before
			return false;
		}

		this.zoomAnimator.pause();
		this.mapWorker.pause();
		this.mapMover.pause();

		this.zoomAnimator.awaitPausing();
		this.mapMover.awaitPausing();
		this.mapWorker.awaitPausing();

		this.mapMover.stopMove();
		this.jobQueue.clear();

		this.zoomAnimator.proceed();
		this.mapWorker.proceed();
		this.mapMover.proceed();

		this.mapDatabase.closeFile();
		FileOpenResult openFile = this.mapDatabase.openFile(mapFile);
		if (openFile.isSuccess()) {
			this.mapFile = mapFile;
			setCenter(this.mapGenerator.getStartPoint());
			clearAndRedrawMapView();
			return true;
		}
		this.mapFile = null;
		clearAndRedrawMapView();
		return false;
	}

	/**
	 * Sets the MapViewMode for this MapView.
	 * 
	 * @param mapViewMode
	 *            the new MapViewMode.
	 */
	public void setMapViewMode(MapViewMode mapViewMode) {
		if (this.mapViewMode != mapViewMode) {
			setMapViewModeInternal(mapViewMode);
			clearAndRedrawMapView();
		}
	}

	/**
	 * Sets the internal theme which is used for rendering the map.
	 * 
	 * @param internalRenderTheme
	 *            the internal rendering theme.
	 * @throws IllegalArgumentException
	 *             if the supplied internalRenderTheme is null.
	 * @throws UnsupportedOperationException
	 *             if the MapView currently works in downloading mode.
	 */
	public void setRenderTheme(InternalRenderTheme internalRenderTheme) {
		if (internalRenderTheme == null) {
			throw new IllegalArgumentException("render theme must not be null");
		} else if (this.mapViewMode.requiresInternetConnection()) {
			throw new UnsupportedOperationException();
		}

		this.jobParameters = new JobParameters(internalRenderTheme, this.jobParameters.textScale);
		clearAndRedrawMapView();
	}

	/**
	 * Sets the theme file which is used for rendering the map.
	 * 
	 * @param renderThemePath
	 *            the path to the XML file which defines the rendering theme.
	 * @throws IllegalArgumentException
	 *             if the supplied internalRenderTheme is null.
	 * @throws UnsupportedOperationException
	 *             if the MapView currently works in downloading mode.
	 * @throws FileNotFoundException
	 *             if the supplied file does not exist, is a directory or cannot be read.
	 */
	public void setRenderTheme(String renderThemePath) throws FileNotFoundException {
		if (renderThemePath == null) {
			throw new IllegalArgumentException("render theme path must not be null");
		} else if (this.mapViewMode.requiresInternetConnection()) {
			throw new UnsupportedOperationException();
		}

		JobTheme jobTheme = new ExternalRenderTheme(renderThemePath);
		this.jobParameters = new JobParameters(jobTheme, this.jobParameters.textScale);
		clearAndRedrawMapView();
	}

	/**
	 * Sets the text scale for the map rendering. Has no effect in downloading mode.
	 * 
	 * @param textScale
	 *            the new text scale for the map rendering.
	 */
	public void setTextScale(float textScale) {
		this.jobParameters = new JobParameters(this.jobParameters.jobTheme, textScale);
		clearAndRedrawMapView();
	}

	/**
	 * Takes a screenshot of the currently visible map and saves it as a compressed image. Zoom buttons, scale bar, FPS
	 * counter, overlays, menus and the title bar are not included in the screenshot.
	 * 
	 * @param fileName
	 *            the name of the image file. If the file exists, it will be overwritten.
	 * @param compressFormat
	 *            the file format of the compressed image.
	 * @param quality
	 *            value from 0 (low) to 100 (high). Has no effect on some formats like PNG.
	 * @return true if the image was saved successfully, false otherwise.
	 * @throws IOException
	 *             if an error occurs while writing the image file.
	 */
	public boolean takeScreenshot(CompressFormat compressFormat, int quality, String fileName) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(fileName);
		boolean success = this.frameBuffer.compress(compressFormat, quality, outputStream);
		outputStream.close();
		return success;
	}

	/**
	 * Zooms in or out by the given amount of zoom levels.
	 * 
	 * @param zoomLevelDiff
	 *            the difference to the current zoom level.
	 * @param zoomStart
	 *            the zoom factor at the begin of the animation.
	 * @return true if the zoom level was changed, false otherwise.
	 */
	public boolean zoom(byte zoomLevelDiff, float zoomStart) {
		float matrixScaleFactor;
		if (zoomLevelDiff > 0) {
			// check if zoom in is possible
			if (this.mapPosition.getZoomLevel() + zoomLevelDiff > getMaximumPossibleZoomLevel()) {
				return false;
			}
			matrixScaleFactor = 1 << zoomLevelDiff;
		} else if (zoomLevelDiff < 0) {
			// check if zoom out is possible
			if (this.mapPosition.getZoomLevel() + zoomLevelDiff < this.mapZoomControls.getZoomLevelMin()) {
				return false;
			}
			matrixScaleFactor = 1.0f / (1 << -zoomLevelDiff);
		} else {
			// zoom level is unchanged
			matrixScaleFactor = 1;
		}

		this.mapPosition.setZoomLevel((byte) (this.mapPosition.getZoomLevel() + zoomLevelDiff));
		this.mapZoomControls.onZoomLevelChange(this.mapPosition.getZoomLevel());

		this.zoomAnimator.setParameters(zoomStart, matrixScaleFactor, getWidth() >> 1, getHeight() >> 1);
		this.zoomAnimator.startAnimation();
		return true;
	}

	private void setMapViewModeInternal(MapViewMode mapViewMode) {
		this.mapViewMode = mapViewMode;

		this.mapGenerator = MapGeneratorFactory.createMapGenerator(this);
		this.mapWorker.setMapGenerator(this.mapGenerator);

		GeoPoint startPoint = this.mapGenerator.getStartPoint();
		if (startPoint != null) {
			this.mapPosition.setMapCenterAndZoomLevel(startPoint, this.mapGenerator.getZoomLevelDefault());
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		this.frameBuffer.draw(canvas);
		synchronized (this.overlays) {
			for (int i = 0, n = this.overlays.size(); i < n; ++i) {
				this.overlays.get(i).draw(canvas);
			}
		}

		if (this.mapScaleBar.isShowMapScaleBar()) {
			this.mapScaleBar.draw(canvas);
		}

		if (this.fpsCounter.isShowFpsCounter()) {
			this.fpsCounter.draw(canvas);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		this.mapZoomControls.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// find out how big the zoom controls should be
		this.mapZoomControls.measure(
				MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST));

		// make sure that MapView is big enough to display the zoom controls
		setMeasuredDimension(Math.max(MeasureSpec.getSize(widthMeasureSpec), this.mapZoomControls.getMeasuredWidth()),
				Math.max(MeasureSpec.getSize(heightMeasureSpec), this.mapZoomControls.getMeasuredHeight()));
	}

	@Override
	protected synchronized void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		this.frameBuffer.destroy();

		if (width > 0 && height > 0) {
			this.frameBuffer.onSizeChanged();
			redraw();

			synchronized (this.overlays) {
				for (int i = 0, n = this.overlays.size(); i < n; ++i) {
					this.overlays.get(i).onSizeChanged();
				}
			}
		}
	}

	void clearAndRedrawMapView() {
		this.jobQueue.clear();
		this.frameBuffer.clear();
		redraw();
	}

	void destroy() {
		this.overlays.clear();

		this.mapMover.interrupt();
		this.mapWorker.interrupt();
		this.zoomAnimator.interrupt();

		try {
			this.mapWorker.join();
		} catch (InterruptedException e) {
			// restore the interrupted status
			Thread.currentThread().interrupt();
		}

		this.frameBuffer.destroy();
		this.touchEventHandler.destroy();
		this.mapScaleBar.destroy();
		this.inMemoryTileCache.destroy();
		this.fileSystemTileCache.destroy();

		this.mapDatabase.closeFile();
	}

	/**
	 * @return the maximum possible zoom level.
	 */
	byte getMaximumPossibleZoomLevel() {
		return (byte) Math.min(this.mapZoomControls.getZoomLevelMax(), this.mapGenerator.getZoomLevelMax());
	}

	/**
	 * @return true if the current center position of this MapView is valid, false otherwise.
	 */
	boolean hasValidCenter() {
		if (!this.mapPosition.isValid()) {
			return false;
		} else if (!this.mapViewMode.requiresInternetConnection()
				&& (!this.mapDatabase.hasOpenFile() || !this.mapDatabase.getMapFileInfo().getBoundingBox()
						.contains(getMapPosition().getMapCenter()))) {
			return false;
		}

		return true;
	}

	byte limitZoomLevel(byte zoom) {
		return (byte) Math.max(Math.min(zoom, getMaximumPossibleZoomLevel()), this.mapZoomControls.getZoomLevelMin());
	}

	void onPause() {
		this.mapWorker.pause();
		this.mapMover.pause();
		this.zoomAnimator.pause();
	}

	void onResume() {
		this.mapWorker.proceed();
		this.mapMover.proceed();
		this.zoomAnimator.proceed();
	}

	/**
	 * Sets the center and zoom level of the MapView and triggers a redraw.
	 * 
	 * @param geoPoint
	 *            the new center point of the map.
	 * @param zoomLevel
	 *            the new zoom level. This value will be limited by the maximum and minimum possible zoom level.
	 */
	void setCenterAndZoom(GeoPoint geoPoint, byte zoomLevel) {
		if (geoPoint == null) {
			return;
		}

		if (this.mapViewMode.requiresInternetConnection()
				|| (this.mapDatabase.hasOpenFile() && this.mapDatabase.getMapFileInfo().getBoundingBox()
						.contains(geoPoint))) {
			if (hasValidCenter()) {
				// calculate the distance between previous and current position
				MapPositionFix mapPositionFix = this.mapPosition.getMapPositionFix();
				float matrixTranslateX = (float) (MercatorProjection.longitudeToPixelX(mapPositionFix.longitude,
						mapPositionFix.zoomLevel) - MercatorProjection.longitudeToPixelX(geoPoint.getLongitude(),
						mapPositionFix.zoomLevel));
				float matrixTranslateY = (float) (MercatorProjection.latitudeToPixelY(mapPositionFix.latitude,
						mapPositionFix.zoomLevel) - MercatorProjection.latitudeToPixelY(geoPoint.getLatitude(),
						mapPositionFix.zoomLevel));
				this.frameBuffer.matrixPostTranslate(matrixTranslateX, matrixTranslateY);
			}

			this.mapPosition.setMapCenterAndZoomLevel(geoPoint, zoomLevel);
			this.mapZoomControls.onZoomLevelChange(this.mapPosition.getZoomLevel());
			redraw();
		}
	}
}
