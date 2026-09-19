# Implementation Plan - UI Cleanup and Interaction Restrictions

This plan covers removing the history, timer display, and restricting the editing action to a 3-tap gesture on the Kisten-ID.

## User Review Required

> [!IMPORTANT]
> - **3-Tap to Edit**: Navigating to the edit screen now requires tapping the `kID` text exactly 3 times. Regular clicks on the card will no longer trigger editing.
> - **History Removed**: The "Recent Searches" bar has been removed.
> - **Timer Hidden**: The countdown timer ("30s") in the search bar is no longer visible, though the auto-clear logic remains active.

## Proposed Changes

### ViewModel
#### [MODIFY] [DashboardViewModel.kt](file:///Users/kaneplarium/AndroidStudioProjects/Lagerverwaltung/app/src/main/java/kaneplarium/lagerverwaltung/ui/dashboard/DashboardViewModel.kt)
- Remove `recentSearches` and `addRecentSearch` logic.

### UI Components
#### [MODIFY] [DashboardScreen.kt](file:///Users/kaneplarium/AndroidStudioProjects/Lagerverwaltung/app/src/main/java/kaneplarium/lagerverwaltung/ui/dashboard/DashboardScreen.kt)
- **DashboardScreen**: Remove `recentSearches` state collection.
- **DashboardScreenContent**:
    - Remove `recentSearches` parameter.
    - Remove History `LazyRow`.
    - Remove `trailingIcon` from search `OutlinedTextField` that displayed the timer.
    - Change "Sperren" button color in the options dialog to match "Bearbeiten" (default).
- **ArticleItem**:
    - Add internal `tapCount` state.
    - Implement a `Modifier.clickable` on the `kID` Text that increments `tapCount` and triggers `onClick` (navigation) only on the 3rd tap.
    - Disable swipe-to-edit to maintain the 3-tap restriction.

## Verification Plan

### Manual Verification
- Verify the "History" bar is gone.
- Verify the search bar no longer shows the seconds timer.
- Open an article card's options: verify "Sperren" is the same color as "Bearbeiten".
- Try to edit an article: confirm it only works after tapping `kID` 3 times.
