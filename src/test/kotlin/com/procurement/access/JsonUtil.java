package com.procurement.access;

public class JsonUtil {
//    private final ObjectMapper mapper;
//
//    public JsonUtil() {
//        final ObjectMapper m = new ObjectMapper();
//        m.registerModule(new JavaTimeModule());
//        mapper = m;
//    }
//
//    public <T> String toJson(final T object) {
//        Objects.requireNonNull(object);
//
//        try {
//            return mapper.writeValueAsString(object);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public <T> T toObject(final Class<T> clazz, final String json) {
//        Objects.requireNonNull(json);
//
//        try {
//            return mapper.readValue(json, clazz);
//        } catch (IOException e) {
//            throw new IllegalArgumentException(e);
//        }
//    }
//
//    public String getResource(final String fileName) {
//        final String resource = readResource(fileName);
//        try {
//            return toCompact(resource);
//        } catch (IOException e) {
//            throw new IllegalArgumentException("Error in file: '" + fileName + "'.");
//        }
//    }
//
//    private String readResource(final String fileName) {
//        final String path = getPathFile(fileName);
//        return read(path);
//    }
//
//    private String getPathFile(final String fileName) {
//        return Optional.ofNullable(getClass().getClassLoader()
//                .getResource(fileName))
//                .map(URL::getPath)
//                .orElseThrow(() ->
//                        new IllegalArgumentException("File: '" + fileName + "' not found.")
//                );
//    }
//
//    private String read(final String pathToFile) {
//        try {
//            final Path path = Paths.get(pathToFile);
//            final byte[] buffer = Files.readAllBytes(path);
//            return new String(buffer);
//        } catch (IOException | InvalidPathException e) {
//            throw new IllegalArgumentException("File: '" + pathToFile + "' can not be readData.");
//        }
//    }
//
//    private String toCompact(final String source) throws IOException {
//        final JsonFactory factory = new JsonFactory();
//        final JsonParser parser = factory.createParser(source);
//        final StringWriter out = new StringWriter();
//        try (JsonGenerator gen = factory.createGenerator(out)) {
//            while (parser.nextToken() != null) {
//                gen.copyCurrentEvent(parser);
//            }
//        }
//        return out.getBuffer()
//                .toString();
//    }
}
