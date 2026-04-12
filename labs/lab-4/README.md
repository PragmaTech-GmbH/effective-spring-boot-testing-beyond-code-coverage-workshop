# Lab 4 - Tips & Tricks beyond Code Coverage

The grab-bag lab. Same baseline app, now used as a target for mutation testing, architecture rules, and other testing utilities.

## Learning Objectives

- Understand why code coverage is a **vanity metric** and what mutation testing adds
- Run **PIT mutation testing** and read the HTML report
- Write strong assertions that kill boundary mutants
- Enforce architecture rules with **ArchUnit**

## Exercises

### Exercise 1: Run PIT and Kill the Mutants

The `service` package contains `LateReturnFeeCalculator` - a tiered fee calculation with boundary conditions at days 0, 7, and 14.

1. Run PIT: `./mvnw test-compile org.pitest:pitest-maven:mutationCoverage`
2. Open `target/pit-reports/index.html` and find the surviving mutants
3. Look at `LateReturnFeeCalculatorWeakTest` - it achieves 100% line coverage but many mutants survive. Understand **why** weak assertions miss the mutations
4. Implement a stronger test with `LateReturnFeeCalculatorTest` — it uses exact
5. Run PIT again and confirm all mutants are killed


**Hints:**
- Boundary pairs (`day 7` vs `day 8`, `day 14` vs `day 15`) kill the "conditionals boundary" mutator
- `isEqualByComparingTo("7.00")` catches return-value mutations that `isNotNull()` misses
- PIT only runs unit tests (`*Test.java`) — integration tests (`*IT.java`) are excluded for speed

### Exercise 2: Explore the ArchUnit Rules

The `experiment` package contains `ArchUnitTest` with two architecture rules:

1. Run `./mvnw test -Dtest=ArchUnitTest` and verify both rules pass
2. **Challenge:** Add a third rule that forbids classes in `controller` from directly importing classes in `repository` (enforce the service layer boundary)
3. **Challenge:** Add a rule that all `@Service` classes must reside in the `service` package

## How to Run

```bash
cd labs/lab-4
./mvnw verify                                                       
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage        
open target/pit-reports/index.html                                   
```
