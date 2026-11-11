import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class ConfigValidator {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "(?:@[a-z0-9][a-z0-9._~-]*/)?[a-z0-9][a-z0-9._~-]*");
    private static final int MAX_PACKAGE_NAME_LENGTH = 214;
    private static final int MIN_DEPTH = 0;
    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";

    public static void validate(AppConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("настройки приложения не заданы");
        }
        validatePackageName(config.getPackageName());
        validateRepository(config.getRepository(), config.isTestMode());
        validateDepth(config.getMaxDepth());
        validateFilter(config.getFilter());
    }

    private static void validatePackageName(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            throw new IllegalArgumentException("необходимо указать --package");
        }
        if (packageName.length() > MAX_PACKAGE_NAME_LENGTH
                || !PACKAGE_PATTERN.matcher(packageName).matches()) {
            throw new IllegalArgumentException("некорректное имя npm-пакета: " + packageName);
        }
    }

    private static void validateRepository(String repository, boolean testMode) {
        if (repository == null || repository.isBlank()) {
            throw new IllegalArgumentException("репозиторий не должен быть пустым");
        }
        if (testMode) {
            validateTestFile(repository);
        } else {
            validateRepositoryUrl(repository);
        }
    }

    private static void validateRepositoryUrl(String repository) {
        URI uri;
        try {
            uri = URI.create(repository);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("некорректный URL репозитория");
        }
        String scheme = uri.getScheme();
        boolean validScheme = HTTP_SCHEME.equalsIgnoreCase(scheme)
                || HTTPS_SCHEME.equalsIgnoreCase(scheme);
        if (!validScheme || uri.getHost() == null) {
            throw new IllegalArgumentException(
                    "URL должен содержать HTTP/HTTPS-схему и имя узла");
        }
    }

    private static void validateTestFile(String repository) {
        try {
            Path path = Path.of(repository);
            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                throw new IllegalArgumentException(
                        "тестовый файл не найден или недоступен для чтения");
            }
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("некорректный путь к тестовому файлу");
        }
    }

    private static void validateDepth(int maxDepth) {
        if (maxDepth < MIN_DEPTH) {
            throw new IllegalArgumentException("глубина не может быть отрицательной");
        }
    }

    private static void validateFilter(String filter) {
        if (filter == null) {
            throw new IllegalArgumentException("параметр --filter не может быть null");
        }
    }
}
