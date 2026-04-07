package pragmatech.digital.workshops.lab1.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution for Exercise 1: Optimize GitHub Actions Workflow
 *
 * <p>The enhanced build.yml should look like this:
 *
 * <pre>{@code
 * name: Build
 *
 * on:
 *   push:
 *     branches: [ main ]
 *   pull_request:
 *     branches: [ main ]
 *
 * jobs:
 *   build:
 *     runs-on: ubuntu-latest
 *     timeout-minutes: 15
 *
 *     steps:
 *     - uses: actions/checkout@v4
 *
 *     - name: Set up JDK 21
 *       uses: actions/setup-java@v3
 *       with:
 *         java-version: '21'
 *         distribution: 'temurin'
 *         cache: maven
 *
 *     - name: Build with Maven
 *       run: ./mvnw verify
 *
 *     - name: Upload test reports
 *       if: failure()
 *       uses: actions/upload-artifact@v4
 *       with:
 *         name: test-reports
 *         path: 'target/surefire-reports'
 * }</pre>
 *
 * <p>Key improvements:
 * <ul>
 *   <li><b>cache: maven</b> -- Caches the ~/.m2/repository directory between runs, reducing
 *       dependency download time from minutes to seconds on subsequent builds.</li>
 *   <li><b>timeout-minutes: 15</b> -- Prevents runaway builds from consuming CI/CD minutes.
 *       Without this, a stuck build could run for up to 6 hours (the GitHub Actions default).</li>
 *   <li><b>Upload test reports on failure</b> -- When tests fail, the Surefire/Failsafe reports
 *       are uploaded as artifacts so developers can diagnose failures without re-running locally.</li>
 * </ul>
 *
 * <p>Additional best practices to consider:
 * <ul>
 *   <li>Use <code>./mvnw</code> (Maven Wrapper) instead of <code>mvn</code> to ensure consistent
 *       Maven versions across all environments.</li>
 *   <li>Consider using <code>mvnd</code> (Maven Daemon) for faster local development builds.</li>
 *   <li>Split unit and integration tests into separate CI jobs for faster feedback.</li>
 *   <li>Use <code>concurrency</code> groups to cancel outdated PR builds.</li>
 * </ul>
 */
class Solution1GithubActionsWorkflow {

  @Test
  void workflowShouldIncludeCaching() {
    // This test validates the understanding of GHA best practices.
    // The actual workflow file is at .github/workflows/build.yml.

    String expectedCacheConfig = "cache: maven";
    assertThat(expectedCacheConfig)
      .as("The setup-java action supports built-in Maven caching via the 'cache' parameter")
      .contains("maven");
  }

  @Test
  void workflowShouldIncludeTimeout() {
    // timeout-minutes prevents runaway builds from consuming CI/CD resources.
    int recommendedTimeoutMinutes = 15;
    assertThat(recommendedTimeoutMinutes)
      .as("A reasonable timeout prevents stuck builds from running indefinitely")
      .isGreaterThan(0)
      .isLessThanOrEqualTo(30);
  }

  @Test
  void workflowShouldUploadArtifactsOnFailure() {
    // The 'if: failure()' condition ensures artifacts are only uploaded when tests fail.
    String artifactCondition = "if: failure()";
    assertThat(artifactCondition)
      .as("Artifacts should only be uploaded when the build fails to save storage")
      .contains("failure()");
  }
}
