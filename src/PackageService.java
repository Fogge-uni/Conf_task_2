
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class PackageService {

    public Map<String, String> getDirectDependencies(AppConfig config)
            throws IOException {

        String json = config.isTestMode()
                ? readTestRepository(config.getRepository())
                : downloadPackage(config.getRepository(),
                config.getPackageName());

        Object parsedJson = new JsonParser().parse(json);

        if (!(parsedJson instanceof Map)) {
            throw new IllegalArgumentException(
                    "Корневой элемент JSON должен быть объектом");
        }

        Map<String, Object> metadata = castObject(parsedJson);
        checkPackageName(metadata, config.getPackageName());

        Map<String, Object> dependencies = findDependencies(metadata);
        return convertDependencies(dependencies);
    }

    private void checkPackageName(
            Map<String, Object> metadata, String expectedName) {

        Object actualName = metadata.get("name");

        if (actualName != null && !expectedName.equals(actualName)) {
            throw new IllegalArgumentException(
                    "В репозитории найден другой пакет: " + actualName);
        }
    }

    private Map<String, String> convertDependencies(
            Map<String, Object> dependencies) {

        Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : dependencies.entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                throw new IllegalArgumentException(
                        "Некорректная версия зависимости: " + entry.getKey());
            }

            result.put(entry.getKey(), (String) entry.getValue());
        }

        return result;
    }

    private String readTestRepository(String filePath)
            throws IOException {

        Path path = Path.of(filePath);

        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IOException(
                    "Тестовый файл не найден или недоступен для чтения");
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private String downloadPackage(String repository, String packageName)
            throws IOException {

        String encodedName = packageName.replace("/", "%2f");
        String baseUrl = repository.endsWith("/")
                ? repository
                : repository + "/";

        URL url = new URL(baseUrl + encodedName);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        configureConnection(connection);

        try {
            return readResponse(connection, packageName);
        } finally {
            connection.disconnect();
        }
    }

    private void configureConnection(HttpURLConnection connection)
            throws IOException {

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty(
                "User-Agent", "Java-Dependency-Analyzer");
    }

    private String readResponse(
            HttpURLConnection connection, String packageName)
            throws IOException {

        int status = connection.getResponseCode();

        if (status == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IOException(
                    "Пакет не найден в npm Registry: " + packageName);
        }

        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException(
                    "Ошибка HTTP-запроса. Код ответа: " + status);
        }

        try (InputStream input = connection.getInputStream()) {
            return new String(
                    input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Map<String, Object> findDependencies(
            Map<String, Object> metadata) {

        Object dependencies = metadata.get("dependencies");

        if (dependencies == null) {
            dependencies = findLatestDependencies(metadata);
        }

        if (dependencies == null) {
            return new LinkedHashMap<>();
        }

        if (!(dependencies instanceof Map)) {
            throw new IllegalArgumentException(
                    "Список зависимостей имеет неправильный формат");
        }

        return castObject(dependencies);
    }

    private Object findLatestDependencies(Map<String, Object> metadata) {
        Map<String, Object> distTags =
                getOptionalObject(metadata.get("dist-tags"));

        if (distTags == null) {
            throw new IllegalArgumentException(
                    "В JSON отсутствуют данные о последней версии пакета");
        }

        Object latestVersion = distTags.get("latest");

        if (!(latestVersion instanceof String)) {
            throw new IllegalArgumentException(
                    "В JSON отсутствует версия latest");
        }

        Map<String, Object> versions =
                getOptionalObject(metadata.get("versions"));

        if (versions == null) {
            throw new IllegalArgumentException(
                    "В JSON отсутствует список версий пакета");
        }

        Map<String, Object> versionData =
                getOptionalObject(versions.get(latestVersion));

        if (versionData == null) {
            throw new IllegalArgumentException(
                    "Не найдены данные последней версии пакета");
        }

        return versionData.get("dependencies");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castObject(Object value) {
        return (Map<String, Object>) value;
    }

    private Map<String, Object> getOptionalObject(Object value) {
        if (value instanceof Map) {
            return castObject(value);
        }
        return null;
    }
}
