package pragmatech.digital.workshops.lab2.experiment;

import java.time.LocalDate;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import pragmatech.digital.workshops.lab2.controller.ThreadController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Demonstrates ArchUnit architecture rules.
 *
 * <p>Note: ThreadController is excluded from certain checks as it is intentionally
 * a test-helper controller that bypasses the service layer for thread-context experiments.
 * In a real project, document and suppress known violations using @SuppressWarnings("ArchUnit").
 */
@AnalyzeClasses(
  packages = "pragmatech.digital.workshops.lab2",
  importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchUnitTest {

  @ArchTest
  static final ArchRule layeredArchitectureRuleShouldBeRespected = layeredArchitecture()
    .consideringAllDependencies()
    .layer("Controller").definedBy("..controller..")
    .layer("Service").definedBy("..service..")
    .layer("Repository").definedBy("..repository..")
    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
    .ignoreDependency(ThreadController.class, pragmatech.digital.workshops.lab2.repository.BookRepository.class);

  @ArchTest
  static final ArchRule controllersShouldNotDirectlyAccessRepositories = noClasses()
    .that().resideInAPackage("..controller..")
    .and().doNotHaveSimpleName("ThreadController")
    .should().accessClassesThat().resideInAPackage("..repository..");

  @ArchTest
  static final ArchRule classesShouldNotCallLocalDateNowDirectly = noClasses()
    .that().resideOutsideOfPackage("..service..")
    .and().resideInAPackage("pragmatech.digital.workshops.lab2..")
    .and().doNotHaveSimpleName("ThreadController")
    .should().callMethod(LocalDate.class, "now");
}
