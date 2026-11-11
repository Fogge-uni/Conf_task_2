public class Main {

    public static void main(String[] args) {
        try {
            ArgumentParser parser = new ArgumentParser();
            AppConfig config = parser.parse(args);

            ConfigValidator.validate(config);

            printConfiguration(config);

        } catch (IllegalArgumentException exception) {
            System.out.println("Ошибка параметров: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void printConfiguration(AppConfig config) {
        System.out.println("Параметры запуска:");
        System.out.println("package = " + config.getPackageName());
        System.out.println("repository = " + config.getRepository());
        System.out.println("test = " + config.isTestMode());
        System.out.println("max-depth = " + config.getMaxDepth());
        System.out.println("filter = " + config.getFilter());
    }
}
