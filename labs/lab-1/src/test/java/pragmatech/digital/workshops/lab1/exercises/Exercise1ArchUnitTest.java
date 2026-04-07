package pragmatech.digital.workshops.lab1.exercises;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Exercise 1: Write ArchUnit rules to enforce architectural constraints.
 *
 * <p>Tasks:
 * <ol>
 *   <li>Replace the placeholder rule {@code controllersShouldNotAccessRepositories} with a real rule
 *       verifying that no class in the {@code controller} package accesses the {@code repository} package.
 *       Exclude {@code ThreadController} (it's a test-helper that intentionally bypasses the layer).</li>
 *   <li>Replace the placeholder rule {@code layeredArchitectureRuleShouldBeRespected} with a
 *       {@code layeredArchitecture()} rule covering Controller → Service → Repository.</li>
 * </ol>
 *
 * <p>Hints:
 * <ul>
 *   <li>Use {@code noClasses().that().resideInAPackage("..controller..").and().doNotHaveSimpleName("ThreadController")}</li>
 *   <li>Use {@code layeredArchitecture().consideringAllDependencies().layer(...).definedBy(...).whereLayer(...).mayOnlyBeAccessedByLayers(...)}</li>
 *   <li>Use {@code .ignoreDependency(ThreadController.class, BookRepository.class)} on the layered rule</li>
 * </ul>
 *
 * <p>Run with: {@code ./mvnw test -Dtest=Exercise1ArchUnitTest}
 */
@AnalyzeClasses(
  packages = "pragmatech.digital.workshops.lab1",
  importOptions = ImportOption.DoNotIncludeTests.class
)
class Exercise1ArchUnitTest {

  // TODO: Replace this placeholder with a rule like:
  // noClasses().that().resideInAPackage("..controller..")
  //   .and().doNotHaveSimpleName("ThreadController")
  //   .should().accessClassesThat().resideInAPackage("..repository..")
  @ArchTest
  static final ArchRule controllersShouldNotAccessRepositories = noClasses()
    .that().haveSimpleName("__placeholder__")
    .should().accessClassesThat().resideInAPackage("..repository..")
    .allowEmptyShould(true);

  // TODO: Replace this placeholder with a layeredArchitecture() rule:
  // layeredArchitecture().consideringAllDependencies()
  //   .layer("Controller").definedBy("..controller..")
  //   ...
  @ArchTest
  static final ArchRule layeredArchitectureRuleShouldBeRespected = noClasses()
    .that().haveSimpleName("__placeholder__")
    .should().accessClassesThat().resideInAPackage("..service..")
    .allowEmptyShould(true);
}
