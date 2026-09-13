import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CalendarClock, ChevronLeft, Search, ShieldCheck } from "lucide-react";
import { useState, type ReactNode } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { defaultRoute, useAuth } from "../../auth/auth-core";
import { apiClient, type ApiResponse } from "../../api/client";
import "./policy-workspace.css";

type PolicyStatus =
  | "PENDING"
  | "ACTIVE"
  | "EXPIRED"
  | "RENEWED"
  | "CANCEL_REQUESTED"
  | "CANCELLED";
type Policy = {
  id: number;
  policyNumber: string;
  customerId: number;
  customerUsername: string;
  productId: number;
  productName: string;
  nomineeName: string;
  nomineeRelation: string;
  startDate: string;
  endDate: string;
  coverageAmount: number;
  premiumAmount: number;
  renewalPremium: number;
  policyStatus?: PolicyStatus;
  status?: PolicyStatus;
  renewalStatus: string;
  cancellationStatus: string;
};
type Coverage = {
  coverageAmount: number;
  policyStatus: PolicyStatus;
  claimEligible: boolean;
};
type Page<T> = { content: T[]; totalPages: number };
type Product = {
  id: number;
  productName: string;
  coverageAmount: number;
  premiumAmount: number;
  policyTenureMonths: number;
};
type Payment = { id: number; paymentNumber: string; amount: number; paymentDate: string; paymentStatus?: string; status?: string; receiptNumber: string };
type PurchaseRequest = {
  productId: number;
  nomineeName: string;
  nomineeRelation: string;
};

const statuses: PolicyStatus[] = [
  "PENDING",
  "ACTIVE",
  "EXPIRED",
  "RENEWED",
  "CANCEL_REQUESTED",
  "CANCELLED",
];
const policyStatus = (policy: Policy) =>
  policy.policyStatus ?? policy.status ?? "PENDING";
const currency = (value: number) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(value);
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

async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) {
  const response = await request;
  if (!response.data.success) throw new Error(response.data.message);
  return response.data.data;
};

const policyApi = {
  list: (params: Record<string, string | number | undefined>) =>
    unwrap(apiClient.get<ApiResponse<Page<Policy>>>("/api/policies", { params })),
  detail: (id: string) =>
    unwrap(apiClient.get<ApiResponse<Policy>>(`/api/policies/${id}`)),
  coverage: (id: string) =>
    unwrap(apiClient.get<ApiResponse<Coverage>>(`/api/policies/${id}/coverage`)),
  products: () =>
    unwrap(apiClient.get<ApiResponse<Product[]>>("/api/products", { params: { status: "ACTIVE" } })),
  purchase: (request: PurchaseRequest) =>
    unwrap(apiClient.post<ApiResponse<Policy>>("/api/policies/purchase", request)),
  renew: (id: number) =>
    unwrap(apiClient.post<ApiResponse<Policy>>(`/api/policies/${id}/renew`)),
  cancel: (id: number) =>
    unwrap(apiClient.post<ApiResponse<Policy>>(`/api/policies/${id}/cancel`)),
  approveCancellation: (id: number) =>
    unwrap(apiClient.patch<ApiResponse<Policy>>(`/api/policies/${id}/approve-cancellation`)),
  updateStatus: (id: number, status: PolicyStatus) =>
    unwrap(apiClient.patch<ApiResponse<Policy>>(`/api/policies/${id}/status`, { policyStatus: status })),
  payments: (id: number) =>
    unwrap(apiClient.get<ApiResponse<Page<Payment>>>(`/api/payments/policy/${id}`, { params: { page: 0, size: 5 } })),
};

export function PolicyListPage() {
  const { session } = useAuth();
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const policies = useQuery({
    queryKey: ["policies", query, status, page],
    queryFn: () =>
      policyApi.list({
        query: query || undefined,
        status: status || undefined,
        page,
        size: 12,
      }),
  });
  const canPurchase =
    session?.role === "CUSTOMER" ||
    session?.role === "AGENT" ||
    session?.role === "ADMIN";

  return (
    <PolicyFrame
      title={session?.role === "CUSTOMER" ? "My policies" : "Policy management"}
    >
      <section className="policy-toolbar">
        <Search size={18} />
        <input
          value={query}
          onChange={(event) => {
            setQuery(event.target.value);
            setPage(0);
          }}
          placeholder="Search policy number or customer"
        />
        <select
          value={status}
          onChange={(event) => {
            setStatus(event.target.value);
            setPage(0);
          }}
        >
          <option value="">All statuses</option>
          {statuses.map((item) => (
            <option key={item}>{item}</option>
          ))}
        </select>
        {canPurchase && (
          <Link className="policy-primary" to="/policies/purchase">
            Purchase policy
          </Link>
        )}
      </section>
      {policies.isLoading ? (
        <State text="Loading policies..." />
      ) : policies.isError ? (
        <State text="Unable to load policies for this account." error />
      ) : (
        <>
          <section className="policy-table">
            <table>
              <thead>
                <tr>
                  <th>Policy number</th>
                  <th>Product</th>
                  <th>Customer</th>
                  <th>Coverage</th>
                  <th>Premium</th>
                  <th>End date</th>
                  <th>Status</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {policies.data?.content.map((policy) => (
                  <tr key={policy.id}>
                    <td>
                      <b>{policy.policyNumber}</b>
                    </td>
                    <td>{policy.productName}</td>
                    <td>{policy.customerUsername}</td>
                    <td>{currency(policy.coverageAmount)}</td>
                    <td>{currency(policy.renewalPremium)}</td>
                    <td>{date(policy.endDate)}</td>
                    <td>
                      <Status value={policyStatus(policy)} />
                    </td>
                    <td>
                      <button
                        className="table-action"
                        onClick={() => navigate(`/policies/${policy.id}`)}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
          <div className="policy-pagination">
            <button disabled={page === 0} onClick={() => setPage(page - 1)}>
              Previous
            </button>
            <span>
              Page {page + 1} of {policies.data?.totalPages ?? 1}
            </span>
            <button
              disabled={!policies.data || page >= policies.data.totalPages - 1}
              onClick={() => setPage(page + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </PolicyFrame>
  );
}

export function PolicyDetailsPage() {
  const { id = "" } = useParams();
  const { session } = useAuth();
  const cache = useQueryClient();
  const policyQuery = useQuery({
    queryKey: ["policy", id],
    queryFn: () => policyApi.detail(id),
  });
  const coverageQuery = useQuery({
    queryKey: ["policy", id, "coverage"],
    queryFn: () => policyApi.coverage(id),
  });
  const paymentsQuery = useQuery({
    queryKey: ["policy", id, "payments"],
    queryFn: () => policyApi.payments(Number(id)),
  });
  const refresh = () => {
    cache.invalidateQueries({ queryKey: ["policy", id] });
    cache.invalidateQueries({ queryKey: ["policies"] });
  };
  const renew = useMutation({
    mutationFn: () => policyApi.renew(Number(id)),
    onSuccess: refresh,
  });
  const cancel = useMutation({
    mutationFn: () => policyApi.cancel(Number(id)),
    onSuccess: refresh,
  });
  const approve = useMutation({
    mutationFn: () => policyApi.approveCancellation(Number(id)),
    onSuccess: refresh,
  });
  const updateStatus = useMutation({
    mutationFn: (status: PolicyStatus) =>
      policyApi.updateStatus(Number(id), status),
    onSuccess: refresh,
  });
  if (policyQuery.isLoading) return <State text="Loading policy details..." />;
  if (!policyQuery.data || policyQuery.isError)
    return <State text="Unable to load this policy." error />;
  const policy = policyQuery.data;
  const currentStatus = policyStatus(policy);
  const canRenew =
    session?.role !== "CLAIMS_OFFICER" &&
    ["ACTIVE", "EXPIRED", "RENEWED"].includes(currentStatus);
  const canCancel =
    (session?.role === "CUSTOMER" || session?.role === "ADMIN") &&
    ["ACTIVE", "RENEWED"].includes(currentStatus);
  const today = new Date().toISOString().slice(0, 10);
  const showClaimAction =
    session?.role === "CUSTOMER" &&
    ["ACTIVE", "RENEWED"].includes(currentStatus);
  const canFileClaim =
    showClaimAction &&
    policy.startDate <= today &&
    policy.endDate >= today &&
    coverageQuery.data?.claimEligible !== false;

  return (
    <PolicyFrame title={`Policy ${policy.policyNumber}`}>
      <div className="detail-actions">
        <Link to="/policies">
          <ChevronLeft size={17} />
          All policies
        </Link>
        {canRenew && (
          <button
            className="policy-primary"
            onClick={() => renew.mutate()}
            disabled={renew.isPending}
          >
            Renew policy
          </button>
        )}
        {canCancel && (
          <button
            className="outline-action"
            onClick={() => cancel.mutate()}
            disabled={cancel.isPending}
          >
            Request cancellation
          </button>
        )}
        {showClaimAction &&
          (canFileClaim ? (
            <Link
              className="policy-primary"
              to={`/claims/new?policyId=${policy.id}&policyNumber=${encodeURIComponent(policy.policyNumber)}&productName=${encodeURIComponent(policy.productName)}`}
            >
              File claim
            </Link>
          ) : (
            <button
              className="policy-primary"
              type="button"
              disabled
              title="Claims can be filed only during an active policy coverage period."
            >
              File claim unavailable
            </button>
          ))}
        {session?.role === "ADMIN" &&
          policy.cancellationStatus === "REQUESTED" && (
            <button
              className="policy-primary"
              onClick={() => approve.mutate()}
              disabled={approve.isPending}
            >
              Approve cancellation
            </button>
          )}
      </div>
      <section className="detail-grid">
        <InfoCard title="Policy information">
          <Info label="Status" value={<Status value={currentStatus} />} />
          <Info
            label="Effective period"
            value={`${date(policy.startDate)} - ${date(policy.endDate)}`}
          />
          <Info label="Renewal status" value={policy.renewalStatus} />
          <Info label="Cancellation status" value={policy.cancellationStatus} />
        </InfoCard>
        <InfoCard title="Customer information">
          <Info label="Customer" value={policy.customerUsername} />
          <Info label="Nominee" value={policy.nomineeName} />
          <Info label="Relationship" value={policy.nomineeRelation} />
        </InfoCard>
        <InfoCard title="Product and premium">
          <Info label="Product" value={policy.productName} />
          <Info label="Coverage" value={currency(policy.coverageAmount)} />
          <Info
            label="Renewal premium"
            value={currency(policy.renewalPremium)}
          />
        </InfoCard>
        <InfoCard title="Coverage eligibility">
          <Info
            label="Claim eligible"
            value={
              coverageQuery.data?.claimEligible
                ? "Yes - active coverage period"
                : "No"
            }
          />
          <Info
            label="Coverage amount"
            value={
              coverageQuery.data
                ? currency(coverageQuery.data.coverageAmount)
                : "Loading..."
            }
          />
          <Info
            label="Coverage status"
            value={coverageQuery.data?.policyStatus ?? "Loading..."}
          />
        </InfoCard>
        <InfoCard title="Payment summary">
          {paymentsQuery.isLoading ? <p className="unavailable">Loading payment history...</p> : paymentsQuery.isError ? <p className="unavailable">Unable to load payment history.</p> : paymentsQuery.data?.content.length ? paymentsQuery.data.content.map((payment) => <div className="info-row" key={payment.id}><span>{payment.paymentNumber} · {date(payment.paymentDate)}</span><b>{currency(payment.amount)}</b><Link to={`/payments/${payment.id}`}>View</Link></div>) : <p className="unavailable">No payments have been recorded for this policy.</p>}
        </InfoCard>
        <InfoCard title="Claims summary">
          <p className="unavailable">
            Policy-scoped claim history is not available from the backend.
          </p>
        </InfoCard>
      </section>
      {session?.role === "ADMIN" && (
        <section className="admin-status">
          <h2>Policy lifecycle</h2>
          <p>Update platform policy status.</p>
          <select
            value={currentStatus}
            onChange={(event) =>
              updateStatus.mutate(event.target.value as PolicyStatus)
            }
            disabled={updateStatus.isPending}
          >
            {statuses.map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>
        </section>
      )}
    </PolicyFrame>
  );
}

export function PurchasePolicyPage() {
  const navigate = useNavigate();
  const [productId, setProductId] = useState("");
  const [nomineeName, setNomineeName] = useState("");
  const [nomineeRelation, setNomineeRelation] = useState("");
  const products = useQuery({
    queryKey: ["products", "purchase"],
    queryFn: policyApi.products,
  });
  const purchase = useMutation({
    mutationFn: () =>
      policyApi.purchase({
        productId: Number(productId),
        nomineeName,
        nomineeRelation,
      }),
    onSuccess: (policy) => navigate(`/policies/${policy.id}`),
  });
  const selected = products.data?.find(
    (product) => product.id === Number(productId),
  );
  return (
    <PolicyFrame title="Purchase policy">
      <section className="purchase-layout">
        <form
          className="purchase-form"
          onSubmit={(event) => {
            event.preventDefault();
            purchase.mutate();
          }}
        >
          <label>
            Selected product
            <select
              value={productId}
              onChange={(event) => setProductId(event.target.value)}
              required
            >
              <option value="">Choose an active product</option>
              {products.data?.map((product) => (
                <option key={product.id} value={product.id}>
                  {product.productName}
                </option>
              ))}
            </select>
          </label>
          <label>
            Nominee name
            <input
              value={nomineeName}
              onChange={(event) => setNomineeName(event.target.value)}
              maxLength={100}
              required
            />
          </label>
          <label>
            Relationship to nominee
            <input
              value={nomineeRelation}
              onChange={(event) => setNomineeRelation(event.target.value)}
              maxLength={60}
              required
            />
          </label>
          <button className="policy-primary" disabled={purchase.isPending}>
            {purchase.isPending
              ? "Creating policy..."
              : "Confirm policy purchase"}
          </button>
          {purchase.isError && (
            <p className="form-error">{purchase.error.message}</p>
          )}
        </form>
        <article className="purchase-summary">
          <p>SELECTED COVERAGE</p>
          {selected ? (
            <>
              <h2>{selected.productName}</h2>
              <span>{currency(selected.coverageAmount)} coverage</span>
              <b>{currency(selected.premiumAmount)} premium</b>
              <small>{selected.policyTenureMonths} month policy term</small>
            </>
          ) : (
            <p>Select an active product to review its coverage and premium.</p>
          )}
        </article>
      </section>
    </PolicyFrame>
  );
}

export function RenewalCenterPage() {
  const navigate = useNavigate();
  const policies = useQuery({
    queryKey: ["policies", "renewals"],
    queryFn: () => policyApi.list({ page: 0, size: 50 }),
  });
  const renewable =
    policies.data?.content
      .filter((policy) =>
        ["ACTIVE", "EXPIRED", "RENEWED"].includes(policyStatus(policy)),
      )
      .sort((left, right) => left.endDate.localeCompare(right.endDate)) ?? [];
  return (
    <PolicyFrame title="Renewal center">
      <section className="renewal-list">
        {policies.isLoading ? (
          <State text="Loading renewal options..." />
        ) : (
          renewable.map((policy) => (
            <article key={policy.id}>
              <CalendarClock size={20} />
              <div>
                <b>{policy.productName}</b>
                <span>
                  {policy.policyNumber} · Expires {date(policy.endDate)}
                </span>
              </div>
              <strong>{currency(policy.renewalPremium)}</strong>
              <button
                className="table-action"
                onClick={() => navigate(`/policies/${policy.id}`)}
              >
                Review renewal
              </button>
            </article>
          ))
        )}
        {!renewable.length && (
          <State text="No policies are currently available for renewal." />
        )}
      </section>
    </PolicyFrame>
  );
}

function PolicyFrame({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  const { session } = useAuth();

  return (
    <main className="policy-workspace">
      <header>
        <Link to={session ? defaultRoute(session.role) : "/login"}>
          <ChevronLeft size={17} />
          Workspace
        </Link>
        <span>
          <ShieldCheck size={18} />
          Policy Management
        </span>
      </header>
      <section className="policy-title">
        <p>POLICY MANAGEMENT</p>
        <h1>{title}</h1>
      </section>
      {children}
    </main>
  );
}
function InfoCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <article className="policy-info-card">
      <h2>{title}</h2>
      {children}
    </article>
  );
}
function Info({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="info-row">
      <span>{label}</span>
      <b>{value}</b>
    </div>
  );
}
function Status({ value }: { value: PolicyStatus }) {
  return (
    <span className={`policy-status ${value.toLowerCase()}`}>
      {value.replaceAll("_", " ")}
    </span>
  );
}
function State({ text, error }: { text: string; error?: boolean }) {
  return <div className={`policy-state${error ? " error" : ""}`}>{text}</div>;
}
