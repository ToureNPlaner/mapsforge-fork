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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link MapDatabase} class.
 */
public class MapDatabaseTest {
	private static final String FILE_NAME = "src/test/resources/empty_map_file_version_3.map";

	/**
	 * Tests the {@link MapDatabase#getMapFileInfo} method.
	 */
	@Test
	public void getMapFileInfoTest() {
		MapDatabase mapDatabase = new MapDatabase();
		FileOpenResult fileOpenResult = mapDatabase.openFile(FILE_NAME);
		MapFileInfo mapFileInfo = mapDatabase.getMapFileInfo();
		mapDatabase.closeFile();

		Assert.assertTrue(fileOpenResult.getErrorMessage(), fileOpenResult.isSuccess());
		Assert.assertNull(fileOpenResult.getErrorMessage());

		Assert.assertEquals(127, mapFileInfo.fileSize);
		Assert.assertEquals(3, mapFileInfo.fileVersion);
		Assert.assertEquals(1324124730145L, mapFileInfo.mapDate);
		Assert.assertEquals(2, mapFileInfo.numberOfSubFiles);
		Assert.assertEquals("Mercator", mapFileInfo.projectionName);
		Assert.assertEquals(256, mapFileInfo.tilePixelSize);

		Assert.assertFalse(mapFileInfo.debugFile);

		Assert.assertNull(mapFileInfo.commentText);
		Assert.assertNull(mapFileInfo.languagePreference);
		Assert.assertNull(mapFileInfo.startPosition);
	}
}
