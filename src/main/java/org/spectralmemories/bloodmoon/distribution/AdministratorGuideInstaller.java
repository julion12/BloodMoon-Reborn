package org.spectralmemories.bloodmoon.distribution;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Extracts a closed manifest of administrator resources without overwriting user files. */
public final class AdministratorGuideInstaller {
    private static final List<ResourceFile> MANIFEST = List.of(
            file("README.txt"),
            file("EXAMPLES/TAB-scoreboards.yml"),
            file("EXAMPLES/CommandsOnStart.yml"),
            file("EXAMPLES/CommandsOnEnd.yml"),
            file("EXAMPLES/SurvivorRewards.yml"),
            file("EXAMPLES/BossRewards.yml"),
            file("EXAMPLES/PlaceholderAPI-examples.txt"),
            file("EXAMPLES/MythicMobs-BloodMoonBoss.yml"),
            file("docs/VERSION.txt"),
            file("docs/COMPATIBILITY.md"),
            file("docs/CONFIGURATION.md"),
            file("docs/EXAMPLES.md"),
            file("docs/MANUAL_TEST_CHECKLIST.md"),
            file("docs/MIGRATION.md"),
            file("docs/MYTHICMOBS.md"),
            file("docs/PLACEHOLDERAPI.md"),
            file("docs/PLACEHOLDERS.md"),
            file("docs/SURVIVOR_REWARDS.md"));

    private final Path dataFolder;
    private final ResourceProvider resources;
    private final Logger logger;
    private final List<ResourceFile> manifest;

    public AdministratorGuideInstaller(Path dataFolder, ClassLoader classLoader, Logger logger) {
        this(dataFolder, classLoader::getResourceAsStream, logger, MANIFEST);
    }

    AdministratorGuideInstaller(Path dataFolder, ResourceProvider resources,
                                Logger logger, List<ResourceFile> manifest) {
        this.dataFolder = dataFolder.toAbsolutePath().normalize();
        this.resources = resources;
        this.logger = logger;
        this.manifest = List.copyOf(manifest);
    }

    public InstallResult installMissing() {
        List<String> created = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (ResourceFile resource : manifest) {
            try {
                if (install(resource)) created.add(resource.targetPath());
            } catch (IOException | RuntimeException exception) {
                failed.add(resource.targetPath());
                logger.log(Level.WARNING, "Could not install administrator guide "
                        + resource.targetPath() + "; plugin startup will continue", exception);
            }
        }
        return new InstallResult(List.copyOf(created), List.copyOf(failed));
    }

    private boolean install(ResourceFile resource) throws IOException {
        validateResourcePath(resource.resourcePath());
        Path destination = dataFolder.resolve(resource.targetPath()).normalize();
        if (!destination.startsWith(dataFolder)) {
            throw new IOException("Rejected administrator guide path outside plugin data folder: "
                    + resource.targetPath());
        }
        if (Files.exists(destination)) return false;
        Path parent = destination.getParent();
        if (parent == null || !parent.startsWith(dataFolder)) {
            throw new IOException("Rejected administrator guide parent path: " + resource.targetPath());
        }
        Files.createDirectories(parent);
        Path temporary = parent.resolve("." + destination.getFileName() + ".tmp-" + UUID.randomUUID()).normalize();
        if (!temporary.startsWith(dataFolder)) throw new IOException("Rejected temporary guide path");
        try (InputStream input = resources.open(resource.resourcePath())) {
            if (input == null) throw new IOException("Bundled resource is missing: " + resource.resourcePath());
            Files.copy(input, temporary);
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                try {
                    Files.move(temporary, destination);
                } catch (FileAlreadyExistsException race) {
                    return false;
                }
            } catch (FileAlreadyExistsException exception) {
                return false;
            }
            return true;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void validateResourcePath(String resourcePath) throws IOException {
        if (!resourcePath.startsWith("distribution/")
                || resourcePath.contains("..")
                || resourcePath.startsWith("/")
                || resourcePath.contains("\\")) {
            throw new IOException("Rejected invalid bundled resource path: " + resourcePath);
        }
    }

    public static List<ResourceFile> manifest() { return MANIFEST; }

    private static ResourceFile file(String targetPath) {
        return new ResourceFile("distribution/" + targetPath, targetPath);
    }

    @FunctionalInterface
    interface ResourceProvider {
        InputStream open(String path) throws IOException;
    }

    public record ResourceFile(String resourcePath, String targetPath) { }

    public record InstallResult(List<String> created, List<String> failed) {
        public int createdCount() { return created.size(); }
        public boolean readmeCreated() { return created.contains("README.txt"); }
    }
}
