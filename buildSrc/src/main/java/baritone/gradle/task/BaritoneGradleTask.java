/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.gradle.task;

import org.gradle.api.DefaultTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Brady
 * @since 10/12/2018
 */
class BaritoneGradleTask extends DefaultTask {

    protected static final String
            PROGUARD_ZIP = "proguard.zip",
            PROGUARD_JAR = "proguard.jar",
            PROGUARD_CONFIG_TEMPLATE = "scripts/proguard.pro",
            PROGUARD_CONFIG_DEST = "template.pro",
            PROGUARD_API_CONFIG = "api.pro",
            PROGUARD_STANDALONE_CONFIG = "standalone.pro",
            PROGUARD_EXPORT_PATH = "proguard_out.jar",
            PROGUARD_MAPPING_DIR = "mapping",

    ARTIFACT_STANDARD = "%s-%s.jar",
            ARTIFACT_UNOPTIMIZED = "%s-unoptimized-%s.jar",
            ARTIFACT_API = "%s-api-%s.jar",
            ARTIFACT_STANDALONE = "%s-standalone-%s.jar";

    protected String artifactName, artifactVersion;
    protected final Path
            artifactPath,
            artifactUnoptimizedPath, artifactApiPath, artifactStandalonePath, // these are different for forge builds
            proguardOut;

    public BaritoneGradleTask() {
        this.artifactName = getProject().getProperties().get("archivesBaseName").toString();
        this.artifactVersion = getProject().getVersion().toString();

        this.artifactPath = this.getBuildFile(formatVersion(ARTIFACT_STANDARD));

        this.artifactUnoptimizedPath = this.getBuildFile(formatVersion(ARTIFACT_UNOPTIMIZED));
        this.artifactApiPath = this.getBuildFile(formatVersion(ARTIFACT_API));
        this.artifactStandalonePath = this.getBuildFile(formatVersion(ARTIFACT_STANDALONE));

        this.proguardOut = this.getTemporaryFile(PROGUARD_EXPORT_PATH);
    }

    protected void verifyArtifacts() throws IllegalStateException {
        if (!Files.exists(this.artifactPath)) {
            throw new IllegalStateException("Artifact not found! Run build first! Missing file: " + this.artifactPath);
        }
    }

    protected void write(InputStream stream, Path file) throws IOException {
        if (Files.exists(file)) {
            Files.delete(file);
        }
        Files.copy(stream, file);
    }

    protected String formatVersion(String string) {
        return String.format(string, this.artifactName, this.artifactVersion);
    }

    protected Path getRelativeFile(String file) {
        return Paths.get(new File(getProject().getBuildDir(), file).getAbsolutePath());
    }

    protected Path getRootRelativeFile(String file) {
        return Paths.get(new File(getProject().getRootDir(), file).getAbsolutePath());
    }

    protected Path getTemporaryFile(String file) {
        return Paths.get(new File(getTemporaryDir(), file).getAbsolutePath());
    }

    protected Path getBuildFile(String file) {
        return getRelativeFile("libs/" + file);
    }
}
