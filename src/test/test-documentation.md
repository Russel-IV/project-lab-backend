# Test Plan
- **Approach:** Unit tests for service-layer validation and persistence mapping using JUnit 5 with mocked repositories.
- **Scope:** Core entities — User, Stay, Review — focusing on input validation and success paths.
- **Schedule:** Implemented alongside service code changes; run on demand in CI or local test runs.
- **Deliverables:** JUnit 5 test classes in `test/` plus this documentation.
- **Resources:** JUnit 5, Mockito, and the service/repository classes under test.

# Test Execution
- Run `mvn -q test` from the repository root.

# Test Case
- **TC-USER-001 — Create user rejects blank name**
  - **Preconditions:** None.
  - **Test steps:** Call `UserService.createUser` with a blank `name`.
  - **TC-USER-002 — Create user returns persisted response**
  - **Preconditions:** Repository save returns a user with an ID.
  - **Test steps:** Call `UserService.createUser` with a valid `name` and assert response fields.
- **TC-STAY-001 — Create stay rejects negative price**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with `price < 0`.
- **TC-STAY-002 — Create stay returns persisted response**
  - **Preconditions:** Host exists; repository save returns a stay with an ID.
  - **Test steps:** Call `StayService.createStay` with required fields and assert response fields.
- **TC-STAY-003 — Create stay allows zero price**
  - **Preconditions:** Host exists; repository save returns a stay with an ID.
  - **Test steps:** Call `StayService.createStay` with `price = 0`.
- **TC-STAY-004 — Create stay rejects zero sleeps**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with `sleeps = 0`.
- **TC-STAY-005 — Create stay rejects blank name**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with a blank `name`.
- **TC-STAY-006 — Create stay rejects blank street address**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with a blank `streetAddress`.
- **TC-STAY-007 — Create stay rejects blank city**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with a blank `city`.
- **TC-STAY-008 — Create stay rejects negative bathrooms**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with `bathrooms < 0`.
- **TC-STAY-009 — Create stay rejects star rating above five**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with `starRating > 5.0`.
- **TC-STAY-010 — Create stay allows star rating at five**
  - **Preconditions:** Host exists; repository save returns a stay with an ID.
  - **Test steps:** Call `StayService.createStay` with `starRating = 5.0`.
- **TC-STAY-011 — Create stay rejects host id zero**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with `hostId = 0`.
- **TC-STAY-012 — Create stay rejects view ids with zero**
  - **Preconditions:** None.
  - **Test steps:** Call `StayService.createStay` with `viewIds` containing `0`.
- **TC-STAY-013 — Update stay returns not found when missing**
  - **Preconditions:** Stay does not exist.
  - **Test steps:** Call `StayService.updateStay` with a missing ID.
- **TC-STAY-014 — Update stay idempotent for same data**
  - **Preconditions:** Stay exists; repository save returns a stay with an ID.
  - **Test steps:** Call `StayService.updateStay` twice with the same data and compare responses.
- **TC-REVIEW-001 — Create review rejects missing user**
  - **Preconditions:** User ID does not exist; stay ID exists.
  - **Test steps:** Call `ReviewService.createReview` with a missing user ID.
- **TC-REVIEW-002 — Create review returns persisted response**
  - **Preconditions:** User and stay exist; repository save returns a review with an ID.
  - **Test steps:** Call `ReviewService.createReview` and assert response fields.

# Requirements Traceability Matrix (RTM)
| Requirement | Test Case(s) | Status |
| --- | --- | --- |
<<<<<<< HEAD
| R-USER-VAL — User input validation and persistence | TC-USER-001, TC-USER-002 | Ran |
| R-STAY-VAL — Stay input validation and persistence | TC-STAY-001, TC-STAY-002, TC-STAY-003, TC-STAY-004, TC-STAY-005, TC-STAY-006, TC-STAY-007, TC-STAY-008, TC-STAY-009, TC-STAY-010, TC-STAY-011, TC-STAY-012, TC-STAY-013, TC-STAY-014 | Ran |
| R-REVIEW-VAL — Review input validation and persistence | TC-REVIEW-001, TC-REVIEW-002 | Ran |
=======
| R-USER-VAL — User input validation and persistence | TC-USER-001, TC-USER-002 | Not Run |
| R-STAY-VAL — Stay input validation and persistence | TC-STAY-001, TC-STAY-002, TC-STAY-003, TC-STAY-004, TC-STAY-005, TC-STAY-006, TC-STAY-007, TC-STAY-008, TC-STAY-009, TC-STAY-010, TC-STAY-011, TC-STAY-012, TC-STAY-013, TC-STAY-014 | Not Run |
| R-REVIEW-VAL — Review input validation and persistence | TC-REVIEW-001, TC-REVIEW-002 | Not Run |
>>>>>>> 992cf47796d65c491ba33e93261893d6789faca8
