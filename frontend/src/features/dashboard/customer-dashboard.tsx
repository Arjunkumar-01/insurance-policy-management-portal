import {
  Activity,
  Bell,
  CalendarClock,
  ChevronRight,
  CircleDollarSign,
  ClipboardCheck,
  CreditCard,
  FileText,
  Home,
  LogOut,
  Menu,
  ShieldCheck,
  WalletCards,
} from "lucide-react";
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { useEffect, useState } from "react";
import { useAuth } from "../../auth/auth-core";
import { ProfileAvatarMenu } from "../../components/navigation/profile-avatar-menu";
import { Link } from "react-router-dom";
import { useDashboard } from "./use-dashboard";
import type { ClaimStatus, PolicyStatus } from "./dashboard-api";
import "./dashboard.css";

const formatCurrency = (value: number) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(value);
const formatDate = (value: string | undefined | null) => {
  if (!value) return "N/A";
  try {
    const raw = String(value).trim();
    const dateObj = raw.includes("T") ? new Date(raw) : new Date(`${raw}T00:00:00`);
    if (isNaN(dateObj.getTime())) {
      const fallback = new Date(raw);
      if (!isNaN(fallback.getTime())) {
        return new Intl.DateTimeFormat("en-US", {
          month: "short",
          day: "numeric",
          year: "numeric",
        }).format(fallback);
      }
      return raw;
    }
    return new Intl.DateTimeFormat("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    }).format(dateObj);
  } catch {
    return String(value);
  }
};
const claimLabel: Record<string, string> = {
  SUBMITTED: "Submitted",
  UNDER_REVIEW: "In review",
  APPROVED: "Approved",
  REJECTED: "Rejected",
  SETTLED: "Settled",
};

function getGreeting(hour: number) {
  if (hour < 5) return "Good Night";
  if (hour < 12) return "Good Morning";
  if (hour < 17) return "Good Afternoon";
  return "Good Evening";
}

function Status({ status }: { status: PolicyStatus | ClaimStatus }) {
  return (
    <span className={`status status-${status.toLowerCase()}`}>
      {status.replaceAll("_", " ")}
    </span>
  );
}

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
  icon: typeof ShieldCheck;
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

function PageLoader() {
  return (
    <main className="loading-page">
      <ShieldCheck size={28} />
      <p>Loading your policy overview...</p>
    </main>
  );
}

export function CustomerDashboard() {
  const { session, logout } = useAuth();
  const [greeting, setGreeting] = useState(() => getGreeting(new Date().getHours()));
  const { data, isLoading, error } = useDashboard();
  useEffect(() => {
    const updateGreeting = () => setGreeting(getGreeting(new Date().getHours()));
    const interval = window.setInterval(updateGreeting, 60_000);
    return () => window.clearInterval(interval);
  }, []);
  if (isLoading) return <PageLoader />;
  if (error || !data)
    return (
      <main className="loading-page">
        <ShieldCheck size={28} />
        <p>
          We could not load your dashboard. Sign in with a customer account and
          try again.
        </p>
      </main>
    );

  const { report, policies, claims, payments } = data;
  const paid = (payments ?? []).filter(
    (payment) =>
      payment.status === "SUCCESS" ||
      (payment.status as string) === "SUCCESSFUL" ||
      (payment as { paymentStatus?: string }).paymentStatus === "SUCCESSFUL",
  );
  const paidTotal = paid.reduce((total, payment) => total + payment.amount, 0);
  const reviewClaims = (claims ?? []).filter(
    (claim) => claim.status === "SUBMITTED" || claim.status === "UNDER_REVIEW",
  );
  const upcomingRenewals = (policies ?? [])
    .filter((policy) => policy.status === "ACTIVE" || (policy as { policyStatus?: string }).policyStatus === "ACTIVE")
    .sort((a, b) => (a.endDate ?? "").localeCompare(b.endDate ?? ""))
    .slice(0, 3);
  const paymentChart = paid
    .slice(0, 6)
    .reverse()
    .map((payment) => {
      let dateLabel = "N/A";
      try {
        const d = new Date(payment.paymentDate);
        if (!isNaN(d.getTime())) {
          dateLabel = d.toLocaleDateString("en-US", {
            month: "short",
            day: "numeric",
          });
        }
      } catch {
        dateLabel = "N/A";
      }
      return {
        date: dateLabel,
        amount: payment.amount,
      };
    });
  const activity = [
    ...(claims ?? []).map((claim) => ({
      label: `Claim ${claim.claimNumber ?? claim.id}`,
      detail: claimLabel[claim.status] ?? claim.status ?? "Submitted",
      date: claim.incidentDate ?? (claim as { submittedDate?: string }).submittedDate ?? "",
      icon: ClipboardCheck,
    })),
    ...(payments ?? []).map((payment) => ({
      label: `Payment ${payment.paymentReference ?? (payment as { paymentNumber?: string }).paymentNumber ?? payment.id}`,
      detail: formatCurrency(payment.amount),
      date: payment.paymentDate ?? "",
      icon: CreditCard,
    })),
  ]
    .filter((item) => Boolean(item.date))
    .sort((a, b) => b.date.localeCompare(a.date))
    .slice(0, 4);

  return (
    <div className="portal-shell">
      <aside className="sidebar">
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
          <Link className="nav-item active" to="#dashboard">
            <Home size={19} />
            Dashboard
          </Link>
          <Link className="nav-item" to="/products">
            <FileText size={19} />
            Products
          </Link>
          <Link className="nav-item" to="/policies">
            <FileText size={19} />
            My Policies
          </Link>
          <Link className="nav-item" to="/claims/list">
            <ClipboardCheck size={19} />
            Claims
          </Link>
          <Link className="nav-item" to="/payments/dashboard">
            <WalletCards size={19} />
            Payments
          </Link>
          <Link className="nav-item" to="/profile">
            <ShieldCheck size={19} />
            Profile
          </Link>
        </nav>
        <div className="sidebar-support">
          <span>Need help?</span>
          <b>Contact support</b>
        </div>
        <div className="profile-mini">
          <span>{session?.fullName.slice(0, 2).toUpperCase()}</span>
          <div>
            <b>{session?.fullName}</b>
            <small>Policyholder</small>
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
            Customer portal <ChevronRight size={15} /> <b>Dashboard</b>
          </div>
          <div className="header-actions">
            <button aria-label="Notifications" className="icon-button">
              <Bell size={19} />
              <i />
            </button>
            <ProfileAvatarMenu />
          </div>
        </header>
        <main className="dashboard" id="dashboard">
          <section className="welcome">
            <div>
              <p className="eyebrow">POLICYHOLDER DASHBOARD</p>
              <h1>{greeting}, {session?.fullName ?? "Customer"}</h1>
              <p>
                Here is a current view of your insurance coverage and activity.
              </p>
            </div>
            <Link className="primary-action" to="/payments/new">
              <CircleDollarSign size={18} />
              Make a payment
            </Link>
          </section>
          <section className="metrics-grid">
            <Metric
              title="Total policies"
              value={policies.length}
              note="Across all coverage"
              icon={ShieldCheck}
              tone="blue"
            />
            <Metric
              title="Active policies"
              value={report.activePolicies}
              note="Coverage in force"
              icon={Activity}
              tone="teal"
            />
            <Metric
              title="Claims submitted"
              value={report.claimsSubmitted}
              note="All time"
              icon={ClipboardCheck}
              tone="purple"
            />
            <Metric
              title="Claims in review"
              value={reviewClaims.length}
              note="Awaiting a decision"
              icon={FileText}
              tone="amber"
            />
            <Metric
              title="Total premium paid"
              value={formatCurrency(paidTotal)}
              note={`${paid.length} successful payments`}
              icon={CreditCard}
              tone="green"
            />
            <Metric
              title="Pending payments"
              value="0"
              note="No balance API available"
              icon={WalletCards}
              tone="slate"
            />
          </section>
          <section className="dashboard-grid">
            <article className="panel chart-panel">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">PAYMENT SUMMARY</p>
                  <h2>Premium payments</h2>
                </div>
                <span className="period">Recent activity</span>
              </div>
              <div className="chart-wrap">
                {paymentChart.length ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={paymentChart}>
                      <defs>
                        <linearGradient
                          id="paymentFill"
                          x1="0"
                          x2="0"
                          y1="0"
                          y2="1"
                        >
                          <stop
                            offset="0%"
                            stopColor="#2563eb"
                            stopOpacity={0.26}
                          />
                          <stop
                            offset="100%"
                            stopColor="#2563eb"
                            stopOpacity={0}
                          />
                        </linearGradient>
                      </defs>
                      <CartesianGrid vertical={false} stroke="#e8eef7" />
                      <XAxis
                        dataKey="date"
                        tickLine={false}
                        axisLine={false}
                        tick={{ fill: "#74829a", fontSize: 12 }}
                      />
                      <YAxis
                        tickFormatter={(value) => `$${value}`}
                        tickLine={false}
                        axisLine={false}
                        tick={{ fill: "#74829a", fontSize: 12 }}
                        width={45}
                      />
                      <Tooltip
                        formatter={(value) => formatCurrency(Number(value))}
                      />
                      <Area
                        type="monotone"
                        dataKey="amount"
                        stroke="#2563eb"
                        strokeWidth={3}
                        fill="url(#paymentFill)"
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="empty-chart">
                    Your completed payments will appear here.
                  </div>
                )}
              </div>
            </article>
            <article className="panel tracker">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">CLAIM STATUS</p>
                  <h2>Claims tracker</h2>
                </div>
                <a href="#claims">View all</a>
              </div>
              {claims.length ? (
                claims.slice(0, 3).map((claim) => (
                  <div className="tracker-row" key={claim.id}>
                    <div className="tracker-icon">
                      <ClipboardCheck size={18} />
                    </div>
                    <div>
                      <b>{claim.claimNumber}</b>
                      <small>
                        {claim.policyNumber} · {formatDate(claim.incidentDate)}
                      </small>
                    </div>
                    <Status status={claim.status} />
                  </div>
                ))
              ) : (
                <div className="empty-list">No claims submitted</div>
              )}
            </article>
            <article className="panel policies">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">COVERAGE</p>
                  <h2>Policy overview</h2>
                </div>
                <a href="#policies">View policies</a>
              </div>
              <div className="policy-list">
                {policies.slice(0, 3).map((policy) => (
                  <div className="policy-row" key={policy.id}>
                    <div className="policy-logo">
                      <ShieldCheck size={20} />
                    </div>
                    <div>
                      <b>{policy.productName}</b>
                      <small>{policy.policyNumber}</small>
                    </div>
                    <Status status={policy.status} />
                    <ChevronRight className="chevron" size={18} />
                  </div>
                ))}
              </div>
            </article>
            <article className="panel activity-feed">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">TIMELINE</p>
                  <h2>Recent activity</h2>
                </div>
                <a href="#activity">View history</a>
              </div>
              {activity.length ? (
                activity.map((item, index) => {
                  const Icon = item.icon;
                  return (
                    <div
                      className="activity-row"
                      key={`${item.label}-${index}`}
                    >
                      <span>
                        <Icon size={16} />
                      </span>
                      <div>
                        <b>{item.label}</b>
                        <small>{item.detail}</small>
                      </div>
                      <time>{formatDate(item.date)}</time>
                    </div>
                  );
                })
              ) : (
                <div className="empty-list">No recent activity</div>
              )}
            </article>
            <article className="panel renewals">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">PLAN AHEAD</p>
                  <h2>Upcoming renewals</h2>
                </div>
                <CalendarClock size={19} />
              </div>
              {upcomingRenewals.length ? (
                upcomingRenewals.map((policy) => (
                  <div className="renewal-row" key={policy.id}>
                    <div>
                      <b>{policy.productName}</b>
                      <small>Renews {formatDate(policy.endDate)}</small>
                    </div>
                    <strong>{formatCurrency(policy.renewalPremium)}</strong>
                  </div>
                ))
              ) : (
                <div className="empty-list">
                  No active policy renewals found
                </div>
              )}
            </article>
          </section>
        </main>
      </section>
    </div>
  );
}
