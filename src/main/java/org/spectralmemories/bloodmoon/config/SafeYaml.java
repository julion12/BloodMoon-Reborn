package org.spectralmemories.bloodmoon.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Creates YAML readers that cannot instantiate arbitrary Java types. */
public final class SafeYaml {
    private SafeYaml() { }

    public static Yaml create() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setAllowRecursiveKeys(false);
        return new Yaml(new SafeConstructor(options));
    }
}
