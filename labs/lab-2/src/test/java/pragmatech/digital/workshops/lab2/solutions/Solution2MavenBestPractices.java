package pragmatech.digital.workshops.lab2.solutions;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution for Exercise 2: Test Organization with @Tag and Maven Profiles
 *
 * <p>Key concepts demonstrated:
 *
 * <h3>1. Test Categorization with @Tag</h3>
 * <ul>
 *   <li>{@code @Tag("unit")} -- Fast tests with no external dependencies (milliseconds)</li>
 *   <li>{@code @Tag("integration")} -- Tests requiring Spring context, databases, or external
 *       services (seconds)</li>
 *   <li>{@code @Tag("nightly")} -- Expensive or slow tests that only run in nightly builds
 *       (minutes)</li>
 * </ul>
 *
 * <h3>2. Maven Profiles for Test Filtering</h3>
 * <p>The pom.xml defines two profiles:
 * <ul>
 *   <li>{@code unit-tests} -- Configures maven-surefire-plugin to only run @Tag("unit") tests</li>
 *   <li>{@code integration-tests} -- Configures maven-failsafe-plugin to only run
 *       @Tag("integration") tests</li>
 * </ul>
 *
 * <h3>3. Parallel Test Execution</h3>
 * <p>The junit-platform.properties file enables class-level parallelism:
 * <ul>
 *   <li>Tests within a class run sequentially to avoid shared state issues</li>
 *   <li>Different test classes run concurrently for faster overall execution</li>
 *   <li>Use {@code @Execution(ExecutionMode.SAME_THREAD)} on a class to opt out of parallelism</li>
 * </ul>
 *
 * <h3>4. CI/CD Strategy</h3>
 * <pre>
 *   PR builds:      ./mvnw verify -DexcludedGroups=nightly   (skip slow tests)
 *   Merge to main:  ./mvnw verify                            (run all standard tests)
 *   Nightly:        ./mvnw verify -Dgroups=nightly            (run expensive tests)
 * </pre>
 */
@Tag("unit")
class Solution2MavenBestPractices {

  @Test
  void shouldDemonstrateTagFilteringWithMavenProfiles() {
    // Maven profiles allow filtering tests by tag without changing code.
    //
    // In pom.xml, the unit-tests profile configures:
    //   <groups>unit</groups>
    //
    // This tells Surefire to only run tests annotated with @Tag("unit").
    // Running: ./mvnw test -Punit-tests
    // will execute only unit-tagged tests.

    String surefireGroupConfig = "<groups>unit</groups>";
    assertThat(surefireGroupConfig)
      .as("The Surefire plugin can filter tests by JUnit 5 tag groups")
      .contains("unit");
  }

  @Test
  void shouldDemonstrateTagExclusion() {
    // You can also exclude tags instead of including them.
    // This is useful for skipping slow tests in PR builds:
    //
    //   ./mvnw verify -DexcludedGroups=nightly
    //
    // This runs ALL tests EXCEPT those tagged as "nightly".

    String excludeConfig = "-DexcludedGroups=nightly";
    assertThat(excludeConfig)
      .as("The -DexcludedGroups flag excludes tests with a specific tag")
      .contains("nightly");
  }

  @Test
  void shouldUnderstandParallelExecutionConfig() {
    // junit-platform.properties controls parallel execution:
    //
    //   junit.jupiter.execution.parallel.enabled = true
    //   junit.jupiter.execution.parallel.mode.default = same_thread
    //   junit.jupiter.execution.parallel.mode.classes.default = concurrent
    //
    // "same_thread" for methods: tests in the same class run sequentially
    // "concurrent" for classes: different test classes run in parallel
    //
    // This gives the best balance of speed and safety.

    String parallelMode = "concurrent";
    String methodMode = "same_thread";

    assertThat(parallelMode)
      .as("Classes should run concurrently for speed")
      .isEqualTo("concurrent");
    assertThat(methodMode)
      .as("Methods within a class should run on the same thread for safety")
      .isEqualTo("same_thread");
  }
}
