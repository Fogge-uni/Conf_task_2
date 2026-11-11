public class AppConfig {
    private final String packageName;
    private final String repository;
    private final boolean testMode;
    private final int maxDepth;
    private final String filter;

    public AppConfig(String packageName, String repository, boolean testMode,
                     int maxDepth, String filter) {
        this.packageName = packageName;
        this.repository = repository;
        this.testMode = testMode;
        this.maxDepth = maxDepth;
        this.filter = filter;
    }
    public String getPackageName() {
        return packageName;
    }
    public String getRepository() {
        return repository;
    }
    public boolean isTestMode() {
        return testMode;
    }
    public int getMaxDepth() {
        return maxDepth;
    }
    public String getFilter() {
        return filter;
    }
}
