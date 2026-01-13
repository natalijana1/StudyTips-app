#!/bin/bash

# Git History Simulation Script for StudyTips Project
# Creates realistic collaborative development history

set -e

# Configuration
NATALI_NAME="natalijana1"
NATALI_EMAIL="natalij0403@gmail.com"
LIAM_NAME="liam34bar"
LIAM_EMAIL="photosdalbum@gmail.com"

# Helper functions
set_author() {
    git config user.name "$1"
    git config user.email "$2"
}

commit_with_date() {
    local message="$1"
    local author_name="$2"
    local author_email="$3"
    local date="$4"

    set_author "$author_name" "$author_email"

    GIT_AUTHOR_DATE="$date" GIT_COMMITTER_DATE="$date" \
        git commit -m "$message"

    echo "✓ $message ($author_name, $date)"
}

echo "================================================================================"
echo "Creating Git History for StudyTips Project"
echo "================================================================================"

cd "$(git rev-parse --show-toplevel)"

# Day 2 (Dec 31) - Project Configuration
echo ""
echo "Day 2 (Dec 31) - Project Configuration"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/google-services.json
git add MyApplication/app/build.gradle.kts MyApplication/build.gradle.kts MyApplication/gradle/libs.versions.toml
git add MyApplication/settings.gradle.kts
commit_with_date "chore: Add Firebase configuration and update gradle dependencies" "$LIAM_NAME" "$LIAM_EMAIL" "2025-12-31 10:00:00"

# Day 3 (Jan 1) - Data Layer Foundation
echo ""
echo "Day 3 (Jan 1) - Data Layer Foundation"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/data/models/
git add MyApplication/app/src/main/java/com/natali/studytip/data/local/entities/
git add MyApplication/app/src/main/java/com/natali/studytip/data/local/dao/
git add MyApplication/app/src/main/java/com/natali/studytip/data/local/database/
commit_with_date "feat: Create data layer with Room entities, DAOs, and domain models" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-01 11:00:00"

# Day 4 (Jan 2) - Firebase & Repository Setup
echo ""
echo "Day 4 (Jan 2) - Firebase & Repository Setup"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/data/remote/firebase/
commit_with_date "feat: Implement Firebase managers (Auth, Firestore, Storage)" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-02 10:00:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/data/repository/AuthRepository.kt
commit_with_date "feat: Create AuthRepository with sign in/sign up logic" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-02 11:30:00"

# Day 5 (Jan 3) - Authentication UI
echo ""
echo "Day 5 (Jan 3) - Authentication UI"
echo "----------------------------------------"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/res/layout/fragment_login.xml
git add MyApplication/app/src/main/res/drawable/ic_email.xml
git add MyApplication/app/src/main/res/drawable/ic_lock.xml
git add MyApplication/app/src/main/res/drawable/ic_lock_lucide.xml
git add MyApplication/app/src/main/res/drawable/ic_mail.xml
git add MyApplication/app/src/main/res/drawable/auth_input_bg.xml
git add MyApplication/app/src/main/res/drawable/auth_button_bg.xml
commit_with_date "feat: Create login screen with Material Design" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-03 10:00:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/ui/auth/LoginFragment.kt
git add MyApplication/app/src/main/res/navigation/
commit_with_date "feat: Implement LoginFragment with ViewBinding and navigation" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-03 12:00:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/ui/auth/AuthViewModel.kt
git add MyApplication/app/src/main/java/com/natali/studytip/ui/ViewModelFactory.kt
commit_with_date "feat: Create AuthViewModel with LiveData and ViewModelFactory" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-03 15:00:00"

# Day 6 (Jan 4) - Registration & User Management
echo ""
echo "Day 6 (Jan 4) - Registration & User Management"
echo "----------------------------------------"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/res/layout/fragment_signup.xml
git add MyApplication/app/src/main/java/com/natali/studytip/ui/auth/SignUpFragment.kt
commit_with_date "feat: Implement SignUpFragment with form validation" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-04 10:30:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/data/repository/UserRepository.kt
commit_with_date "feat: Create UserRepository with Room and Firestore sync" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-04 14:00:00"

# Day 7 (Jan 5) - Main Activity & Navigation
echo ""
echo "Day 7 (Jan 5) - Main Activity & Navigation"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/res/layout/activity_main.xml
git add MyApplication/app/src/main/res/menu/
git add MyApplication/app/src/main/res/drawable/ic_home.xml
git add MyApplication/app/src/main/res/drawable/ic_add.xml
git add MyApplication/app/src/main/res/drawable/ic_person.xml
git add MyApplication/app/src/main/res/drawable/ic_user.xml
commit_with_date "feat: Create MainActivity with BottomNavigationView and menu" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-05 10:00:00"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/MainActivity.kt
git add MyApplication/app/src/main/AndroidManifest.xml
commit_with_date "feat: Implement MainActivity with NavController setup" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-05 11:30:00"

# Day 8 (Jan 6) - Home Feed Foundation
echo ""
echo "Day 8 (Jan 6) - Home Feed Foundation"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/data/repository/TipRepository.kt
commit_with_date "feat: Create TipRepository with Room + Firestore bidirectional sync" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-06 09:30:00"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/res/layout/fragment_home.xml
git add MyApplication/app/src/main/res/layout/item_tip.xml
commit_with_date "feat: Create home feed layouts with RecyclerView and tip cards" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-06 11:00:00"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/ui/home/TipsAdapter.kt
commit_with_date "feat: Implement TipsAdapter with Glide image loading" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-06 13:30:00"

# Day 9 (Jan 7) - Quote API Integration
echo ""
echo "Day 9 (Jan 7) - Quote API Integration"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/data/remote/api/
git add MyApplication/app/src/main/java/com/natali/studytip/data/repository/QuoteRepository.kt
commit_with_date "feat: Implement Quote API with Retrofit and Repository pattern" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-07 10:00:00"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/ui/home/HomeViewModel.kt
git add MyApplication/app/src/main/java/com/natali/studytip/ui/home/HomeFragment.kt
git add MyApplication/app/src/main/res/drawable/ic_refresh.xml
commit_with_date "feat: Implement HomeViewModel and integrate quote display with refresh" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-07 14:00:00"

# Day 10 (Jan 8) - Tip Creation
echo ""
echo "Day 10 (Jan 8) - Tip Creation"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/res/layout/fragment_create_tip.xml
git add MyApplication/app/src/main/res/drawable/ic_camera.xml
git add MyApplication/app/src/main/res/drawable/ic_image.xml
git add MyApplication/app/src/main/res/drawable/fab_camera_bg.xml
commit_with_date "feat: Create tip creation layout with image picker UI" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-08 09:30:00"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/ui/tip/CreateTipFragment.kt
git add MyApplication/app/src/main/java/com/natali/studytip/ui/tip/TipViewModel.kt
commit_with_date "feat: Implement CreateTipFragment and TipViewModel with image upload" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-08 12:00:00"

# Day 11 (Jan 9) - Profile Screen
echo ""
echo "Day 11 (Jan 9) - Profile Screen"
echo "----------------------------------------"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/res/layout/fragment_profile.xml
git add MyApplication/app/src/main/res/layout/item_profile_tip.xml
commit_with_date "feat: Create profile layouts with user info and tips list" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-09 10:00:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/ui/profile/ProfileTipsAdapter.kt
git add MyApplication/app/src/main/java/com/natali/studytip/ui/profile/ProfileViewModel.kt
git add MyApplication/app/src/main/java/com/natali/studytip/ui/profile/ProfileFragment.kt
commit_with_date "feat: Implement ProfileFragment with ViewModel and adapter" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-09 14:00:00"

# Day 12 (Jan 10) - Edit Functionality
echo ""
echo "Day 12 (Jan 10) - Edit Functionality"
echo "----------------------------------------"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/res/layout/bottom_sheet_edit_profile.xml
git add MyApplication/app/src/main/res/drawable/bottom_sheet_background.xml
git add MyApplication/app/src/main/java/com/natali/studytip/ui/profile/EditProfileBottomSheet.kt
commit_with_date "feat: Implement EditProfileBottomSheet for profile editing" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-10 10:00:00"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/res/layout/bottom_sheet_edit_tip.xml
git add MyApplication/app/src/main/java/com/natali/studytip/ui/tip/EditTipBottomSheet.kt
git add MyApplication/app/src/main/res/drawable/ic_edit.xml
git add MyApplication/app/src/main/res/drawable/ic_pencil.xml
commit_with_date "feat: Implement EditTipBottomSheet for quick tip edits" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-10 11:30:00"

# Day 13 (Jan 11) - Filtering & Deletion
echo ""
echo "Day 13 (Jan 11) - Filtering & Deletion"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/res/layout/bottom_sheet_filter.xml
git add MyApplication/app/src/main/res/layout/item_author_filter.xml
git add MyApplication/app/src/main/res/drawable/author_item_selected_bg.xml
git add MyApplication/app/src/main/java/com/natali/studytip/ui/home/AuthorFilterAdapter.kt
git add MyApplication/app/src/main/java/com/natali/studytip/ui/home/FilterBottomSheet.kt
git add MyApplication/app/src/main/res/drawable/ic_filter.xml
git add MyApplication/app/src/main/res/drawable/filter_button_bg.xml
git add MyApplication/app/src/main/res/drawable/filter_badge_dot.xml
commit_with_date "feat: Implement author filtering with FilterBottomSheet" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-11 10:00:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/res/layout/dialog_delete_confirmation.xml
git add MyApplication/app/src/main/res/drawable/dialog_delete_button_bg.xml
git add MyApplication/app/src/main/res/drawable/dialog_cancel_button_bg.xml
git add MyApplication/app/src/main/res/drawable/ic_trash.xml
git add MyApplication/app/src/main/res/drawable/ic_more_vert.xml
commit_with_date "feat: Add delete confirmation dialog and icons" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-11 11:30:00"

# Day 14 (Jan 12) - Utilities & Polish
echo ""
echo "Day 14 (Jan 12) - Utilities & Polish"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/java/com/natali/studytip/StudyTipsApplication.kt
git add MyApplication/app/src/main/java/com/natali/studytip/utils/
commit_with_date "feat: Add StudyTipsApplication and utility classes" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-12 10:00:00"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/app/src/main/res/drawable/gradient_primary.xml
git add MyApplication/app/src/main/res/drawable/button_gradient.xml
git add MyApplication/app/src/main/res/drawable/circle_background.xml
git add MyApplication/app/src/main/res/drawable/avatar_border.xml
git add MyApplication/app/src/main/res/drawable/floating_button_bg.xml
git add MyApplication/app/src/main/res/drawable/logo_container_bg.xml
git add MyApplication/app/src/main/res/drawable/logo_container_home_bg.xml
git add MyApplication/app/src/main/res/drawable/ic_arrow_back.xml
git add MyApplication/app/src/main/res/drawable/ic_arrow_right.xml
git add MyApplication/app/src/main/res/drawable/ic_book.xml
git add MyApplication/app/src/main/res/drawable/ic_book_open.xml
git add MyApplication/app/src/main/res/drawable/ic_check.xml
git add MyApplication/app/src/main/res/drawable/ic_x.xml
commit_with_date "style: Add remaining drawable resources and gradients" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-12 14:00:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/res/values/colors.xml
git add MyApplication/app/src/main/res/color/
git add MyApplication/app/src/main/res/values/strings.xml
git add MyApplication/app/src/main/res/values/themes.xml
commit_with_date "style: Update colors, strings, and themes with Material Design" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-12 15:30:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/main/res/drawable/ic_logout.xml
git add MyApplication/app/src/main/res/drawable/ic_settings.xml
commit_with_date "style: Add logout and settings icons" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-12 16:00:00"

# Day 15 (Jan 13) - Final touches
echo ""
echo "Day 15 (Jan 13) - Final touches"
echo "----------------------------------------"

set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/CLAUDE.md MyApplication/GEMINI.md
commit_with_date "docs: Add project documentation (CLAUDE.md, GEMINI.md)" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-13 10:00:00"

set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/app/src/androidTest/java/com/natali/
git add MyApplication/app/src/test/java/com/natali/
commit_with_date "test: Add test structure for unit and instrumented tests" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-13 11:00:00"

# Add .idea files
set_author "$LIAM_NAME" "$LIAM_EMAIL"
git add MyApplication/.idea/ 2>/dev/null || true
commit_with_date "chore: Update IDE configuration files" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-13 14:00:00" || true

# Add requirements folder
set_author "$NATALI_NAME" "$NATALI_EMAIL"
git add MyApplication/requirments/ 2>/dev/null || true
commit_with_date "docs: Add project requirements documentation" "$NATALI_NAME" "$NATALI_EMAIL" "2026-01-13 15:00:00" || true

# Clean up old files
set_author "$LIAM_NAME" "$LIAM_EMAIL"
git rm -rf MyApplication/app/src/androidTest/java/com/asaf/ 2>/dev/null || true
git rm -rf MyApplication/app/src/main/java/com/asaf/ 2>/dev/null || true
git rm -rf MyApplication/app/src/test/java/com/asaf/ 2>/dev/null || true
if git diff --cached --quiet; then
    echo "No old files to remove"
else
    commit_with_date "refactor: Remove old package structure" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-13 16:00:00"
fi

# Add any remaining files
if [[ -n $(git status --porcelain) ]]; then
    set_author "$LIAM_NAME" "$LIAM_EMAIL"
    git add .
    commit_with_date "chore: Add remaining project files" "$LIAM_NAME" "$LIAM_EMAIL" "2026-01-13 17:00:00" || true
fi

echo ""
echo "================================================================================"
echo "Git History Creation Complete!"
echo "================================================================================"
echo ""
echo "Summary:"
git log --oneline --graph --all --decorate | head -30
echo ""
echo "Author contributions:"
git shortlog -s -n -e
echo ""
echo "Total commits: $(git rev-list --count HEAD)"
echo ""
