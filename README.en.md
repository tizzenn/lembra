# Lembra

**Language:** [Español](README.md) · English

Native Android app (Kotlin) for managing recurring alerts/reminders: insurance,
vehicle inspections, service checks, pet vaccines, and so on.

> Built with [Claude Code](https://claude.com/claude-code) (Claude Fable 5 model, Anthropic's Claude 5 family).

## Features

- Records with: title, category, start date, optional time and location,
  how often it repeats, number of repetitions and how many days of advance notice
  (editable).
- Automatic local notification X days before **each** occurrence (not just the last one).
- Records can be edited and deleted (editing/deleting cancels and reschedules the alerts).
- Categories: Car, Motorbike, Home, Pets, Kids, Documents, Licences and
  Miscellaneous — with a filter on the main list and sorting by date (ascending
  or descending).
- Settings: white/black/system theme and a choice of primary and accent colors
  (corporate black and red by default).
- Alarms survive a phone reboot.
- Optional per record: dump every occurrence as events into the phone's calendar
  (you pick which one), with its own reminder and location. Editing or deleting
  the record regenerates or removes its events automatically.

## User guide

Lembra is a notebook of alerts: you create one **record** for each thing that
expires or needs renewing, and the app reminds you on its own with as much lead
time as you tell it. No sign-up, no internet needed, and it never sends anything
anywhere.

### The main screen ("My alerts")

The list of all your records. Each card shows, next to its category icon, the
title, when the **next alert** is —the date and, if you set one, the time too
(`date - time`)— and how many repetitions are left ("3 of 12 left", or
"Finished" once they have all passed).

- **Category filter** (top row): tap *All* or a category to see only those records.
- **Sort** (the ⇅ icon in the bar): sort records by their next date, ascending
  (soonest first) or descending.
- **`+` button** (bottom right): create a new record.
- **Tap a card** to edit it.
- **⋮ menu → Settings**: appearance, category order and backup.

### Creating or editing a record

Tapping `+` opens the form, split into sections. Field by field:

**Basics**

| Field | What it's for |
|-------|---------------|
| **Title** | The name of the alert: "Car inspection", "Home insurance", "Dog's vaccine". It's the only required field along with the date. |
| **Category** | Car, Motorbike, Home, Pets, Kids, Documents, Licences or Miscellaneous. It only sets an icon and lets you filter; pick the closest one. |

**When**

| Field | What it's for |
|-------|---------------|
| **Start date** | The day of the first due date. From here Lembra works out all the following ones. |
| **Time (optional)** | If the time doesn't matter, leave it on "No time" and it will alert in the morning. Set it if the alert must land at a specific hour. |
| **Location (optional)** | Free text (address, garage, clinic). Purely informational. |

**Repetition**

| Field | What it's for |
|-------|---------------|
| **Repeat every** + **Unit** | How often it repeats: e.g. "every **1** year" for the inspection, "every **6** months" for a service. The unit can be Days, Weeks, Months or Years. |
| **Number of repetitions** | How many times in total. Set **1** for a one-off alert (a vaccine that doesn't repeat); set 12 for twelve occurrences, etc. |
| **Advance notice (days)** | How many days before you want the alert. With "15" it warns you 15 days before **each** due date, not just the first. |

**Details**

| Field | What it's for |
|-------|---------------|
| **Notes (optional)** | Any note: policy number, garage phone, whatever. |

**System calendar** (optional) — see the box below.

When you save, Lembra automatically schedules all the alerts. If you later edit
the record, it cancels the old ones and reschedules them; if you delete it, they
are removed. The **Delete** button is inside the edit form.

> 🤓 **Nerd note — how it manages to alert you even if you don't open the app**
>
> Lembra doesn't need to be open or running in the background. When you save a
> record, it "hands off" each notification to Android's own clock (the same
> mechanism an alarm-clock alarm uses). That's why the alerts **survive turning
> the phone off and on**: on reboot, Lembra re-registers every pending alert.
>
> It schedules **one alert per repetition** (not just the next one): if something
> repeats twelve times, it sets all twelve alarms. On phones with very aggressive
> battery saving the alert could arrive a few minutes late; for renewals and
> expiry dates that's irrelevant. If you want maximum punctuality, tell the system
> not to optimize Lembra's battery.

> 🤓 **Nerd note — dumping the alerts into the phone's calendar**
>
> In the form's *System calendar* section you can turn on **"Add to the phone's
> calendar"**. If you enable it, in addition to Lembra's own alert, the app creates
> **a real event in your calendar** (Google Calendar, the manufacturer's, etc.)
> for each repetition, with its own reminder and the location you entered. You
> choose which calendar to write to with **"Target calendar"**.
>
> What's the point? So the alert also shows up in the calendar you share or view
> on your computer, not just as a phone notification. The first time it will ask
> for calendar permission. The nice part: Lembra is in charge of those events — if
> you edit or delete the record, its calendar events **regenerate or delete
> themselves**, with no need to go clean them up by hand. If you leave it off, only
> Lembra itself alerts you.

### Settings

- **Theme**: White, Black or System (follows the phone's light/dark mode).
- **Primary color** and **Accent color**: corporate defaults (black and red); you
  can choose blue, green, purple, teal, orange or pink.
- **Category order**: move them up or down so they appear in the filter in the
  order you use most.
- **Backup**: *Export* saves all your records to a file; *Import* restores them on
  another phone or after reinstalling. Handy when switching phones.

## Installation and updates via F-Droid (recommended)

The app is published in a personal F-Droid repository served with GitHub Pages
([tizzenn/fdroid](https://github.com/tizzenn/fdroid)). To get automatic updates:

1. Open F-Droid on the phone → **Settings → Repositories → +**
2. Add this URL (it already includes the verification fingerprint):
   ```
   https://tizzenn.github.io/fdroid/repo?fingerprint=63D6489D8FC1E7D4076E52B127003666B7805D83189A4F83F4933BB72F8FB144
   ```
3. Search for **Lembra** and install it. Future versions will update on their own.

### Publishing a new version

1. Bump `versionCode` (and `versionName`) in `app/build.gradle.kts`.
2. Commit and create a tag: `git tag v1.2 && git push origin main v1.2`.
3. The **"Publish to F-Droid"** workflow builds the APK, signs it with the release
   key (repo secrets), updates the F-Droid index and creates the GitHub release
   with the APK attached.

The signing key and its passwords live in `C:\Users\rober\lembra-claves\`
(outside the repo). **Back up that folder**: without it no more updates can be
published.

## Debug build

Every push to `main` builds a debug APK (workflow "Build APK"); download it as
the `lembra-debug-apk` artifact from the **Actions** tab.

## Opening the project in Android Studio (optional)

If at some point you want to edit it with Android Studio instead of only with
GitHub Actions, just open the `lembra` folder as an existing project — all the
Gradle setup is done (compileSdk 34, minSdk 26).

## Technical notes

- Local database with Room (no backend or internet involved).
- Alerts are scheduled with `AlarmManager` (exact alarms when the system allows
  it, falling back to inexact ones).
- minSdk 26 to be able to use notification channels reliably.
