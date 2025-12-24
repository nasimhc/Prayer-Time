# Prayer Time App - Improvement Recommendations

## Critical Issues

### 1. API Key Exposed in Source Code
Your RapidAPI key is hardcoded in `PrayerTimeApi.kt:9`. This is a security risk - anyone who decompiles your APK can steal it.

**Fix:** Move the key to `local.properties` or use BuildConfig fields with Gradle secrets.

---

## High Priority Features

### 2. Fix Current Prayer Detection
The `updateCurrentPrayer()` function in `PrayerTimeViewModel.kt:32-37` is incomplete - it always returns "fajr".

**Implement:** Parse prayer times and compare with current time to highlight the active/next prayer.

### 3. Add Location Support
Currently hardcoded to "dhaka.json". Users should be able to:
- Auto-detect location using GPS
- Manually select city/country
- Save preferred location

### 4. Add Prayer Notifications/Alarms
Users expect to be notified when prayer time arrives. Use:
- `AlarmManager` for scheduling
- `NotificationManager` for alerts
- Allow Adhan sound selection

### 5. Offline Support / Caching
Add Room database to:
- Cache prayer times for offline access
- Store user preferences
- Reduce API calls

---

## Medium Priority Features

### 6. Countdown Timer
Show time remaining until the next prayer - a highly requested feature.

### 7. Qibla Direction
Add a compass showing Qibla direction using device sensors.

### 8. Monthly Prayer Timetable
Display a calendar view with all prayer times for the month.

### 9. Settings Screen
Add preferences for:
- Calculation method (Hanafi, Shafi, etc.)
- Time format (12h/24h)
- Language selection
- Notification preferences
- Dark/Light theme toggle

### 10. Widget Support
Home screen widget showing next prayer time.

---

## Code Quality Improvements

### 11. Dependency Injection
Add Hilt/Koin for better testability and separation of concerns.

### 12. Error Handling
Currently errors are silently ignored in `fetchSunriseSunset()`. Add proper error handling and retry logic.

### 13. Pull-to-Refresh
Allow users to manually refresh prayer times.

### 14. Navigation
Add Jetpack Navigation for multiple screens (Home, Settings, Qibla, etc.)

### 15. Unit Tests
Current test files are just examples. Add proper tests for ViewModel logic.

---

## UI/UX Improvements

### 16. Dark Mode Support
Your theme has dark mode setup but the UI hardcodes colors like `Color(0xFFF8F9FA)`.

### 17. Animated Transitions
Add subtle animations when switching between prayers.

### 18. Islamic Date Display
Show Hijri date alongside Gregorian date.

### 19. Display Date & Location
Show the current date and selected city prominently.

---

## Quick Wins (Easy to Implement)

| Feature | Effort |
|---------|--------|
| Show date in UI | Low |
| Pull-to-refresh | Low |
| Time format toggle (12h/24h) | Low |
| Fix current prayer logic | Medium |
| Add countdown timer | Medium |
