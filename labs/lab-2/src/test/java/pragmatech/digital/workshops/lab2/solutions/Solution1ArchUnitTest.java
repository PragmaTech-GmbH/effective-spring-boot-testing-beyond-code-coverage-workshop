package pragmatech.digital.workshops.lab2.solutions;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import pragmatech.digital.workshops.lab2.controller.ThreadController;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Solution for Exercise 1 — ArchUnit architecture rules.
 *
 * <p>Note: ThreadController is excluded from these rules as it is an intentional
 * test-helper controller that bypasses the service layer for thread-context experiments.
 */
@AnalyzeClasses(
  packages = "pragmatech.digital.workshops.lab2",
  importOptions = ImportOption.DoNotIncludeTests.class
)
class Solution1ArchUnitTest {

  @ArchTest
  static final ArchRule controllersShouldNotAccessRepositories = noClasses()
    .that().resideInAPackage("..controller..")
    .and().doNotHaveSimpleName("ThreadController")
    .should().accessClassesThat().resideInAPackage("..repository..")
    .because("Controllers must go through the service layer to access data");

  @ArchTest
  static final ArchRule layeredArchitectureRuleShouldBeRespected = layeredArchitecture()
    .consideringAllDependencies()
    .layer("Controller").definedBy("..controller..")
    .layer("Service").definedBy("..service..")
    .layer("Repository").definedBy("..repository..")
    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
    .ignoreDependency(ThreadController.class, BookRepository.class);
}
