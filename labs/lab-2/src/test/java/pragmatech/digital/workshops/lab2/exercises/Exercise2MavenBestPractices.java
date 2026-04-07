package pragmatech.digital.workshops.lab2.exercises;

import org.junit.jupiter.api.Test;

/**
 * Exercise 2: Test Organization with @Tag and Maven Profiles
 *
 * <p>Tasks:
 * <ol>
 *   <li>Review the {@code @Tag("unit")} annotation on the TestCategorization experiment class</li>
 *   <li>Review the {@code @Tag("nightly")} annotation on the NightlyBuildDemoIT experiment class</li>
 *   <li>Run only unit tests: {@code ./mvnw test -Punit-tests}</li>
 *   <li>Run only integration tests: {@code ./mvnw verify -Pintegration-tests}</li>
 *   <li>Run only nightly tests: {@code ./mvnw verify -Dgroups=nightly}</li>
 *   <li>Compare build times for each profile vs running all tests</li>
 *   <li>Discuss: When would you use test separation in a real project?</li>
 * </ol>
 *
 * <p>Maven profile commands to try:
 * <pre>
 *   # Run all tests (default)
 *   ./mvnw verify
 *
 *   # Run only unit-tagged tests (fast feedback)
 *   ./mvnw test -Punit-tests
 *
 *   # Run only integration-tagged tests
 *   ./mvnw verify -Pintegration-tests
 *
 *   # Run tests with a specific tag directly
 *   ./mvnw test -Dgroups=unit
 *   ./mvnw verify -Dgroups=nightly
 *
 *   # Exclude specific tags
 *   ./mvnw verify -DexcludedGroups=nightly
 * </pre>
 */
class Exercise2MavenBestPractices {

  @Test
  void shouldUnderstandTestCategorization() {
    // TODO: Review the experiment classes and their @Tag annotations.
    //
    // 1. Open experiment/TestCategorization.java -- notice @Tag("unit")
    // 2. Open experiment/NightlyBuildDemoIT.java -- notice @Tag("nightly")
    // 3. Open the pom.xml and review the Maven profiles (unit-tests, integration-tests)
    // 4. Run: ./mvnw test -Punit-tests
    //    Observe that only @Tag("unit") tests are executed
    // 5. Run: ./mvnw verify -Dgroups=nightly
    //    Observe that only @Tag("nightly") tests are executed
    // 6. Compare the execution times
  }

  @Test
  void shouldUnderstandParallelExecution() {
    // TODO: Review the junit-platform.properties file in src/test/resources.
    //
    // The configuration enables parallel test execution at the class level:
    //   junit.jupiter.execution.parallel.enabled = true
    //   junit.jupiter.execution.parallel.mode.default = same_thread
    //   junit.jupiter.execution.parallel.mode.classes.default = concurrent
    //
    // This means:
    // - Tests within a single class run sequentially (same_thread)
    // - Different test classes run in parallel (concurrent)
    //
    // Questions to consider:
    // - What problems can parallel execution cause with shared state?
    // - How does @ResourceLock help with parallel test safety?
    // - When should you disable parallel execution for specific tests?
  }
}
