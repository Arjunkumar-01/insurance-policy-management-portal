import { useQuery } from "@tanstack/react-query";
import {
  Bell,
  CalendarClock,
  ChevronLeft,
  ChevronRight,
  ClipboardCheck,
  FileText,
  Home,
  LogOut,
  Menu,
  Search,
  ShieldCheck,
  Users,
} from "lucide-react";
import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/auth-core";
import { ProfileAvatarMenu } from "../../components/navigation/profile-avatar-menu";
import { agentApi, type Customer } from "./agent-api";
import "../dashboard/dashboard.css";
import "./agent-customer-management.css";

const money = (amount: number) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(amount);

export function AgentCustomerManagementPage() {
  const { session, logout } = useAuth();
  const [search, setSearch] = useState("");
  const [submittedSearch, setSubmittedSearch] = useState("");
  const [page, setPage] = useState(0);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const customersQuery = useQuery({
    queryKey: ["agent", "customer-management", submittedSearch, page],
    queryFn: () => agentApi.getCustomers(submittedSearch, page),
  });
  const portfolioQuery = useQuery({
    queryKey: ["agent", "customer-management", selectedCustomer?.username],
    enabled: Boolean(selectedCustomer),
    queryFn: async () => {
      if (!selectedCustomer) throw new Error("Select a customer");
      const [policies, claims, payments] = await Promise.all([
        agentApi.getCustomerPoliciesByUsername(selectedCustomer.username),
        agentApi.getCustomerClaims(selectedCustomer.id),
        agentApi.getCustomerPayments(selectedCustomer.id),
      ]);
      return { policies, claims: claims.content, payments: payments.content };
    },
  });
  const customers = customersQuery.data?.content ?? [];

  return (
    <div className="portal-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark"><ShieldCheck size={22} /></span>
          <span>Insurance Policy<br /><b>Management Portal</b></span>
        </div>
        <nav>
          <Link className="nav-item" to="/agent/dashboard"><Home size={19} />Dashboard</Link>
          <Link className="nav-item" to="/products"><FileText size={19} />Products</Link>
          <Link className="nav-item active" to="/agent/customers"><Users size={19} />Customer Management</Link>
          <Link className="nav-item" to="/policies"><FileText size={19} />Policy Assistance</Link>
          <Link className="nav-item" to="/claims/list"><ClipboardCheck size={19} />Claims Assistance</Link>
          <Link className="nav-item" to="/policies/renewals"><CalendarClock size={19} />Renewals</Link>
        </nav>
        <div className="sidebar-support"><span>Agent workspace</span><b>Customer portfolio support</b></div>
        <div className="profile-mini">
          <span>{session?.fullName.slice(0, 2).toUpperCase()}</span>
          <div><b>{session?.fullName}</b><small>Insurance agent</small></div>
          <button onClick={logout} aria-label="Sign out"><LogOut size={17} /></button>
        </div>
      </aside>
      <section className="content">
        <header className="topbar">
          <button className="mobile-menu" aria-label="Open menu"><Menu size={21} /></button>
          <div className="crumbs">Agent workspace <ChevronRight size={15} /> <b>Customer Management</b></div>
          <div className="header-actions">
            <button aria-label="Notifications" className="icon-button"><Bell size={19} /><i /></button>
            <ProfileAvatarMenu />
          </div>
        </header>
        <main className="dashboard agent-customers">
          <section className="agent-customers-title">
            <div>
              <p className="eyebrow">CUSTOMER MANAGEMENT</p>
              <h1>Customer portfolios</h1>
              <p>Find customers and review their policies, claims, and payments.</p>
            </div>
            <Link className="secondary-action" to="/agent/dashboard"><ChevronLeft size={17} />Dashboard</Link>
          </section>
          <form
            className="agent-customer-search"
            onSubmit={(event) => {
              event.preventDefault();
              setPage(0);
              setSelectedCustomer(null);
              setSubmittedSearch(search.trim());
            }}
          >
            <Search size={19} />
            <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by name, username, or email" />
            <button type="submit">Search</button>
          </form>
          <section className="agent-customer-layout">
            <article className="panel customer-directory">
              <div className="panel-heading">
                <div><p className="eyebrow">DIRECTORY</p><h2>Customers</h2></div>
                <span className="period">{customersQuery.data?.totalElements ?? 0} found</span>
              </div>
              {customersQuery.isLoading ? (
                <div className="customer-state">Loading customers...</div>
              ) : customersQuery.isError ? (
                <div className="customer-state error">Unable to load customers.</div>
              ) : customers.length ? (
                <div className="table-scroll">
                  <table>
                    <thead><tr><th>Customer</th><th>Username</th><th>Status</th><th /></tr></thead>
                    <tbody>
                      {customers.map((customer) => (
                        <tr key={customer.id} className={selectedCustomer?.id === customer.id ? "selected" : undefined}>
                          <td><b>{customer.firstName} {customer.lastName}</b><small>{customer.email}</small></td>
                          <td>{customer.username}</td>
                          <td><span className={`customer-status ${customer.enabled ? "enabled" : "disabled"}`}>{customer.enabled ? "Enabled" : "Disabled"}</span></td>
                          <td><button className="table-action" type="button" onClick={() => setSelectedCustomer(customer)}>View</button></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="customer-state">No customers match this search.</div>
              )}
              <div className="customer-pagination">
                <button type="button" disabled={page === 0 || customersQuery.isFetching} onClick={() => { setPage((current) => current - 1); setSelectedCustomer(null); }}>Previous</button>
                <span>Page {page + 1} of {Math.max(customersQuery.data?.totalPages ?? 1, 1)}</span>
                <button type="button" disabled={!customersQuery.data || page >= customersQuery.data.totalPages - 1 || customersQuery.isFetching} onClick={() => { setPage((current) => current + 1); setSelectedCustomer(null); }}>Next</button>
              </div>
            </article>
            <article className="panel customer-portfolio-detail">
              <div className="panel-heading">
                <div><p className="eyebrow">PORTFOLIO</p><h2>{selectedCustomer ? `${selectedCustomer.firstName} ${selectedCustomer.lastName}` : "Select a customer"}</h2></div>
              </div>
              {!selectedCustomer ? (
                <div className="customer-state">Choose View to inspect a customer portfolio.</div>
              ) : portfolioQuery.isLoading ? (
                <div className="customer-state">Loading portfolio...</div>
              ) : portfolioQuery.isError ? (
                <div className="customer-state error">Unable to load this customer portfolio.</div>
              ) : (
                <>
                  <div className="portfolio-summary">
                    <div><span>Policies</span><b>{portfolioQuery.data?.policies.length ?? 0}</b></div>
                    <div><span>Claims</span><b>{portfolioQuery.data?.claims.length ?? 0}</b></div>
                    <div><span>Payments</span><b>{portfolioQuery.data?.payments.length ?? 0}</b></div>
                  </div>
                  <div className="portfolio-records">
                    <h3>Policies</h3>
                    {portfolioQuery.data?.policies.length ? portfolioQuery.data.policies.map((policy) => (
                      <Link key={policy.id} to={`/policies/${policy.id}`}>
                        <span><b>{policy.policyNumber ?? policy.productName}</b><small>{policy.productName}</small></span>
                        <span>{policy.policyStatus ?? policy.status ?? "UNKNOWN"}<ChevronRight size={16} /></span>
                      </Link>
                    )) : <p>No policies found.</p>}
                    <h3>Recent claims</h3>
                    {portfolioQuery.data?.claims.length ? portfolioQuery.data.claims.slice(0, 5).map((claim) => (
                      <Link key={claim.id} to={`/claims/${claim.id}`}><span><b>Claim #{claim.id}</b></span><span>{claim.claimStatus ?? claim.status ?? "UNKNOWN"}<ChevronRight size={16} /></span></Link>
                    )) : <p>No claims found.</p>}
                    <h3>Recent payments</h3>
                    {portfolioQuery.data?.payments.length ? portfolioQuery.data.payments.slice(0, 5).map((payment) => (
                      <Link key={payment.id} to={`/payments/${payment.id}`}><span><b>Payment #{payment.id}</b><small>{money(payment.amount)}</small></span><span>{payment.paymentStatus ?? payment.status ?? "UNKNOWN"}<ChevronRight size={16} /></span></Link>
                    )) : <p>No payments found.</p>}
                  </div>
                </>
              )}
            </article>
          </section>
        </main>
      </section>
    </div>
  );
}