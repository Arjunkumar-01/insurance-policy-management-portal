import { useQuery } from "@tanstack/react-query";
import {
  Activity,
  Bell,
  CalendarClock,
  ChevronRight,
  CircleDollarSign,
  ClipboardCheck,
  FileText,
  Home,
  LogOut,
  Menu,
  Search,
  ShieldCheck,
  Users,
  WalletCards,
} from "lucide-react";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/auth-core";
import { ProfileAvatarMenu } from "../../components/navigation/profile-avatar-menu";
import { agentApi, type CustomerPortfolio, type Policy } from "./agent-api";
import "../dashboard/dashboard.css";
import "./agent-dashboard.css";

const money = (amount: number) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(amount);
const date = (value: string | undefined | null) => {
  if (!value) return "N/A";
  try {
    const raw = String(value).trim();
    const dateObj = raw.includes("T") ? new Date(raw) : new Date(`${raw}T00:00:00`);
    if (isNaN(dateObj.getTime())) {
      const fallback = new Date(raw);
      return isNaN(fallback.getTime())
        ? raw
        : new Intl.DateTimeFormat("en-US", { month: "short", day: "numeric", year: "numeric" }).format(fallback);
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
const EMPTY_PORTFOLIOS: CustomerPortfolio[] = [];

function AgentMetric({
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
export function AgentDashboard() {
  const { session, logout } = useAuth();
  const [search, setSearch] = useState("");
  const [submittedSearch, setSubmittedSearch] = useState("");
  const [today] = useState(() => Date.now());
  const customersQuery = useQuery({
    queryKey: ["agent", "customers", submittedSearch],
    queryFn: () => agentApi.getCustomers(submittedSearch),
  });
  const portfoliosQuery = useQuery({
    queryKey: [
      "agent",
      "portfolios",
      customersQuery.data?.content.map((customer) => customer.id),
    ],
    enabled: Boolean(customersQuery.data),
    queryFn: async () => {
      const customers = customersQuery.data?.content ?? [];
      return Promise.all(customers.map(async (customer) => {
        const [policyResult, claimResult, paymentResult] = await Promise.allSettled([
          agentApi.getCustomerPolicies(customer.id),
          agentApi.getCustomerClaims(customer.id),
          agentApi.getCustomerPayments(customer.id),
        ]);
        if (import.meta.env.DEV) {
          [policyResult, claimResult, paymentResult].forEach((result, index) => {
            if (result.status === "rejected") console.error("Agent portfolio request failed", { customerId: customer.id, resource: ["policies", "claims", "payments"][index], error: result.reason });
          });
        }
        const policies = policyResult.status === "fulfilled" ? policyResult.value.content : [];
        const claims = claimResult.status === "fulfilled" ? claimResult.value.content : [];
        const payments = paymentResult.status === "fulfilled" ? paymentResult.value.content : [];
        const activePolicyCount = policies.filter((policy) => (policy.policyStatus ?? policy.status) === "ACTIVE").length;
        const openClaimCount = claims.filter((claim) => ["SUBMITTED", "UNDER_REVIEW"].includes(claim.claimStatus ?? claim.status ?? "")).length;
        const monthlyPremium = payments.filter((payment) => (payment.paymentStatus ?? payment.status) === "SUCCESSFUL").reduce((total, payment) => total + payment.amount, 0);
        return {
          ...customer,
          policies,
          policyCount: policies.length,
          activePolicyCount,
          openClaimCount,
          policyStatus: policies.find((policy) => (policy.policyStatus ?? policy.status) === "ACTIVE")?.policyStatus ?? policies[0]?.policyStatus ?? policies[0]?.status ?? "UNKNOWN",
          monthlyPremium,
        } satisfies CustomerPortfolio;
      }));
    },
    retry: 2,
  });
  const portfolios = portfoliosQuery.data ?? EMPTY_PORTFOLIOS;
  const allPolicies = useMemo(
    () => portfolios.flatMap((customer) => customer.policies),
    [portfolios],
  );
  const activePolicies = allPolicies.filter(
    (policy) => (policy.policyStatus ?? policy.status) === "ACTIVE",
  );
  const renewals = activePolicies
    .filter((policy) => {
      const days =
        (new Date(`${policy.endDate}T00:00:00`).getTime() - today) /
        86_400_000;
      return days >= 0 && days <= 90;
    })
    .sort((a, b) => a.endDate.localeCompare(b.endDate))
    .slice(0, 5);
  const loading = customersQuery.isLoading || portfoliosQuery.isLoading;
  const openClaims = portfolios.reduce((total, portfolio) => total + portfolio.openClaimCount, 0);
  const premiumCollection = portfolios.reduce((total, portfolio) => total + portfolio.monthlyPremium, 0);
  const pendingRequests = allPolicies.filter((policy) => (policy.policyStatus ?? policy.status) === "CANCEL_REQUESTED").length;

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
          <Link className="nav-item" to="/agent/customers">
            <Users size={19} />
            Customer Management
          </Link>
          <Link className="nav-item" to="/policies">
            <FileText size={19} />
            Policy Assistance
          </Link>
          <Link className="nav-item" to="/claims/list">
            <ClipboardCheck size={19} />
            Claims Assistance
          </Link>
          <Link className="nav-item" to="/policies/renewals">
            <CalendarClock size={19} />
            Renewals
          </Link>
          <Link className="nav-item" to="#reports">
            <Activity size={19} />
            Reports
          </Link>
        </nav>
        <div className="sidebar-support">
          <span>Agent workspace</span>
          <b>Customer portfolio support</b>
        </div>
        <div className="profile-mini">
          <span>{session?.fullName.slice(0, 2).toUpperCase()}</span>
          <div>
            <b>{session?.fullName}</b>
            <small>Insurance agent</small>
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
            Agent workspace <ChevronRight size={15} /> <b>Dashboard</b>
          </div>
          <div className="header-actions">
            <button aria-label="Notifications" className="icon-button">
              <Bell size={19} />
              <i />
            </button>
            <ProfileAvatarMenu />
          </div>
        </header>
        <main className="dashboard agent-dashboard" id="dashboard">
          <section className="welcome">
            <div>
              <p className="eyebrow">AGENT WORKSPACE</p>
              <h1>Portfolio overview</h1>
              <p>
                Review customer policy portfolios and upcoming renewal
                opportunities.
              </p>
            </div>
            <Link className="primary-action" to="/agent/customers">
              <Users size={18} />
              Manage customers
            </Link>
          </section>
          <form
            className="agent-search"
            onSubmit={(event) => {
              event.preventDefault();
              setSubmittedSearch(search);
            }}
          >
            <Search size={19} />
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search customers by name, username, or email"
            />
            <button>Search</button>
          </form>
          {customersQuery.isError && <div className="agent-error">Unable to load customer portfolio data. Confirm this account has the Agent role and try again.</div>}
          <>
              <section className="metrics-grid">
                <AgentMetric
                  title="Assigned customers"
                  value={customersQuery.data?.totalElements ?? "—"}
                  note="Customer search portfolio"
                  icon={Users}
                  tone="blue"
                />
                <AgentMetric
                  title="Active policies managed"
                  value={loading ? "—" : activePolicies.length}
                  note="From loaded customer policies"
                  icon={ShieldCheck}
                  tone="teal"
                />
                <AgentMetric
                  title="Open claims"
                  value={loading ? "—" : openClaims}
                  note="Submitted and under-review claims"
                  icon={ClipboardCheck}
                  tone="purple"
                />
                <AgentMetric
                  title="Policies due for renewal"
                  value={loading ? "—" : renewals.length}
                  note="Due in the next 90 days"
                  icon={CalendarClock}
                  tone="amber"
                />
                <AgentMetric
                  title="Monthly premium collection"
                  value={loading ? "—" : money(premiumCollection)}
                  note="Successful payment records"
                  icon={CircleDollarSign}
                  tone="green"
                />
                <AgentMetric
                  title="Pending customer requests"
                  value={loading ? "—" : pendingRequests}
                  note="Cancellation requests in portfolio"
                  icon={WalletCards}
                  tone="slate"
                />
              </section>
              <section className="agent-grid">
                <article className="panel portfolio-panel" id="customers">
                  <div className="panel-heading">
                    <div>
                      <p className="eyebrow">CUSTOMER PORTFOLIO</p>
                      <h2>Customer policy overview</h2>
                    </div>
                    <span className="period">{portfolios.length} loaded</span>
                  </div>
                  <div className="table-scroll">
                    <table>
                      <thead>
                        <tr>
                          <th>Customer name</th>
                          <th>Policy count</th>
                          <th>Active policies</th>
                          <th>Open claims</th>
                          <th>Policy status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {loading ? (
                          <tr>
                            <td colSpan={5} className="table-state">
                              Loading customer portfolios...
                            </td>
                          </tr>
                        ) : (
                          portfolios.map((customer) => (
                            <tr key={customer.id}>
                              <td>
                                <b>
                                  {customer.firstName} {customer.lastName}
                                </b>
                                <small>{customer.email}</small>
                              </td>
                              <td>{customer.policyCount}</td>
                              <td>{customer.activePolicyCount}</td>
                              <td>{customer.openClaimCount}</td>
                              <td>
                                <span
                                  className={`agent-status ${customer.policyStatus.toLowerCase()}`}
                                >
                                  {customer.policyStatus.replaceAll("_", " ")}
                                </span>
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                </article>
                <article className="panel renewals-panel" id="renewals">
                  <div className="panel-heading">
                    <div>
                      <p className="eyebrow">PLAN AHEAD</p>
                      <h2>Upcoming renewals</h2>
                    </div>
                    <CalendarClock size={19} />
                  </div>
                  {loading ? (
                    <div className="empty-list">Loading renewals...</div>
                  ) : renewals.length ? (
                    renewals.map((policy) => (
                      <RenewalRow key={policy.id} policy={policy} />
                    ))
                  ) : (
                    <div className="empty-list">
                      No active policies due within 90 days
                    </div>
                  )}
                </article>
                <article className="panel claims-panel" id="claims">
                  <div className="panel-heading">
                    <div>
                      <p className="eyebrow">CLAIMS ASSISTANCE</p>
                      <h2>Claims assistance queue</h2>
                    </div>
                  </div>
                  <div className="capability-gap"><ClipboardCheck size={18} /><div><b>{openClaims} open claims</b><span>Submitted and under-review claims across the loaded customer portfolio.</span></div></div>
                </article>
                <article className="panel activity-panel">
                  <div className="panel-heading">
                    <div>
                      <p className="eyebrow">RECENT ACTIVITY</p>
                      <h2>Portfolio activity</h2>
                    </div>
                  </div>
                  <div className="capability-gap"><Activity size={18} /><div><b>{portfolios.length} customer portfolios loaded</b><span>Use Customer Management to search the current portfolio and review linked policies, claims, and payments.</span></div></div>
                </article>
                <article className="panel premium-panel" id="reports">
                  <div className="panel-heading">
                    <div>
                      <p className="eyebrow">PREMIUM COLLECTION</p>
                      <h2>Collection summary</h2>
                    </div>
                  </div>
                  <div className="capability-gap"><CircleDollarSign size={18} /><div><b>{money(premiumCollection)} collected</b><span>Successful payment records across the loaded customer portfolio.</span></div></div>
                </article>
              </section>
          </>
        </main>
      </section>
    </div>
  );
}

function RenewalRow({ policy }: { policy: Policy }) {
  return (
    <div className="renewal-row">
      <div>
        <b>{policy.productName}</b>
        <small>
          {policy.customerUsername} · Renews {date(policy.endDate)}
        </small>
      </div>
      <strong>{money(policy.renewalPremium)}</strong>
    </div>
  );
}
