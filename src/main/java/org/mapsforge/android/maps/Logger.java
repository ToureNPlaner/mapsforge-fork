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

import android.util.Log;

/**
 * Thread-safe class for logging text to the Android LogCat console.
 */
public final class Logger {
	private static final String LOG_TAG = "osm";

	/**
	 * Logs the given string message with debug level.
	 * 
	 * @param message
	 *            the message which should be logged.
	 */
	public static synchronized void debug(String message) {
		Log.d(LOG_TAG, Thread.currentThread().getName() + ": " + message);
	}

	/**
	 * Logs the given exception, including its stack trace.
	 * 
	 * @param exception
	 *            the exception which should be logged.
	 */
	public static synchronized void exception(Exception exception) {
		StringBuilder stringBuilder = new StringBuilder(512);
		stringBuilder.append(exception.getClass().getName());
		stringBuilder.append(" in thread \"");
		stringBuilder.append(Thread.currentThread().getName());
		stringBuilder.append("\" ");
		stringBuilder.append(exception.toString());
		StackTraceElement[] stackTraceElements = exception.getStackTrace();
		for (int i = 0; i < stackTraceElements.length; ++i) {
			stringBuilder.append("\n    at ");
			stringBuilder.append(stackTraceElements[i].getMethodName());
			stringBuilder.append('(');
			stringBuilder.append(stackTraceElements[i].getFileName());
			stringBuilder.append(':');
			stringBuilder.append(stackTraceElements[i].getLineNumber());
			stringBuilder.append(')');
		}
		Log.e(LOG_TAG, stringBuilder.toString());
	}

	/**
	 * Private constructor to prevent instantiation from other classes.
	 */
	private Logger() {
		throw new IllegalStateException();
	}
}
