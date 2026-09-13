import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { ChevronLeft, CreditCard, Search, ShieldCheck } from "lucide-react";
import { useState, type ReactNode } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { defaultRoute, useAuth } from "../../auth/auth-core";
import { apiClient, type ApiResponse } from "../../api/client";
import "./payment-workspace.css";

type PaymentStatus =
  | "PENDING"
  | "PROCESSING"
  | "SUCCESSFUL"
  | "FAILED"
  | "REFUNDED";
type PaymentMethod =
  | "CREDIT_CARD"
  | "DEBIT_CARD"
  | "NET_BANKING"
  | "UPI"
  | "INSURANCE_WALLET";
type Payment = {
  id: number;
  paymentNumber: string;
  policyId: number;
  customerId: number;
  policyNumber: string;
  amount: number;
  paymentDate: string;
  paymentMethod: PaymentMethod;
  paymentStatus?: PaymentStatus;
  status?: PaymentStatus;
  transactionReference: string;
  invoiceNumber: string;
  receiptNumber: string;
  failureReason?: string;
};
type Receipt = {
  paymentId: number;
  paymentNumber: string;
  policyNumber: string;
  customerUsername: string;
  amount: number;
  paymentDate: string;
  invoiceNumber: string;
  receiptNumber: string;
  transactionReference: string;
};
type Analytics = {
  totalPayments: number;
  pendingPayments: number;
  processingPayments: number;
  successfulPayments: number;
  failedPayments: number;
  refundedPayments: number;
  totalPremiumCollected: number;
  successRate: number;
  failureRate: number;
};
type Revenue = {
  totalPremiumCollected: number;
  monthlyRevenue: { key: string; amount: number }[];
};
type Page<T> = { content: T[]; totalPages: number };
type Policy = {
  policyNumber: string;
  productName: string;
  renewalPremium: number;
  policyStatus?: string;
  status?: string;
};
const methods: PaymentMethod[] = [
  "CREDIT_CARD",
  "DEBIT_CARD",
  "NET_BANKING",
  "UPI",
  "INSURANCE_WALLET",
];
const getStatus = (payment: Payment) =>
  payment.paymentStatus ?? payment.status ?? "PENDING";
const money = (value: number) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(value);
const date = (value?: string) =>
  value
    ? new Intl.DateTimeFormat("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
      }).format(new Date(value))
    : "Not available";
async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) {
  const response = await request;
  if (!response.data.success) throw new Error(response.data.message);
  return response.data.data;
}
const paymentApi = {
  mine: (page: number) =>
    unwrap(
      apiClient.get<ApiResponse<Page<Payment>>>("/api/payments/my", {
        params: { page, size: 20 },
      }),
    ),
  all: (page: number) =>
    unwrap(
      apiClient.get<ApiResponse<Page<Payment>>>("/api/payments", {
        params: { page, size: 20 },
      }),
    ),
  detail: (id: string) =>
    unwrap(apiClient.get<ApiResponse<Payment>>(`/api/payments/${id}`)),
  receipt: (id: string) =>
    unwrap(apiClient.get<ApiResponse<Receipt>>(`/api/payments/${id}/receipt`)),
  analytics: () =>
    unwrap(apiClient.get<ApiResponse<Analytics>>("/api/payments/analytics")),
  revenue: () =>
    unwrap(apiClient.get<ApiResponse<Revenue>>("/api/payments/revenue")),
  pay: (body: {
    policyNumber: string;
    amount: number;
    paymentMethod: PaymentMethod;
  }) => unwrap(apiClient.post<ApiResponse<Payment>>("/api/payments", body)),
  policies: () =>
    unwrap(apiClient.get<ApiResponse<Policy[]>>("/api/policies/me")),
};

export function PaymentDashboardPage() {
  const { session } = useAuth();
  const admin = session?.role === "ADMIN";
  const payments = useQuery({
    queryKey: ["payments", "dashboard", session?.role],
    queryFn: () => (admin ? paymentApi.all(0) : paymentApi.mine(0)),
    enabled: session?.role === "CUSTOMER" || admin,
  });
  const analytics = useQuery({
    queryKey: ["payments", "analytics"],
    queryFn: paymentApi.analytics,
    enabled: admin,
  });
  const revenue = useQuery({
    queryKey: ["payments", "revenue"],
    queryFn: paymentApi.revenue,
    enabled: admin,
  });
  const list = payments.data?.content ?? [];
  const successful = list.filter(
    (payment) => getStatus(payment) === "SUCCESSFUL",
  );
  const total = successful.reduce((sum, payment) => sum + payment.amount, 0);
  const data = analytics.data;
  return (
    <PaymentFrame title={admin ? "Payment analytics" : "Payment dashboard"}>
      <section className="payment-actions">
        {session?.role === "CUSTOMER" && (
          <Link className="payment-primary" to="/payments/new">
            <CreditCard size={17} />
            Make payment
          </Link>
        )}
        <Link className="payment-outline" to="/payments/history">
          Payment history
        </Link>
      </section>
      {payments.isError ? (
        <State text="Unable to load payment data." error />
      ) : (
        <>
          <section className="payment-metrics">
            <Metric
              label={admin ? "Total revenue" : "Total premium paid"}
              value={money(admin ? (data?.totalPremiumCollected ?? 0) : total)}
              tone="green"
            />
            <Metric
              label="Successful payments"
              value={
                admin ? (data?.successfulPayments ?? 0) : successful.length
              }
              tone="blue"
            />
            <Metric
              label="Failed payments"
              value={
                admin
                  ? (data?.failedPayments ?? 0)
                  : list.filter((payment) => getStatus(payment) === "FAILED")
                      .length
              }
              tone="slate"
            />
            <Metric
              label={admin ? "Collection rate" : "Upcoming payments"}
              value={admin ? `${data?.successRate ?? 0}%` : "—"}
              tone="purple"
            />
          </section>
          <section className="payment-grid">
            <article className="payment-panel">
              <p>REVENUE TREND</p>
              <h2>
                {admin ? "Monthly collections" : "Recent premium activity"}
              </h2>
              <div className="revenue-chart">
                {admin && revenue.data?.monthlyRevenue.length ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={revenue.data.monthlyRevenue}>
                      <CartesianGrid vertical={false} stroke="#e8eef7" />
                      <XAxis dataKey="key" tickLine={false} axisLine={false} />
                      <YAxis tickLine={false} axisLine={false} />
                      <Tooltip formatter={(value) => money(Number(value))} />
                      <Area
                        type="monotone"
                        dataKey="amount"
                        stroke="#2563eb"
                        fill="#dbeafe"
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                ) : (
                  <State
                    text={
                      admin
                        ? "Loading revenue data..."
                        : "Revenue analytics are available to administrators."
                    }
                  />
                )}
              </div>
            </article>
            <article className="payment-panel">
              <p>RECENT TRANSACTIONS</p>
              <h2>Latest payments</h2>
              {list.slice(0, 4).map((payment) => (
                <Link
                  className="payment-recent"
                  key={payment.id}
                  to={`/payments/${payment.id}`}
                >
                  <div>
                    <b>{payment.paymentNumber}</b>
                    <span>
                      {payment.policyNumber} · {date(payment.paymentDate)}
                    </span>
                  </div>
                  <strong>{money(payment.amount)}</strong>
                  <Status value={getStatus(payment)} />
                </Link>
              ))}
            </article>
          </section>
        </>
      )}
    </PaymentFrame>
  );
}

export function PaymentHistoryPage() {
  const { session } = useAuth();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("");
  const query = useQuery({
    queryKey: ["payments", "history", session?.role, page],
    queryFn: () =>
      session?.role === "CUSTOMER"
        ? paymentApi.mine(page)
        : paymentApi.all(page),
  });
  const list =
    query.data?.content.filter(
      (payment) => !status || getStatus(payment) === status,
    ) ?? [];
  return (
    <PaymentFrame title="Payment history">
      <section className="payment-toolbar">
        <Search size={18} />
        <span>Transactions</span>
        <select
          value={status}
          onChange={(event) => setStatus(event.target.value)}
        >
          <option value="">All statuses</option>
          {["PENDING", "PROCESSING", "SUCCESSFUL", "FAILED", "REFUNDED"].map(
            (item) => (
              <option key={item}>{item}</option>
            ),
          )}
        </select>
      </section>
      {query.isLoading ? (
        <State text="Loading payments..." />
      ) : query.isError ? (
        <State text="Unable to load payment history." error />
      ) : (
        <>
          <section className="payment-table">
            <table>
              <thead>
                <tr>
                  <th>Payment number</th>
                  <th>Policy number</th>
                  <th>Amount</th>
                  <th>Method</th>
                  <th>Payment date</th>
                  <th>Status</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {list.map((payment) => (
                  <tr key={payment.id}>
                    <td>
                      <b>{payment.paymentNumber}</b>
                    </td>
                    <td>{payment.policyNumber}</td>
                    <td>{money(payment.amount)}</td>
                    <td>{payment.paymentMethod.replaceAll("_", " ")}</td>
                    <td>{date(payment.paymentDate)}</td>
                    <td>
                      <Status value={getStatus(payment)} />
                    </td>
                    <td>
                      <button
                        className="table-action"
                        onClick={() => navigate(`/payments/${payment.id}`)}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
          <Pagination
            page={page}
            totalPages={query.data?.totalPages ?? 1}
            onChange={setPage}
          />
        </>
      )}
    </PaymentFrame>
  );
}

export function PaymentDetailsPage() {
  const { id = "" } = useParams();
  const { session } = useAuth();
  const payment = useQuery({
    queryKey: ["payment", id],
    queryFn: () => paymentApi.detail(id),
  });
  if (payment.isLoading) return <State text="Loading payment details..." />;
  if (!payment.data || payment.isError)
    return <State text="Unable to load payment details." error />;
  const item = payment.data;
  return (
    <PaymentFrame title={`Payment ${item.paymentNumber}`}>
      <Link className="payment-back" to="/payments/history">
        <ChevronLeft size={17} />
        Payment history
      </Link>
      <section className="payment-detail-grid">
        <InfoCard title="Transaction details">
          <Info label="Amount" value={money(item.amount)} />
          <Info label="Status" value={<Status value={getStatus(item)} />} />
          <Info
            label="Method"
            value={item.paymentMethod.replaceAll("_", " ")}
          />
          <Info label="Paid on" value={date(item.paymentDate)} />
        </InfoCard>
        <InfoCard title="Policy information">
          <Info label="Policy" value={item.policyNumber} />
          <Info label="Invoice" value={item.invoiceNumber} />
          <Info label="Transaction" value={item.transactionReference} />
        </InfoCard>
        <InfoCard title="Receipt information">
          <Info label="Receipt number" value={item.receiptNumber} />
          {session?.role !== "CLAIMS_OFFICER" && (
            <Link to={`/payments/${item.id}/receipt`}>View receipt</Link>
          )}
        </InfoCard>
      </section>
    </PaymentFrame>
  );
}

export function MakePaymentPage() {
  const navigate = useNavigate();
  const [policyNumber, setPolicyNumber] = useState("");
  const [amount, setAmount] = useState("");
  const [method, setMethod] = useState<PaymentMethod>("CREDIT_CARD");
  const policies = useQuery({
    queryKey: ["policies", "payment"],
    queryFn: paymentApi.policies,
  });
  const submit = useMutation({
    mutationFn: () =>
      paymentApi.pay({
        policyNumber,
        amount: Number(amount),
        paymentMethod: method,
      }),
    onSuccess: (payment) => navigate(`/payments/${payment.id}/receipt`),
  });
  const eligible =
    policies.data?.filter((policy) =>
      ["ACTIVE", "RENEWED"].includes(
        policy.policyStatus ?? policy.status ?? "",
      ),
    ) ?? [];
  return (
    <PaymentFrame title="Make payment">
      <section className="payment-form-layout">
        <form
          className="payment-form"
          onSubmit={(event) => {
            event.preventDefault();
            submit.mutate();
          }}
        >
          <label>
            Policy
            <select
              value={policyNumber}
              onChange={(event) => {
                setPolicyNumber(event.target.value);
                const policy = eligible.find(
                  (item) => item.policyNumber === event.target.value,
                );
                if (policy) setAmount(String(policy.renewalPremium));
              }}
              required
            >
              <option value="">Select active policy</option>
              {eligible.map((policy) => (
                <option key={policy.policyNumber} value={policy.policyNumber}>
                  {policy.policyNumber} · {policy.productName}
                </option>
              ))}
            </select>
          </label>
          <label>
            Payment amount
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
              required
            />
          </label>
          <label>
            Payment method
            <select
              value={method}
              onChange={(event) =>
                setMethod(event.target.value as PaymentMethod)
              }
            >
              {methods.map((item) => (
                <option key={item} value={item}>
                  {item.replaceAll("_", " ")}
                </option>
              ))}
            </select>
          </label>
          <button className="payment-primary" disabled={submit.isPending}>
            {submit.isPending ? "Processing..." : "Confirm payment"}
          </button>
          {submit.isError && (
            <span className="form-error">{submit.error.message}</span>
          )}
        </form>
        <article className="payment-panel">
          <p>PAYMENT SUMMARY</p>
          <h2>Secure policy payment</h2>
          <span>
            Payments can be made only for active or renewed policies you own.
            The backend records the selected payment method and transaction
            reference.
          </span>
        </article>
      </section>
    </PaymentFrame>
  );
}

export function ReceiptPage() {
  const { id = "" } = useParams();
  const receipt = useQuery({
    queryKey: ["payment", id, "receipt"],
    queryFn: () => paymentApi.receipt(id),
  });
  if (receipt.isLoading) return <State text="Loading receipt..." />;
  if (!receipt.data || receipt.isError)
    return <State text="Unable to load this receipt." error />;
  const item = receipt.data;
  return (
    <PaymentFrame title="Payment receipt">
      <article className="receipt-card">
        <p>PAYMENT RECEIPT</p>
        <h2>{item.receiptNumber}</h2>
        <Info label="Payment number" value={item.paymentNumber} />
        <Info label="Policy number" value={item.policyNumber} />
        <Info label="Customer" value={item.customerUsername} />
        <Info label="Amount" value={money(item.amount)} />
        <Info label="Payment date" value={date(item.paymentDate)} />
        <Info label="Transaction reference" value={item.transactionReference} />
        <button className="payment-primary" onClick={() => window.print()}>
          Print or save receipt
        </button>
      </article>
    </PaymentFrame>
  );
}
function PaymentFrame({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  const { session } = useAuth();
  return (
    <main className="payment-workspace">
      <header>
        <Link to={session ? defaultRoute(session.role) : "/login"}>
          <ChevronLeft size={17} />
          Workspace
        </Link>
        <span>
          <ShieldCheck size={18} />
          Payment Management
        </span>
      </header>
      <section className="payment-title">
        <p>PAYMENT MANAGEMENT</p>
        <h1>{title}</h1>
      </section>
      {children}
    </main>
  );
}
function Metric({
  label,
  value,
  tone,
}: {
  label: string;
  value: string | number;
  tone: string;
}) {
  return (
    <article className="metric-card">
      <div className={`metric-icon ${tone}`}>
        <CreditCard size={19} />
      </div>
      <p>{label}</p>
      <strong>{value}</strong>
      <span>Current payment data</span>
    </article>
  );
}
function InfoCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <article className="payment-panel">
      <p>{title.toUpperCase()}</p>
      <h2>{title}</h2>
      {children}
    </article>
  );
}
function Info({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="payment-info">
      <span>{label}</span>
      <b>{value}</b>
    </div>
  );
}
function Status({ value }: { value: PaymentStatus }) {
  return (
    <span className={`payment-status ${value.toLowerCase()}`}>
      {value.replaceAll("_", " ")}
    </span>
  );
}
function State({ text, error }: { text: string; error?: boolean }) {
  return <div className={`payment-state${error ? " error" : ""}`}>{text}</div>;
}
function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}) {
  return (
    <div className="payment-pagination">
      <button disabled={page === 0} onClick={() => onChange(page - 1)}>
        Previous
      </button>
      <span>
        Page {page + 1} of {totalPages}
      </span>
      <button
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
}
