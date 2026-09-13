# IPMP - Frontend (React + TypeScript + Vite)

The frontend Single Page Application (SPA) for the **Insurance Policy Management Portal (IPMP)**.

---

## 🛠 Tech Stack

- **Framework:** React 18.3.1
- **Language:** TypeScript 6
- **Build Tool:** Vite 8.2.2
- **Routing:** React Router DOM 6.30.6
- **State & Caching:** TanStack React Query v5
- **HTTP Client:** Axios
- **Form Management:** React Hook Form + Zod
- **Icons & Charts:** Lucide React, Recharts

---

## 📁 Directory Structure

```
frontend/
├── public/                    # Static browser assets (favicon.svg)
├── src/
│   ├── api/                   # Axios instance, interceptors, error handling
│   ├── app/                   # Root AppRouter & route definitions
│   ├── auth/                  # AuthProvider, ProtectedRoute, Login/Register pages
│   ├── components/            # Shared UI components (ProfileAvatarMenu)
│   ├── features/              # Role-specific workspaces and business modules
│   │   ├── admin/             # Admin dashboard & management panels
│   │   ├── agent/             # Agent customer portfolio
│   │   ├── claims/            # Customer & Officer claims workflows
│   │   ├── claims-officer/    # Claims Officer shell layout
│   │   ├── dashboard/         # Customer dashboard & widgets
│   │   ├── payments/          # Payment dashboard, history, & receipt views
│   │   ├── policies/          # Policy list, details, purchase, cancellations
│   │   ├── products/          # Product catalog & compare modal
│   │   ├── profile/           # Customer profile management
│   │   └── users/             # Admin user management
│   ├── index.css              # Global styles & design system reset
│   └── main.tsx               # Application entry point
├── .env.production.example    # Production environment template
├── package.json               # NPM scripts & dependencies
└── vite.config.ts             # Vite build configuration
```

---

## 🚀 Getting Started

### 1. Install Dependencies
```bash
npm install
```

### 2. Run Development Server
```bash
npm run dev
```
*Accessible at `http://localhost:5173`.*

### 3. Build for Production
```bash
# For local preview:
npm run build

# For AWS S3 deployment:
$env:VITE_API_BASE_URL = "http://<EC2_PUBLIC_DNS>:8080"
npm run build
```
*The compiled, optimized static bundle is output to `dist/`.*

### 4. Linting
```bash
npm run lint
```

