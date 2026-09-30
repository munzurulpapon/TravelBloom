# 🌸 TravelBloom — Smart Trip Planner

TravelBloom is a JavaFX desktop app for planning trips end to end: create trips, build itineraries, track transport, stays and expenses, manage a packing list, and get a smart readiness report. The UI uses a modern dark, glassy theme, and every page shows an aesthetic background photo of the trip's destination.

---

## ✨ Features

| Area | What it does |
|------|--------------|
| **Authentication** | Sign up / log in (SHA-256 hashed passwords, SQLite). A welcome email is sent on signup if an email is provided. |
| **Dashboard** | Stats (total trips, upcoming trips, total spent) and trip cards with dynamic destination photos. |
| **My Trips** | Create, edit, delete and search trips. Each card shows a photo of the destination. |
| **Trip Details** | Photo slideshow of the destination (cross-fade every 5 s), live weather (temperature, condition, wind) and coordinates. |
| **Itinerary** | Day / time / activity / location / notes, automatically sorted by day and time. |
| **Transport** | Enter a start and end location and get suggested modes (bus, train, car, flight) with estimated duration and fare. Save the option you pick to your transport history. |
| **Stay** | Hotel search by destination and minimum star rating (live via Makcorps, or offline estimate), plus a manual stay log. |
| **Expenses** | Track spending against the trip budget. A "Suggest" button estimates a price per category and destination. |
| **Packing List** | Packed/unpacked progress bar and smart suggestions based on live weather and trip keywords. A reminder toast shows for 1 min, hides for 30 s, and repeats. |
| **Smart Planner** | Rule-based report per trip: timeline, budget health, and missing transport, stay, itinerary or packing items. |
| **Chat Assistant** | Guided chatbot (destination → days → who you're travelling with) that returns an itinerary, live weather, famous nearby places and shopping tips. |
| **Responsive window** | `SceneManager` keeps fullscreen, maximized and resized states intact across all page changes. |

---

## 🛠 Tech Stack

- **Java 26** and **JavaFX 26** (FXML + CSS)
- **SQLite** via `sqlite-jdbc` (local `travelbloom.db`)
- **Jackson** for JSON
- **Maven** with `javafx-maven-plugin`

### External services

| Service | Used for | Key needed? |
|---------|----------|-------------|
| [Open-Meteo](https://open-meteo.com) | Geocoding and live weather | ❌ No |
| [Wikipedia GeoSearch](https://www.mediawiki.org/wiki/API:Geosearch) | Famous places nearby | ❌ No |
| [Pexels](https://www.pexels.com/api/) | Destination photos | ✅ Free key |
| [Resend](https://resend.com) | Welcome email on signup | ✅ Free key |
| [Makcorps](https://makcorps.com) | Live hotel prices (optional) | ✅ Free trial key (30 calls) |

If a key is missing, the related feature degrades gracefully: no photos gives a gradient background, no email key means the email is skipped, and no Makcorps key means the offline hotel estimator is used.

---

## 🚀 Getting Started

### Prerequisites

- JDK 26 (or change `maven.compiler.source/target` and `javafx.version` in `pom.xml` to a version you have)
- Maven 3.9+

### 1. Clone

```bash
git clone https://github.com/<your-username>/TravelBloom.git
cd TravelBloom
```

### 2. Configure API keys (environment variables)

```bash
# macOS / Linux
export PEXELS_API_KEY=your_pexels_key
export RESEND_API_KEY=your_resend_key
export MAKCORPS_API_KEY=your_makcorps_key   # optional
```

```powershell
# Windows (PowerShell)
$env:PEXELS_API_KEY="your_pexels_key"
$env:RESEND_API_KEY="your_resend_key"
$env:MAKCORPS_API_KEY="your_makcorps_key"
```

In IntelliJ: **Run → Edit Configurations → Environment variables**.

> ⚠️ Never commit real keys. Keep them in environment variables only.

### 3. Run

```bash
mvn clean javafx:run
```

The database (`travelbloom.db`) and all tables are created automatically on first launch.

---

## 📁 Project Structure

```
src/main/java/com/travelbloom/
├── Main.java
├── controller/     # JavaFX controllers (Dashboard, MyTrips, TripDetails, Itinerary,
│                   #   Transport, Stay, Expense, Packing, SmartPlanner, ChatBot, Login, Signup)
├── dao/            # SQLite data access (Trip, Itinerary, Transport, Stay, Expense, Packing, User)
├── database/       # Database connection + schema init
├── model/          # Trip, ItineraryActivity, Transport, Stay, Expense, PackingItem, User, ...
├── service/        # Weather, Location, Places, Image, Email, Hotel, TransportRoute,
│                   #   PriceEstimator, SmartPlanner, PackingSuggestion, TripPlanReport
└── util/           # SceneManager, Session, PasswordUtil, BackgroundPhotoUtil

src/main/resources/
├── view/           # FXML pages
└── style/          # style.css, dark-theme.css, dark-theme-2.css
```

---

## 📝 Notes & Limitations

- **Transport fares and hotel prices are estimates** unless a live hotel API key is configured. There is no free, key-less API for real multi-modal ticket prices.
- Passwords use plain SHA-256 (no per-user salt), which is fine for a local learning project. Use bcrypt or Argon2 for anything production-grade.
- Currency is shown in BDT (৳).
- The Makcorps free trial allows only 30 API calls, and each "Find Hotels" click uses 2.

---

## 🗺 Roadmap

- Trip picker when opening Itinerary / Transport / Stay / Expenses / Packing from the sidebar
- Dark theme for the Create Trip page
- Live transport routing API
- Stronger password hashing

---

## 📄 License

This project is for educational purposes. Add a license of your choice (e.g. MIT) before publishing.
