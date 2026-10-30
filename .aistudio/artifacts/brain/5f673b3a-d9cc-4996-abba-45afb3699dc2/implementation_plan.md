# Implementation Plan - Enterprise Cisco/Ubiquiti Style UI/UX Modernization

## Goal
Transform the OLT Manager Pro app into a high-end, premium enterprise-grade network management dashboard inspired by Cisco and Ubiquiti design systems. This includes deep navy dark mode with Cyan & Electric Blue accents, off-white light mode, glassmorphic headers with profile avatars and live status dots, pill-shaped floating navigation bars with glow, and glow ring charts.

## Proposed Changes

### 1. Theming & Color Palette (`Theme.kt`)
- **Dark Mode:** Deep Navy background (`#0B132B`, `#1C2541`), Cyan & Electric Blue primary accents (`#00F2FE`, `#4FACFE`), neon status glows.
- **Light Mode:** Crisp Off-White background (`#F8FAFC`, `#FFFFFF`), Slate & Electric Blue accents (`#0284C7`).
- Typography & Material 3 dynamic color styling.

### 2. Enterprise Header (`DashboardScreen.kt`, `GlobalComponents.kt`)
- Glassmorphism header design with translucent blur effect, live status dot indicator, and user profile avatar placeholder.

### 3. Floating Navigation Bar (`MainActivity.kt` or `MainLayout`)
- Pill-shaped floating bottom navigation bar with subtle neon glow, smooth scale/elevation animations, and active state indicators.

### 4. Data Visualization & Glow Charts (`GlobalComponents.kt`)
- Glow ring charts with neon gradient strokes, arc gauges with optimal/warning status indicators, and clean card containers.

## Verification Plan
- Compile applet to verify clean build and error-free Compose rendering.
