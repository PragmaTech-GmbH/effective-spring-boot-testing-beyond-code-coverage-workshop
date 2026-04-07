package pragmatech.digital.workshops.lab3.experiment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates the use of JUnit 5 @Tag for test categorization.
 *
 * <p>This class is tagged as "unit" which means all tests in it will be included
 * when running with {@code -Dgroups=unit} or with the {@code unit-tests} Maven profile.
 *
 * <p>Usage:
 * <pre>
 *   # Run only this class (and other unit-tagged tests)
 *   ./mvnw test -Punit-tests
 *
 *   # Or directly with the JUnit 5 tag filter
 *   ./mvnw test -Dgroups=unit
 * </pre>
 */
@Tag("unit")
class TestCategorization {

  @Test
  void shouldDemonstrateUnitTestTag() {
    // Fast unit test -- no Spring context, no external dependencies.
    // These tests should run in milliseconds.
    assertThat(1 + 1).isEqualTo(2);
  }

  @Test
  void shouldShowTagFiltering() {
    // Run with: ./mvnw test -Dgroups=unit
    // Only tests tagged as "unit" will be executed.
    System.out.println("This test is tagged as 'unit' and runs without Spring context");
    assertThat(true).isTrue();
  }

  @Test
  void shouldDemonstrateMultipleTags() {
    // A test can have multiple tags. This is useful for cross-cutting categories.
    // For example, a test could be both "unit" and "fast":
    //
    //   @Tag("unit")
    //   @Tag("fast")
    //   void myTest() { ... }
    //
    // Then you can run: ./mvnw test -Dgroups="unit & fast"
    // or: ./mvnw test -Dgroups="unit | fast"
    assertThat("unit").isNotBlank();
  }
}
