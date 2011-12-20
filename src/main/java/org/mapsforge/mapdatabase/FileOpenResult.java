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

/**
 * A FileOpenResult is a simple DTO which is returned by {@link MapDatabase#openFile(String)}.
 */
public class FileOpenResult {
	static final FileOpenResult SUCCESS = new FileOpenResult();

	private final String errorMessage;
	private final boolean success;

	private FileOpenResult() {
		this.success = true;
		this.errorMessage = null;
	}

	FileOpenResult(String errorMessage) {
		if (errorMessage == null) {
			throw new IllegalArgumentException("error message must not be null");
		}

		this.success = false;
		this.errorMessage = errorMessage;
	}

	/**
	 * @return a textual error description (might be null).
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * @return true if the file could be opened successfully, false otherwise.
	 */
	public boolean isSuccess() {
		return this.success;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileOpenResult [success=");
		builder.append(this.success);
		builder.append(", errorMessage=");
		builder.append(this.errorMessage);
		builder.append("]");
		return builder.toString();
	}
}
