package io.github.pyke.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class OptionalTypeAdapter<T> extends TypeAdapter<Optional<T>> {

    private final TypeAdapter<T> valueAdapter;

    public OptionalTypeAdapter(Gson gson, Type type) {
        this.valueAdapter = (TypeAdapter<T>) gson.getAdapter(TypeToken.get(type));
    }

    @Override
    public void write(JsonWriter out, Optional<T> value) throws IOException {
        if (value.isEmpty()) {
            out.nullValue();
        } else {
            valueAdapter.write(out, value.get());
        }
    }

    @Override
    public Optional<T> read(JsonReader in) throws IOException {
        T value = valueAdapter.read(in);
        return Optional.ofNullable(value);
    }
}

class OptionalTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Type type = typeToken.getType();

        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        if (!Optional.class.isAssignableFrom(typeToken.getRawType())) {
            return null;
        }

        Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
        return (TypeAdapter<T>) new OptionalTypeAdapter<>(gson, actualType);
    }
}
