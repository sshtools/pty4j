package com.pty4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import com.google.common.io.Resources;
import com.pty4j.unix.SelfExtractingPtyExecutor;
import com.sun.jna.Platform;

public class Lib {

	public static String locateLibrary() {
		final String ao = getAO();
		final ClassLoader loader = SelfExtractingPtyExecutor.class.getClassLoader();
		try {
			URL resource = loader.getResource(ao);
			if (resource == null) {
				/*
				 * The natives are not available on the classpath, either the
				 * user hasn't included the natives Jar, or this is running in a
				 * dev environment. If dev environment, try java.library.path
				 */
				for (String f : System.getProperty("java.library.path", "").split("\\" + File.pathSeparatorChar)) {
					File file = new File(new File(f), ao);
					if (file.exists())
						return file.getAbsolutePath();
				}
				throw new RuntimeException(
						"The appropriate Pty4J library could not be found either on the classpath or anywhere in java.library.path. Please correct this.");
			} else {
				File tf = File.createTempFile("lib", ao.replace("/", "_"));
				tf.deleteOnExit();
				FileOutputStream out = new FileOutputStream(tf);
				try {
					Resources.copy(resource, out);
				} finally {
					out.close();
				}
				return tf.getAbsolutePath();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getAO() {
		if (Platform.isMac() && System.getProperty("os.arch").startsWith("x86_64"))
			return "macosx/x86_64/libpty.dylib";
		else if (Platform.isMac())
			return "macosx/x86/libpty.dylib";
		else if (Platform.isWindows() && System.getProperty("os.arch").startsWith("amd64"))
			return "win64/libwinpty.dll";
		else if (Platform.isWindows())
			return "win32/libwinpty.dll";
		else if (Platform.isLinux() && System.getProperty("os.arch").startsWith("amd64"))
			return "linux/x86_64/libpty.so";
		else if (Platform.isLinux())
			return "linux/x86/libpty.so";
		else
			throw new RuntimeException("Unsupported platform");
	}

}
