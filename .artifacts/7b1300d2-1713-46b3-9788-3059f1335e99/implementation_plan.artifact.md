# Implementation Plan - Restrict and Fix Categories

This plan describes the changes required to remove the ability to create new categories and restrict the application to a fixed set of categories with specific colors and icons.

## User Review Required

> [!IMPORTANT]
> This change will remove the "add category" functionality from the UI. Existing categories that do not match the new fixed set will be effectively "hidden" or should be migrated to the new categories.

## Proposed Changes

### Data Module

#### [MODIFY] [FirstLaunchInitializer.kt](file:///C:/Users/PRATHMESH/AndroidStudioProjects/ring_2/app/src/main/java/com/example/ringapp/data/FirstLaunchInitializer.kt)
- Update the default categories to match the required set:
    - **Gym**: Red (`0xFFD32F2F`), Icon: `fitness`
    - **Health**: Green (`0xFF388E3C`), Icon: `fitness`
    - **Study**: Blue (`0xFF1976D2`), Icon: `school`
    - **Personal**: Yellow (`0xFFFBC02D`), Icon: `person`
    - **Work**: Purple (`0xFF7B1FA2`), Icon: `work`
- Update default habits to use these new category IDs.

### App Module

#### [MODIFY] [AddHabitScreen.kt](file:///C:/Users/PRATHMESH/AndroidStudioProjects/ring_2/app/src/main/java/com/example/ringapp/ui/habits/AddHabitScreen.kt)
- Remove `addCategory` from `AddHabitViewModel`.
- Remove the "add+" button from `AddHabitScreen`.
- Remove `showCategoryDialog` and the associated `AlertDialog`.
- Update `iconFor` to support the new category icons.
- Remove filters that exclude the "habits" category (as it won't be created anymore).

#### [MODIFY] [HabitListScreen.kt](file:///C:/Users/PRATHMESH/AndroidStudioProjects/ring_2/app/src/main/java/com/example/ringapp/ui/habits/HabitListScreen.kt)
- Update `categoryIcon` mapping.
- Ensure filters only show the fixed categories.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/PRATHMESH/AndroidStudioProjects/ring_2/app/src/main/java/com/example/ringapp/ui/home/HomeScreen.kt)
- Update `categoryIcon` mapping.

#### [MODIFY] [AnalyticsScreens.kt](file:///C:/Users/PRATHMESH/AndroidStudioProjects/ring_2/app/src/main/java/com/example/ringapp/ui/analytics/AnalyticsScreens.kt)
- Update category filtering to remove the "habits" exclusion if necessary, and ensure consistency with the new category set.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors after removing `CreateCategoryUseCase` calls.

### Manual Verification
- Deploy the app to a fresh device/emulator.
- Verify that only the 5 specified categories (Gym, Health, Study, Personal, Work) are available.
- Verify that the colors and icons match the requirements.
- Verify that there is no option to add a new category in the "Add Habit" screen.
- Verify that filters in "Habit List" and "Analytics" correctly reflect these categories.
