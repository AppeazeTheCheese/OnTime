package me.camdenorrb.timekeeper.utils;

import com.google.gson.Gson;

import java.io.*;
import java.util.function.Supplier;


public final class GsonUtils {

	private GsonUtils() {}


	public static <T> T fromJsonOrMake(final Gson gson, final File file, final Class<T> type, final Supplier<T> defaultValueBlock) {

		if (!file.exists()) {
			final T defaultValue = defaultValueBlock.get();
			TryUtils.attemptOrBreak(() -> gson.toJson(defaultValue, new FileWriter(file)));
			return defaultValue;
		}

		return TryUtils.attemptOrBreak(() ->
			gson.fromJson(new FileReader(file), type)
		);
	}

}
