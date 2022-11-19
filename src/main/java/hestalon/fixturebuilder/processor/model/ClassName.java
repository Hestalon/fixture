package hestalon.fixturebuilder.processor.model;

public record ClassName(String packageName, String className) {
    public String fqcn() {
        return hasDefaultPackage()
            ? className
            : packageName + '.' + className;
    }

    public boolean hasDefaultPackage() {
        return packageName.isEmpty();
    }

    public boolean hasPackage(String otherPackage) {
        return packageName.equals(otherPackage);
    }
}
