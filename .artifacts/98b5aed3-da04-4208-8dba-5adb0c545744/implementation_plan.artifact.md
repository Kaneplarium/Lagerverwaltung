# Implementation Plan - Switch to 3rd-Swipe Navigation

This plan covers changing the edit-screen navigation trigger from a triple-tap to a triple-swipe-left gesture, while cleaning up residual code from previous features (History, Timer UI).

## User Review Required

> [!IMPORTANT]
> - **3-Swipe to Edit**: To navigate to the article update screen, you must now swipe an article card from **right to left** exactly **3 times**. A small counter ("1/3", "2/3") will appear in the background during the swipe.
> - **History and Timer Clean-up**: All remaining logic and UI components for the search history and visual countdown timer will be removed.

## Proposed Changes

### UI Components
#### [MODIFY] [DashboardScreen.kt](file:///Users/kaneplarium/AndroidStudioProjects/Lagerverwaltung/app/src/main/java/kaneplarium/lagerverwaltung/ui/dashboard/DashboardScreen.kt)
- **DashboardScreen**: Remove `recentSearches` state collection and pass.
- **DashboardScreenContent**:
    - Remove `recentSearches` parameter.
    - Remove History UI and Timer display logic.
    - Clean up unused imports (`LazyRow`, `History`, `SuggestionChip`).
- **ArticleItem**:
    - Replace `tapCount` with `swipeLeftCount`.
    - Update `SwipeToDismissBox` for `EndToStart` direction to increment `swipeLeftCount`.
    - Trigger navigation only when `swipeLeftCount == 3`.
    - Add a visual counter (`1/3`, `2/3`) in the swipe-to-edit background.
    - Remove `clickable` logic from `kID` text.

## Verification Plan

### Manual Verification
- Swipe an article card to the left 3 times and verify it opens the edit screen.
- Verify the counter appears in the background during the swipe.
- Confirm the "History" bar and timer text are completely gone.
- Check that "Sperren" in the options menu still has the same color as "Bearbeiten" (if still present) or matches the primary theme.
