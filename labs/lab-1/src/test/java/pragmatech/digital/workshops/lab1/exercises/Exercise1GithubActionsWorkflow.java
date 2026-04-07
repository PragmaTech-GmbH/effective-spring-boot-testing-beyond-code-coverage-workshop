package pragmatech.digital.workshops.lab1.exercises;

import org.junit.jupiter.api.Test;

/**
 * Exercise 1: Optimize GitHub Actions Workflow
 *
 * <p>This exercise focuses on CI/CD configuration rather than writing test code.
 *
 * <p>Tasks:
 * <ol>
 *   <li>Review the sample .github/workflows/build.yml in this lab's root directory</li>
 *   <li>Enhance the workflow with these best practices:
 *     <ul>
 *       <li>a) Add Maven dependency caching (use setup-java's built-in cache: maven)</li>
 *       <li>b) Add timeout-minutes to prevent runaway builds</li>
 *       <li>c) Add a step to upload test reports on failure (actions/upload-artifact)</li>
 *       <li>d) Configure the build to run on both push and pull_request events</li>
 *     </ul>
 *   </li>
 *   <li>Review the nightly.yml workflow:
 *     <ul>
 *       <li>a) Understand the cron schedule syntax</li>
 *       <li>b) Note the use of -Dgroups for filtering tagged tests</li>
 *       <li>c) Consider what tests should run nightly vs. on every push</li>
 *     </ul>
 *   </li>
 *   <li>Bonus: Research Maven daemon (mvnd) for faster local builds</li>
 * </ol>
 */
class Exercise1GithubActionsWorkflow {

  @Test
  void reviewWorkflowConfiguration() {
    // TODO: This is a configuration exercise.
    // Review and enhance the .github/workflows/build.yml file.
    // No Java code changes needed for this exercise.
    //
    // Steps:
    // 1. Open .github/workflows/build.yml
    // 2. Add 'cache: maven' to the setup-java step
    // 3. Add 'timeout-minutes: 15' to the build job
    // 4. Add a step to upload test reports on failure using actions/upload-artifact@v4
    // 5. Compare your result with the solution in Solution1GithubActionsWorkflow.java
  }
}
