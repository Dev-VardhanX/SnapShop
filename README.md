# 🛍️ SnapShop

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="SnapShop Logo" width="120" style="border-radius: 28px; box-shadow: 0 10px 30px rgba(99, 102, 241, 0.3);" />
</p>

<h3 align="center">AI-Powered Visual Shopping & Smart Price Comparison for Android</h3>

<p align="center">
  Snap any item with your camera or search by text to find the best e-commerce deals across India and global merchants in seconds.
</p>

<p align="center">
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-2026.02-4285F4.svg?style=for-the-badge&logo=android&logoColor=white" alt="Compose" /></a>
  <a href="https://ai.google.dev/"><img src="https://img.shields.io/badge/Gemini%201.5%20Flash-Multimodal%20Vision-8E75FF.svg?style=for-the-badge&logo=google&logoColor=white" alt="Gemini" /></a>
  <a href="https://serpapi.com/"><img src="https://img.shields.io/badge/SerpApi-Google%20Shopping-00C49F.svg?style=for-the-badge" alt="SerpApi" /></a>
  <a href="https://developers.google.com/ml-kit"><img src="https://img.shields.io/badge/ML%20Kit-On--Device%20Vision-FF6F00.svg?style=for-the-badge&logo=google&logoColor=white" alt="ML Kit" /></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat-square&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Min%20SDK-24-informational.svg?style=flat-square" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-37-success.svg?style=flat-square" alt="Target SDK" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange.svg?style=flat-square" alt="Architecture" />
  <img src="https://img.shields.io/badge/UI%20Style-Tactile%20Claymorphic-indigo.svg?style=flat-square" alt="UI Style" />
</p>

---

## 📖 Overview

**SnapShop** is a modern, high-performance Android e-commerce utility designed to simplify deal discovery and price comparison. Combining multimodal artificial intelligence with live search engine aggregation, SnapShop allows shoppers to photograph any physical object—clothing, gadgets, footwear, accessories, furniture—and immediately view real-time prices, merchant offers, ratings, and buying links.

Designed with an ultra-tactile **Claymorphic UI**, SnapShop combines soft-pill 3D physical aesthetics, dynamic spring micro-animations, and fluid gesture interactions for a modern native Android experience.

---

## ✨ Key Features

### 📸 AI Visual Search & Image Recognition
- **Live Camera Scanner**: Integrated with **CameraX** for high-framerate real-time framing, tap-to-focus, and auto-exposure.
- **Gallery Import**: Pick any existing snapshot or screenshot directly from your photo gallery.
- **Gemini 1.5 Flash Multimodal Vision**: Extracts fine-grained product attributes including brand, exact model name, color scheme, product category, and target demographics.
- **Algorithmic Query Expansion**: Automatically synthesizes multi-tier commercial search queries to maximize catalog hit rates.
- **On-Device Vision Fallback**: Powered by **Google ML Kit Image Labeling** for rapid zero-latency local fallback if network connectivity is limited.

### 🔍 Real-Time Price Comparison
- **Live Google Shopping Aggregation**: Real-time pricing across major retailers (Amazon, Flipkart, Croma, Myntra, Reliance Digital, AJIO, Tata CLiQ, etc.) via SerpApi.
- **Direct Merchant Resolver**: Resolves affiliate redirects and opens direct product listings inside native merchant apps (Flipkart, Amazon) or your default browser.
- **Smart Sorting & Filtering**: Filter by store, custom price range sliders, or sort by lowest price, highest price, rating, or popularity.

### 🎨 Custom 4-Layer Claymorphic Design System
- Built entirely with Jetpack Compose custom draw modifiers (`drawWithCache` and Canvas APIs).
- **Physical Depth**: Combines soft tinted drop shadows, ambient top-left specular highlights, subtle linear body gradients, and inner bevel shading.
- **Tactile Responses**: Interactive spring physics on press/release (`Spring.DampingRatioMediumBouncy`) with sunk inset depth states for inputs and selected filters.

### 🗄️ Offline-First Local Storage (Room Database)
- **Wishlist**: Bookmark favorite items with instant add/remove toggles and reactive count badges.
- **Recently Viewed**: Keep track of inspected products for quick revisit.
- **Search History**: Instant search term recall with single-tap replay and history clearing.

---

## 📱 App Screens

| Screen | Description |
|---|---|
| **Splash Screen** | Branded clay badge entry with smooth fade-in routing. |
| **Home Screen** | Greeting header, instant search bar, quick Camera/Gallery launchers, category chips, and trending product deals. |
| **Image Search Screen** | Real-time CameraX viewfinder, scanning radar animation, gallery picker, focus rect, and AI recognition summary chips. |
| **Search Results Screen** | Two-column responsive clay grid, store badges, star ratings, bottom-sheet filter & sort controls. |
| **Product Details Screen** | High-res hero image, merchant identification, pricing, star reviews, related offers carousel, and direct deal launcher. |
| **Wishlist Screen** | Stored favorite products with offline access and one-tap purchasing. |
| **Search History** | Search term timeline with individual deletion and complete wipe options. |
| **Recently Viewed** | History of browsed products for seamless comparison. |
| **Settings Screen** | Cache management, API key status indicators, and app diagnostics. |

---

## 🏗️ Architecture & Tech Stack

SnapShop adheres to **Modern Android Architecture** guidelines using Clean Architecture principles, MVVM (Model-View-ViewModel), and Unidirectional Data Flow (UDF).

```
com.snapshop.app/
├── data/
│   ├── local/            # Room Database, DAOs, and Entities
│   │   ├── dao/          # SearchHistoryDao, WishlistDao, RecentlyViewedDao
│   │   └── entity/       # SearchHistoryEntity, WishlistEntity, RecentlyViewedEntity
│   ├── remote/           # API services & DTOs
│   │   ├── gemini/       # Google Gemini Vision Multimodal Service
│   │   └── SerpApi/      # Google Shopping Engine via SerpApi
│   └── repository/       # ProductRepository, GeminiVisionRepository
├── domain/               # Domain models (Product, RecognizedProductInfo)
├── navigation/           # Compose Navigation graph and destination definitions
├── ui/                   # Jetpack Compose UI layer
│   ├── components/       # Reusable Clay components (Cards, Buttons, Sheets, NavBar)
│   ├── details/          # Product details screen & ViewModel
│   ├── history/          # Search history screen & ViewModel
│   ├── home/             # Home dashboard screen & ViewModel
│   ├── imagesearch/      # CameraX & AI visual search screen & ViewModel
│   ├── recentlyviewed/   # Recently viewed screen & ViewModel
│   ├── search/           # Search results screen & ViewModel
│   ├── settings/         # Settings screen & ViewModel
│   ├── theme/            # 4-layer Claymorphism engine, Colors, Type, Theme
│   └── wishlist/         # Wishlist screen & ViewModel
└── util/                 # Merchant URL resolver, deep-link utilities
```

### Core Technologies

| Layer | Technology |
|---|---|
| **Language** | [Kotlin 2.2](https://kotlinlang.org/) (Coroutines, Flow, StateFlow) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) (BOM 2026.02, Material 3) |
| **Navigation** | [Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose) |
| **Camera & Vision** | [CameraX 1.5](https://developer.android.com/training/camerax), [Google ML Kit Image Labeling](https://developers.google.com/ml-kit/vision/image-labeling) |
| **AI Inference** | [Google Gemini 1.5 Flash](https://ai.google.dev/) (Multimodal REST API) |
| **E-Commerce Search** | [SerpApi](https://serpapi.com/) (Google Shopping Engine) |
| **Networking** | [Retrofit 3.0](https://square.github.io/retrofit/), [OkHttp 5.2](https://square.github.io/okhttp/) (Logging Interceptor), [Gson](https://github.com/google/gson) |
| **Image Loading** | [Coil Compose 2.7](https://coil-kt.github.io/coil/compose/) (Async image pipeline with disk caching) |
| **Local Database** | [Android Room 2.8](https://developer.android.com/training/data-storage/room) with [KSP](https://kotlinlang.org/docs/ksp-overview.html) |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1+) or newer
- **JDK 17** or **JDK 21**
- **Android SDK** with `compileSdk 37` and `minSdk 24`
- A physical Android device or emulator with camera support

### 1. Clone the Repository

```bash
git clone https://github.com/Dev-VardhanX/SnapShop.git
cd SnapShop
```

### 2. Configure API Keys

SnapShop utilizes **SerpApi** for product discovery and **Google Gemini** for visual product identification.

Create or update the `local.properties` file in the root project directory:

```properties
## local.properties
sdk.dir=/path/to/your/Android/sdk

# SerpApi API Key (for Google Shopping results)
# Get a free key at https://serpapi.com/
SERPAPI_KEY=your_serpapi_key_here

# Google Gemini API Key (for Multimodal Visual AI)
# Get a free key at https://aistudio.google.com/
GEMINI_API_KEY=your_gemini_api_key_here
```

> **Note**: Both keys are injected into `BuildConfig` at compile time and are ignored by Git to protect your credentials.

### 3. Build & Run

**Using Gradle CLI:**
```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

**Using Android Studio:**
1. Open the project in Android Studio.
2. Allow Gradle sync to complete.
3. Select your target physical device or emulator.
4. Click **Run (`Shift + F10`)**.

---

## 🔮 Design System Showcase

SnapShop implements a proprietary **Claymorphic Canvas Engine** defined in `ClaymorphicSystem.kt`:

```
   ┌───────────────────────────────────────────────┐
   │  ⚪ Soft Light Specular (Top-Left Shadow)     │
   │  ┌─────────────────────────────────────────┐  │
   │  │ 🔆 Linear Gradient Surface               │  │
   │  │    Top: Light Tint                      │  │
   │  │    Bottom: Soft Shade                   │  │
   │  │                                         │  │
   │  │ 🔘 Inner Bevel Highlight & Shadow       │  │
   │  └─────────────────────────────────────────┘  │
   │  ⚫ Tinted Ambient Drop Shadow (Bottom-Right) │
   └───────────────────────────────────────────────┘
```

- **`ClayCard`**: Elevated tactile container with rounded geometry and configurable shadow depths.
- **`ClayButton`**: Pressable pill button featuring spring scaling (`0.96f`) and color transitions.
- **`ClayBadge`**: High-contrast indicator pills for product discounts and store tags.
- **`clayDepth(inset = true)`**: Inverts shadow direction to create realistic pressed-in clay surfaces for search fields and active selection chips.

---

## 🔒 Permissions

| Permission | Reason |
|---|---|
| `android.permission.CAMERA` | Required to provide live camera feed for visual product search. |
| `android.permission.INTERNET` | Required to communicate with SerpApi, Gemini API, and load product images. |

---

<p align="center">
  Crafted with ❤️ for Android by <a href="https://github.com/Dev-VardhanX">Dev-VardhanX</a>
</p>
