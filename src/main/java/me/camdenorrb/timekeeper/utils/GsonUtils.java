package me.camdenorrb.timekeeper.utils;

import com.google.gson.Gson;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


public final class GsonUtils {

	private GsonUtils() {}


	public static <T> T fromJsonOrMake(final Gson gson, final File file, final Class<T> type, final Supplier<T> defaultValueBlock) {

		if (!file.exists()) {

			file.getParentFile().mkdirs();

			final T defaultValue = defaultValueBlock.get();

			TryUtils.attemptOrBreak(() -> new FileWriter(file), (writer) ->
				gson.toJson(defaultValue, writer)
			);

			return defaultValue;
		}

		final AtomicReference<T> result = new AtomicReference<>();

		TryUtils.attemptOrBreak(() -> new FileReader(file), (reader) ->
			result.set(gson.fromJson(reader, type))
		);

		return result.get();
	}

}
