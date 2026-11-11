import java.util.HashSet;
import java.util.Set;

public class ArgumentParser {
    private static final String PACKAGE_OPTION = "--package";
    private static final String REPOSITORY_OPTION = "--repository";
    private static final String TEST_OPTION = "--test";
    private static final String MAX_DEPTH_OPTION = "--max-depth";
    private static final String FILTER_OPTION = "--filter";
    private static final String DEFAULT_REPOSITORY = "https://registry.npmjs.org/";
    private static final int DEFAULT_MAX_DEPTH = 3;

    public AppConfig parse(String[] args) {
        checkArgumentsArray(args);
        String packageName = null;
        String repository = DEFAULT_REPOSITORY;
        boolean testMode = false;
        int maxDepth = DEFAULT_MAX_DEPTH;
        String filter = "";
        Set<String> usedOptions = new HashSet<>();

        for (int index = 0; index < args.length; index++) {
            String option = args[index];
            checkOption(option, usedOptions);
            if (TEST_OPTION.equals(option)) {
                testMode = true;
            } else {
                String value = readValue(args, index + 1, option);
                index++;
                switch (option) {
                    case PACKAGE_OPTION:
                        packageName = value;
                        break;
                    case REPOSITORY_OPTION:
                        repository = value;
                        break;
                    case MAX_DEPTH_OPTION:
                        maxDepth = parseDepth(value);
                        break;
                    case FILTER_OPTION:
                        filter = value;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "для параметра не предусмотрено значение: " + option);
                }
            }
        }
        checkTestRepository(testMode, usedOptions);
        return new AppConfig(packageName, repository, testMode, maxDepth, filter);
    }

    private void checkTestRepository(boolean testMode, Set<String> usedOptions) {
        if (testMode && !usedOptions.contains(REPOSITORY_OPTION)) {
            throw new IllegalArgumentException(
                    "в тестовом режиме необходимо указать --repository с путём к файлу");
        }
    }

    private void checkArgumentsArray(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("массив аргументов не задан");
        }
    }

    private void checkOption(String option, Set<String> usedOptions) {
        if (!isKnownOption(option)) {
            throw new IllegalArgumentException("неизвестный параметр: " + option);
        }
        if (!usedOptions.add(option)) {
            throw new IllegalArgumentException("параметр указан повторно: " + option);
        }
    }

    private boolean isKnownOption(String option) {
        return PACKAGE_OPTION.equals(option)
                || REPOSITORY_OPTION.equals(option)
                || TEST_OPTION.equals(option)
                || MAX_DEPTH_OPTION.equals(option)
                || FILTER_OPTION.equals(option);
    }

    private String readValue(String[] args, int valueIndex, String option) {
        if (valueIndex >= args.length || args[valueIndex] == null
                || args[valueIndex].startsWith("--")) {
            throw new IllegalArgumentException("не задано значение для " + option);
        }
        return args[valueIndex];
    }

    private int parseDepth(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("глубина должна быть целым числом");
        }
    }
}
