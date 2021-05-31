package me.camdenorrb.jcommons.utils;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


public final class GsonUtils {

	private GsonUtils() {}


	public static <T> T fromJsonOrMake(final Gson gson, final File file, final Class<T> type, final Supplier<T> defaultValueBlock) {

		if (!file.exists()) {

			final File parentFile = file.getParentFile();
			final T defaultValue = defaultValueBlock.get();

			if (parentFile != null) {
				parentFile.mkdirs();
			}

			TryUtils.attemptOrPrintErr(() -> new FileWriter(file), (writer) ->
				gson.toJson(defaultValue, writer)
			);

			return defaultValue;
		}

		final AtomicReference<T> result = new AtomicReference<>();

		TryUtils.attemptOrPrintErr(() -> new FileReader(file), (reader) ->
			result.set(gson.fromJson(reader, type))
		);

		return result.get();
	}

}
