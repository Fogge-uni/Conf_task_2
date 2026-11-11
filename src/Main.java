
import java.io.IOException;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try {
            ArgumentParser parser = new ArgumentParser();
            AppConfig config = parser.parse(args);

            ConfigValidator.validate(config);

            PackageService packageService = new PackageService();

            Map<String, String> dependencies =
                    packageService.getDirectDependencies(config);

            printDependencies(dependencies);

        } catch (IllegalArgumentException exception) {
            System.out.println(
                    "Ошибка параметров или данных: "
                            + exception.getMessage());
            System.exit(1);

        } catch (IOException exception) {
            System.out.println(
                    "Ошибка получения данных: "
                            + exception.getMessage());
            System.exit(1);
        }
    }

    private static void printDependencies(
            Map<String, String> dependencies) {

        System.out.println("Прямые зависимости пакета:");

        if (dependencies.isEmpty()) {
            System.out.println("Зависимости отсутствуют.");
            return;
        }

        System.out.println(
                "Количество зависимостей: " + dependencies.size());

        for (Map.Entry<String, String> entry
                : dependencies.entrySet()) {

            System.out.println(
                    entry.getKey() + " : " + entry.getValue());
        }
    }
}
