import { useQuery } from "@tanstack/react-query";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  Activity,
  BarChart3,
  Bell,
  ChevronRight,
  CircleDollarSign,
  ClipboardCheck,
  Home,
  FileText,
  LogOut,
  Menu,
  Settings,
  ShieldCheck,
  Users,
  WalletCards,
} from "lucide-react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/auth-core";
import { ProfileAvatarMenu } from "../../components/navigation/profile-avatar-menu";
import { adminApi } from "./admin-api";
import "../dashboard/dashboard.css";
import "./admin-dashboard.css";

const currency = (value: number) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(value);
const COLORS = ["#0a2f78", "#2563eb", "#14b8a6", "#8b5cf6", "#f59e0b"];
function Metric({
  title,
  value,
  note,
  icon: Icon,
  tone,
}: {
  title: string;
  value: string | number;
  note: string;
  icon: typeof Users;
  tone: string;
}) {
  return (
    <article className="metric-card">
      <div className={`metric-icon ${tone}`}>
        <Icon size={19} />
      </div>
      <p>{title}</p>
      <strong>{value}</strong>
      <span>{note}</span>
    </article>
  );
}
function AdminShell({ children }: { children: React.ReactNode }) {
  const { session, logout } = useAuth();
  const location = useLocation();
  const nav = [
    { label: "Dashboard", path: "/admin/dashboard", icon: Home },
    { label: "Users", path: "/admin/users", icon: Users },
    { label: "Products", path: "/products", icon: FileText },
    { label: "Policies", path: "/admin/policies", icon: ShieldCheck },
    { label: "Cancellations", path: "/admin/policies/cancellations", icon: ClipboardCheck },
    { label: "Claims", path: "/claims", icon: ClipboardCheck },
    { label: "Payments", path: "/payments/dashboard", icon: WalletCards },
    { label: "Reports", path: "/admin/reports", icon: BarChart3 },
  ];
  return (
    <div className="portal-shell">
      <aside className="sidebar admin-sidebar">
        <div className="brand">
          <span className="brand-mark">
            <ShieldCheck size={22} />
          </span>
          <span>
            Insurance Policy
            <br />
            <b>Management Portal</b>
          </span>
        </div>
        <nav>
          {nav.map((item) => {
            const Icon = item.icon;
            return (
              <Link
                key={item.path}
                className={`nav-item${location.pathname === item.path ? " active" : ""}`}
                to={item.path}
              >
                <Icon size={19} />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="sidebar-support">
          <span>Administrator workspace</span>
          <b>Platform operations</b>
        </div>
        <div className="profile-mini">
          <span>{session?.fullName.slice(0, 2).toUpperCase()}</span>
          <div>
            <b>{session?.fullName}</b>
            <small>Administrator</small>
          </div>
          <button onClick={logout} aria-label="Sign out">
            <LogOut size={17} />
          </button>
        </div>
      </aside>
      <section className="content">
        <header className="topbar">
          <button className="mobile-menu" aria-label="Open menu">
            <Menu size={21} />
          </button>
          <div className="crumbs">
            Administrator workspace <ChevronRight size={15} /> <b>Platform</b>
          </div>
          <div className="header-actions">
            <button aria-label="Notifications" className="icon-button">
              <Bell size={19} />
              <i />
            </button>
            <ProfileAvatarMenu />
          </div>
        </header>
        {children}
      </section>
    </div>
  );
}

export function AdminDashboard() {
  const overview = useQuery({
    queryKey: ["admin", "overview"],
    queryFn: adminApi.overview,
  });
  const report = useQuery({
    queryKey: ["admin", "report"],
    queryFn: adminApi.report,
  });
  const data = overview.data;
  const analytics = report.data;
  const distribution =
    analytics?.productPerformance.map((item) => ({
      name: item.productName,
      value: item.policyCount,
    })) ?? [];
  return (
    <AdminShell>
      <main className="dashboard admin-dashboard">
        <section className="welcome">
          <div>
            <p className="eyebrow">EXECUTIVE OVERVIEW</p>
            <h1>Platform performance</h1>
            <p>
              A current view of users, policies, claims, and premium activity.
            </p>
          </div>
          <Link className="primary-action" to="/admin/users">
            <Users size={18} />
            Manage users
          </Link>
        </section>
        {overview.isError || report.isError ? (
          <div className="admin-error">
            Unable to load administrator analytics. Confirm this account has the
            Admin role.
          </div>
        ) : (
          <>
            <section className="metrics-grid">
              <Metric
                title="Total customers"
                value={data?.totalCustomers ?? "—"}
                note="Registered policyholders"
                icon={Users}
                tone="blue"
              />
              <Metric
                title="Total policies"
                value={data?.totalPolicies ?? "—"}
                note="Across the platform"
                icon={ShieldCheck}
                tone="teal"
              />
              <Metric
                title="Total claims"
                value={data?.totalClaims ?? "—"}
                note="All submitted claims"
                icon={ClipboardCheck}
                tone="purple"
              />
              <Metric
                title="Monthly premium revenue"
                value={analytics ? currency(analytics.premiumCollection) : "—"}
                note="Collection total from reports"
                icon={CircleDollarSign}
                tone="green"
              />
              <Metric
                title="Claims settlement ratio"
                value={analytics ? `${analytics.claimsRatio}%` : "—"}
                note="Current report ratio"
                icon={Activity}
                tone="amber"
              />
              <Metric
                title="Active users"
                value={data?.totalCustomers ?? "—"}
                note="Enabled user total unavailable"
                icon={Users}
                tone="slate"
              />
            </section>
            <section className="admin-analytics">
              <ChartPanel
                title="Revenue trend"
                subtitle="MONTHLY PREMIUM REVENUE"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={analytics?.monthlyRevenue ?? []}>
                    <defs>
                      <linearGradient id="revenue" x1="0" x2="0" y1="0" y2="1">
                        <stop
                          offset="0%"
                          stopColor="#2563eb"
                          stopOpacity=".28"
                        />
                        <stop
                          offset="100%"
                          stopColor="#2563eb"
                          stopOpacity="0"
                        />
                      </linearGradient>
                    </defs>
                    <CartesianGrid vertical={false} stroke="#e8eef7" />
                    <XAxis
                      dataKey="month"
                      tickLine={false}
                      axisLine={false}
                      tick={{ fill: "#74829a", fontSize: 11 }}
                    />
                    <YAxis
                      tickLine={false}
                      axisLine={false}
                      tick={{ fill: "#74829a", fontSize: 11 }}
                      tickFormatter={(value) => `$${value}`}
                    />
                    <Tooltip formatter={(value) => currency(Number(value))} />
                    <Area
                      type="monotone"
                      dataKey="revenue"
                      stroke="#2563eb"
                      strokeWidth={3}
                      fill="url(#revenue)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </ChartPanel>
              <ChartPanel
                title="Product performance"
                subtitle="POLICIES BY PRODUCT"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={analytics?.productPerformance ?? []}
                    layout="vertical"
                  >
                    <XAxis type="number" hide />
                    <YAxis
                      dataKey="productName"
                      type="category"
                      width={92}
                      tickLine={false}
                      axisLine={false}
                      tick={{ fill: "#66758b", fontSize: 11 }}
                    />
                    <Tooltip />
                    <Bar
                      dataKey="policyCount"
                      fill="#14b8a6"
                      radius={[0, 4, 4, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </ChartPanel>
              <ChartPanel title="Policy distribution" subtitle="PRODUCT MIX">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={distribution}
                      dataKey="value"
                      nameKey="name"
                      innerRadius={48}
                      outerRadius={76}
                      paddingAngle={3}
                    >
                      {distribution.map((item, index) => (
                        <Cell
                          key={item.name}
                          fill={COLORS[index % COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </ChartPanel>
              <article className="panel analytics-note">
                <p className="eyebrow">CLAIMS ANALYTICS</p>
                <h2>Claims processing data</h2>
                <p>
                  Claims ratio is provided by the administrator report. Status
                  distribution, user growth, payment history, and recent system
                  activities are not exposed by current administrator APIs.
                </p>
                <Link to="/admin/reports">View reporting details</Link>
              </article>
            </section>
            <section className="quick-actions">
              <p className="eyebrow">MANAGEMENT OVERVIEW</p>
              <h2>Quick actions</h2>
              <div>
                <Link to="/admin/users">
                  <Users size={18} />
                  Manage users
                </Link>
                <Link to="/admin/reports">
                  <BarChart3 size={18} />
                  View reports
                </Link>
              </div>
            </section>
          </>
        )}
      </main>
    </AdminShell>
  );
}
function ChartPanel({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <article className="panel chart-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">{subtitle}</p>
          <h2>{title}</h2>
        </div>
      </div>
      <div className="admin-chart">{children}</div>
    </article>
  );
}

export function AdminManagementPage({
  section,
}: {
  section:
    | "users"
    | "policies"
    | "reports"
    | "payments"
    | "claims";
}) {
  const title = {
    users: "User management",
    policies: "Policy management",
    reports: "Reports and analytics",
    payments: "Payments",
    claims: "Claims management",
  }[section];
  return (
    <AdminShell>
      <main className="dashboard admin-dashboard management-page">
        <section className="welcome">
          <div>
            <p className="eyebrow">ADMINISTRATION</p>
            <h1>{title}</h1>
            <p>
              Platform management data provided by the current service APIs.
            </p>
          </div>
        </section>
        {section === "users" ? (
          <UserTable />
        ) : section === "policies" ? (
          <PolicyTable />
        ) : section === "reports" ? (
          <ReportsPanel />
        ) : (
          <CapabilityPage section={section} />
        )}
      </main>
    </AdminShell>
  );
}
function UserTable() {
  const query = useQuery({
    queryKey: ["admin", "customers"],
    queryFn: adminApi.customers,
  });
  return (
    <TablePanel
      title="Users"
      columns={["Name", "Email", "Role", "Status", "Created date"]}
      loading={query.isLoading}
      error={query.isError}
    >
      {query.data?.content.map((user) => (
        <tr key={user.id}>
          <td>
            <b>
              {user.firstName} {user.lastName}
            </b>
            <small>{user.username}</small>
          </td>
          <td>{user.email}</td>
          <td>
            <span className="role-pill">{user.role.replaceAll("_", " ")}</span>
          </td>
          <td>
            <span className={user.enabled ? "enabled" : "disabled"}>
              {user.enabled ? "Enabled" : "Disabled"}
            </span>
          </td>
          <td>
            <span className="not-available">Not provided</span>
          </td>
        </tr>
      ))}
    </TablePanel>
  );
}
function PolicyTable() {
  const query = useQuery({
    queryKey: ["admin", "policies"],
    queryFn: adminApi.policies,
  });
  return (
    <TablePanel
      title="Policies"
      columns={[
        "Policy number",
        "Customer",
        "Product",
        "Renewal premium",
        "End date",
        "Status",
      ]}
      loading={query.isLoading}
      error={query.isError}
    >
      {query.data?.content.map((policy) => (
        <tr key={policy.id}>
          <td>
            <b>{policy.policyNumber}</b>
          </td>
          <td>{policy.customerUsername}</td>
          <td>{policy.productName}</td>
          <td>{currency(policy.renewalPremium)}</td>
          <td>{policy.endDate}</td>
          <td>
            <span className="role-pill">{policy.status}</span>
          </td>
        </tr>
      ))}
    </TablePanel>
  );
}
function ReportsPanel() {
  const query = useQuery({
    queryKey: ["admin", "report"],
    queryFn: adminApi.report,
  });
  return (
    <article className="panel report-summary">
      <p className="eyebrow">REPORTING</p>
      <h2>Administrator metrics</h2>
      {query.isLoading ? (
        <p>Loading report data...</p>
      ) : query.data ? (
        <>
          <div>
            <strong>{currency(query.data.premiumCollection)}</strong>
            <span>Premium collection</span>
          </div>
          <div>
            <strong>{query.data.claimsRatio}%</strong>
            <span>Claims ratio</span>
          </div>
          <p>
            Monthly revenue and product performance are available on the
            administrator dashboard.
          </p>
        </>
      ) : (
        <p>Unable to load report data.</p>
      )}
    </article>
  );
}
function CapabilityPage({ section }: { section: string }) {
  return (
    <article className="panel capability-page">
      <Settings size={25} />
      <h2>
        {section === "claims"
          ? "Claims queue available in claims workspace"
          : `${section.charAt(0).toUpperCase() + section.slice(1)} management unavailable`}
      </h2>
      <p>
        {section === "claims"
          ? "The implemented claims queue is available at /claims/queue. The administrator API does not expose full claim history or settlement processing."
          : section === "payments"
            ? "The backend provides payment history only for Customers. No administrator payment-management or payment analytics endpoint exists."
            : "No platform configuration endpoint is available in the Spring Boot backend."}
      </p>
    </article>
  );
}
function TablePanel({
  title,
  columns,
  loading,
  error,
  children,
}: {
  title: string;
  columns: string[];
  loading: boolean;
  error: boolean;
  children: React.ReactNode;
}) {
  return (
    <article className="panel management-table">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">PLATFORM DATA</p>
          <h2>{title}</h2>
        </div>
      </div>
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              {columns.map((column) => (
                <th key={column}>{column}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td className="table-state" colSpan={columns.length}>
                  Loading {title.toLowerCase()}...
                </td>
              </tr>
            ) : error ? (
              <tr>
                <td className="table-state" colSpan={columns.length}>
                  Unable to load {title.toLowerCase()}.
                </td>
              </tr>
            ) : (
              children
            )}
          </tbody>
        </table>
      </div>
    </article>
  );
}
