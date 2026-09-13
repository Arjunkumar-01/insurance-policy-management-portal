import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  CheckCircle2,
  ChevronLeft,
  ClipboardCheck,
  FilePlus2,
  FileText,
  Search,
  ShieldCheck,
  XCircle,
} from "lucide-react";
import { useState, type ReactNode } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { defaultRoute, useAuth } from "../../auth/auth-core";
import { apiClient, type ApiResponse } from "../../api/client";
import "./claims-workspace.css";

type ClaimStatus =
  | "SUBMITTED"
  | "UNDER_REVIEW"
  | "APPROVED"
  | "REJECTED"
  | "SETTLED";
type Claim = {
  id: number;
  claimNumber: string;
  policyNumber: string;
  submittedBy: string;
  incidentDate: string;
  claimAmount: number;
  description: string;
  supportingDocuments?: string;
  claimStatus?: ClaimStatus;
  status?: ClaimStatus;
  submittedDate: string;
  reviewedDate?: string;
  settledDate?: string;
};
type StatusInfo = {
  claimStatus: ClaimStatus;
  submittedDate: string;
  reviewedDate?: string;
  settledDate?: string;
};
type Analytics = {
  totalClaims: number;
  submittedClaims: number;
  underReviewClaims: number;
  approvedClaims: number;
  rejectedClaims: number;
  settledClaims: number;
  approvalRatio: number;
  settlementRatio: number;
};
type Page<T> = { content: T[]; totalPages: number };
type Policy = {
  policyNumber: string;
  productName: string;
  policyStatus?: string;
  status?: string;
};

const statuses: ClaimStatus[] = [
  "SUBMITTED",
  "UNDER_REVIEW",
  "APPROVED",
  "REJECTED",
  "SETTLED",
];
const getStatus = (claim: Claim) =>
  claim.claimStatus ?? claim.status ?? "SUBMITTED";
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

const claimsApi = {
  mine: (page: number) =>
    unwrap(
      apiClient.get<ApiResponse<Page<Claim>>>("/api/claims/my", {
        params: { page, size: 20 },
      }),
    ),
  all: (page: number) =>
    unwrap(
      apiClient.get<ApiResponse<Page<Claim>>>("/api/claims", {
        params: { page, size: 20 },
      }),
    ),
  queue: (page: number) =>
    unwrap(
      apiClient.get<ApiResponse<Page<Claim>>>("/api/claims/queue", {
        params: { page, size: 20 },
      }),
    ),
  detail: (id: string) =>
    unwrap(apiClient.get<ApiResponse<Claim>>(`/api/claims/${id}`)),
  status: (id: string) =>
    unwrap(apiClient.get<ApiResponse<StatusInfo>>(`/api/claims/${id}/status`)),
  analytics: () =>
    unwrap(apiClient.get<ApiResponse<Analytics>>("/api/claims/analytics")),
  submit: (body: {
    policyNumber: string;
    incidentDate: string;
    claimAmount: number;
    description: string;
    supportingDocuments?: string;
  }) => unwrap(apiClient.post<ApiResponse<Claim>>("/api/claims", body)),
  process: (
    id: number,
    action: "review" | "approve" | "reject" | "settle",
    decisionReason?: string,
  ) =>
    unwrap(
      apiClient.patch<ApiResponse<Claim>>(
        `/api/claims/${id}/${action}`,
        decisionReason ? { decisionReason } : undefined,
      ),
    ),
  updateStatus: (
    id: number,
    claimStatus: ClaimStatus,
    decisionReason?: string,
  ) =>
    unwrap(
      apiClient.patch<ApiResponse<Claim>>(`/api/claims/${id}/status`, {
        claimStatus,
        decisionReason,
      }),
    ),
  policies: () =>
    unwrap(apiClient.get<ApiResponse<Policy[]>>("/api/policies/me")),
};

export function ClaimsDashboardPage() {
  const { session } = useAuth();
  const role = session?.role;
  const loader = role === "CUSTOMER" ? claimsApi.mine : claimsApi.all;
  const claims = useQuery({
    queryKey: ["claims", role, "dashboard"],
    queryFn: () => loader(0),
  });
  const analytics = useQuery({
    queryKey: ["claims", "analytics"],
    queryFn: claimsApi.analytics,
    enabled: role === "ADMIN" || role === "CLAIMS_OFFICER",
  });
  const list = claims.data?.content ?? [];
  const counts = analytics.data ?? buildCounts(list);

  return (
    <ClaimsFrame
      title={
        role === "CUSTOMER"
          ? "My claims"
          : role === "ADMIN"
            ? "Claims analytics"
            : "Claims operations"
      }
    >
      <section className="claim-actions">
        {role === "CUSTOMER" && (
          <Link className="claim-primary" to="/claims/new">
            <FilePlus2 size={17} />
            Submit claim
          </Link>
        )}
        <Link className="claim-outline" to="/claims/list">
          View claims
        </Link>
        {(role === "CLAIMS_OFFICER" || role === "ADMIN") && (
          <Link className="claim-primary" to="/claims/queue">
            <ClipboardCheck size={17} />
            Claims queue
          </Link>
        )}
      </section>
      {claims.isError ? (
        <State text="Unable to load claim data for this account." error />
      ) : (
        <>
          <section className="claims-metrics">
            <Metric
              label="Total claims"
              value={counts.totalClaims}
              icon={ClipboardCheck}
              tone="blue"
            />
            <Metric
              label="Submitted"
              value={counts.submittedClaims}
              icon={FileText}
              tone="amber"
            />
            <Metric
              label="Under review"
              value={counts.underReviewClaims}
              icon={Search}
              tone="purple"
            />
            <Metric
              label="Approved"
              value={counts.approvedClaims}
              icon={CheckCircle2}
              tone="green"
            />
            <Metric
              label="Rejected"
              value={counts.rejectedClaims}
              icon={XCircle}
              tone="slate"
            />
            {role !== "CUSTOMER" && (
              <Metric
                label="Settled"
                value={counts.settledClaims}
                icon={CheckCircle2}
                tone="teal"
              />
            )}
          </section>
          <section className="claims-grid">
            <article className="claim-panel">
              <p>CLAIMS STATUS OVERVIEW</p>
              <h2>Workflow summary</h2>
              <ClaimTimeline
                status={list[0] ? getStatus(list[0]) : "SUBMITTED"}
              />
              <span className="panel-note">
                {role === "ADMIN" || role === "CLAIMS_OFFICER"
                  ? `Approval ratio ${counts.approvalRatio}% · Settlement ratio ${counts.settlementRatio}%`
                  : "Track each claim through its active lifecycle."}
              </span>
            </article>
            <article className="claim-panel">
              <p>RECENT CLAIM ACTIVITY</p>
              <h2>Latest claims</h2>
              {claims.isLoading ? (
                <State text="Loading claims..." />
              ) : (
                list.slice(0, 4).map((claim) => (
                  <Link
                    className="recent-claim"
                    key={claim.id}
                    to={`/claims/${claim.id}`}
                  >
                    <div>
                      <b>{claim.claimNumber}</b>
                      <span>
                        {claim.policyNumber} · {date(claim.submittedDate)}
                      </span>
                    </div>
                    <Status value={getStatus(claim)} />
                  </Link>
                ))
              )}
            </article>
          </section>
        </>
      )}
    </ClaimsFrame>
  );
}

export function ClaimsListPage({ queue = false }: { queue?: boolean }) {
  const { session } = useAuth();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [filter, setFilter] = useState("");
  const loader = queue
    ? claimsApi.queue
    : session?.role === "CUSTOMER"
      ? claimsApi.mine
      : claimsApi.all;
  const claims = useQuery({
    queryKey: ["claims", queue ? "queue" : "list", session?.role, page],
    queryFn: () => loader(page),
  });
  const list =
    claims.data?.content.filter(
      (claim) => !filter || getStatus(claim) === filter,
    ) ?? [];
  return (
    <ClaimsFrame title={queue ? "Claims queue" : "Claims list"}>
      <section className="claims-toolbar">
        <Search size={18} />
        <span>Claims</span>
        <select
          value={filter}
          onChange={(event) => setFilter(event.target.value)}
        >
          <option value="">All statuses</option>
          {statuses.map((status) => (
            <option key={status}>{status}</option>
          ))}
        </select>
      </section>
      {claims.isLoading ? (
        <State text="Loading claims..." />
      ) : claims.isError ? (
        <State text="Unable to load claims." error />
      ) : (
        <>
          <section className="claims-table">
            <table>
              <thead>
                <tr>
                  <th>Claim number</th>
                  <th>Customer</th>
                  <th>Policy</th>
                  <th>Amount</th>
                  <th>Submitted</th>
                  <th>Status</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {list.map((claim) => (
                  <tr key={claim.id}>
                    <td>
                      <b>{claim.claimNumber}</b>
                    </td>
                    <td>{claim.submittedBy}</td>
                    <td>{claim.policyNumber}</td>
                    <td>{money(claim.claimAmount)}</td>
                    <td>{date(claim.submittedDate)}</td>
                    <td>
                      <Status value={getStatus(claim)} />
                    </td>
                    <td>
                      <button
                        className="table-action"
                        onClick={() => navigate(`/claims/${claim.id}`)}
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
            totalPages={claims.data?.totalPages ?? 1}
            onChange={setPage}
          />
        </>
      )}
    </ClaimsFrame>
  );
}

export function ClaimDetailsPage() {
  const { id = "" } = useParams();
  const { session } = useAuth();
  const cache = useQueryClient();
  const [reason, setReason] = useState("");
  const claim = useQuery({
    queryKey: ["claim", id],
    queryFn: () => claimsApi.detail(id),
  });
  const history = useQuery({
    queryKey: ["claim", id, "status"],
    queryFn: () => claimsApi.status(id),
  });
  const refresh = () => {
    cache.invalidateQueries({ queryKey: ["claim", id] });
    cache.invalidateQueries({ queryKey: ["claims"] });
  };
  const process = useMutation({
    mutationFn: (action: "review" | "approve" | "reject" | "settle") =>
      claimsApi.process(Number(id), action, reason || undefined),
    onSuccess: refresh,
  });
  const adminStatus = useMutation({
    mutationFn: (next: ClaimStatus) =>
      claimsApi.updateStatus(Number(id), next, reason || undefined),
    onSuccess: refresh,
  });
  if (claim.isLoading) return <State text="Loading claim details..." />;
  if (!claim.data || claim.isError)
    return <State text="Unable to load claim details." error />;
  const item = claim.data;
  const status = getStatus(item);
  const processor =
    session?.role === "CLAIMS_OFFICER" || session?.role === "ADMIN";
  return (
    <ClaimsFrame title={`Claim ${item.claimNumber}`}>
      <Link className="claim-back" to="/claims/list">
        <ChevronLeft size={17} />
        All claims
      </Link>
      <section className="claim-detail-grid">
        <InfoCard title="Claim information">
          <Info label="Status" value={<Status value={status} />} />
          <Info label="Claim amount" value={money(item.claimAmount)} />
          <Info label="Incident date" value={date(item.incidentDate)} />
          <Info label="Submitted" value={date(item.submittedDate)} />
        </InfoCard>
        <InfoCard title="Policy and customer">
          <Info label="Policy" value={item.policyNumber} />
          <Info label="Customer" value={item.submittedBy} />
        </InfoCard>
        <InfoCard title="Supporting details">
          <p>{item.description}</p>
          {item.supportingDocuments ? (
            <a href={item.supportingDocuments} target="_blank" rel="noreferrer">
              Open supporting document
            </a>
          ) : (
            <span className="unavailable">No document URL supplied</span>
          )}
        </InfoCard>
        <InfoCard title="Status timeline">
          <ClaimTimeline
            status={history.data?.claimStatus ?? status}
            submittedDate={history.data?.submittedDate}
            reviewedDate={history.data?.reviewedDate}
            settledDate={history.data?.settledDate}
          />
        </InfoCard>
      </section>
      {processor && (
        <section className="claim-panel process-panel">
          <p>CLAIM PROCESSING</p>
          <h2>Record a decision</h2>
          <textarea
            value={reason}
            onChange={(event) => setReason(event.target.value)}
            maxLength={500}
            placeholder="Decision reason (required for rejection)"
          />
          <div>
            {status === "SUBMITTED" && (
              <button
                className="claim-outline"
                onClick={() => process.mutate("review")}
              >
                Move to review
              </button>
            )}
            {["SUBMITTED", "UNDER_REVIEW"].includes(status) && (
              <button
                className="claim-primary"
                onClick={() => process.mutate("approve")}
              >
                Approve
              </button>
            )}
            {["SUBMITTED", "UNDER_REVIEW"].includes(status) && (
              <button
                className="danger-action"
                disabled={!reason.trim()}
                onClick={() => process.mutate("reject")}
              >
                Reject
              </button>
            )}
            {status === "APPROVED" && (
              <button
                className="claim-primary"
                onClick={() => process.mutate("settle")}
              >
                Mark settled
              </button>
            )}
          </div>
          {process.isError && (
            <span className="form-error">{process.error.message}</span>
          )}
        </section>
      )}
      {session?.role === "ADMIN" && (
        <section className="claim-panel admin-status">
          <p>ADMIN LIFECYCLE CONTROL</p>
          <select
            value={status}
            onChange={(event) =>
              adminStatus.mutate(event.target.value as ClaimStatus)
            }
          >
            {statuses.map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>
        </section>
      )}
    </ClaimsFrame>
  );
}

export function SubmitClaimPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const requestedPolicyId = searchParams.get("policyId") ?? "";
  const requestedPolicyNumber = searchParams.get("policyNumber") ?? "";
  const requestedProductName = searchParams.get("productName") ?? "";
  const [policyNumber, setPolicyNumber] = useState(requestedPolicyNumber);
  const [incidentDate, setIncidentDate] = useState("");
  const [claimAmount, setClaimAmount] = useState("");
  const [description, setDescription] = useState("");
  const [supportingDocuments, setSupportingDocuments] = useState("");
  const policies = useQuery({
    queryKey: ["policies", "claim-submit"],
    queryFn: claimsApi.policies,
  });
  const submit = useMutation({
    mutationFn: () =>
      claimsApi.submit({
        policyNumber,
        incidentDate,
        claimAmount: Number(claimAmount),
        description,
        supportingDocuments: supportingDocuments || undefined,
      }),
    onSuccess: (claim) => navigate(`/claims/${claim.id}`),
  });
  const eligible =
    policies.data?.filter((policy) =>
      ["ACTIVE", "RENEWED"].includes(
        policy.policyStatus ?? policy.status ?? "",
      ),
    ) ?? [];
  const selectedPolicy = eligible.find(
    (policy) => policy.policyNumber === policyNumber,
  );
  return (
    <ClaimsFrame title="Submit claim">
      <section className="submit-layout">
        <form
          className="claim-form"
          onSubmit={(event) => {
            event.preventDefault();
            submit.mutate();
          }}
        >
          <label>
            Eligible policy
            <select
              value={policyNumber}
              onChange={(event) => setPolicyNumber(event.target.value)}
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
          {selectedPolicy && (
            <div className="selected-claim-policy">
              <b>{selectedPolicy.productName}</b>
              <span>Policy {selectedPolicy.policyNumber}</span>
            </div>
          )}
          {!selectedPolicy && requestedPolicyNumber && (
            <div className="selected-claim-policy">
              <b>{requestedProductName || "Selected policy"}</b>
              <span>Policy {requestedPolicyNumber}{requestedPolicyId ? ` · ID ${requestedPolicyId}` : ""}</span>
            </div>
          )}
          <label>
            Incident date
            <input
              type="date"
              value={incidentDate}
              onChange={(event) => setIncidentDate(event.target.value)}
              required
            />
          </label>
          <label>
            Claim amount
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={claimAmount}
              onChange={(event) => setClaimAmount(event.target.value)}
              required
            />
          </label>
          <label>
            Description
            <textarea
              maxLength={2000}
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              required
            />
          </label>
          <label>
            Supporting document URL <small>Optional HTTP/HTTPS URL</small>
            <input
              type="url"
              value={supportingDocuments}
              onChange={(event) => setSupportingDocuments(event.target.value)}
            />
          </label>
          <button className="claim-primary" disabled={submit.isPending}>
            {submit.isPending ? "Submitting claim..." : "Submit claim"}
          </button>
          <Link className="claim-outline" to="/policies">
            Cancel
          </Link>
          {submit.isError && (
            <span className="form-error">{submit.error.message}</span>
          )}
        </form>
        <article className="claim-panel">
          <p>CLAIM ELIGIBILITY</p>
          <h2>Before you submit</h2>
          <span>
            Claims can be filed only against policies with active coverage. The
            backend verifies policy ownership and coverage dates.
          </span>
          <p className="unavailable">
            File uploads are not available. Provide an external document URL
            only.
          </p>
        </article>
      </section>
    </ClaimsFrame>
  );
}

export function ClaimTimeline({
  status,
  submittedDate,
  reviewedDate,
  settledDate,
}: {
  status: ClaimStatus;
  submittedDate?: string;
  reviewedDate?: string;
  settledDate?: string;
}) {
  const stages: ClaimStatus[] = [
    "SUBMITTED",
    "UNDER_REVIEW",
    "APPROVED",
    "SETTLED",
  ];
  const rejected = status === "REJECTED";
  const current = stages.indexOf(status);
  return (
    <div className="claim-timeline">
      {stages.map((stage, index) => (
        <div
          className={`timeline-stage ${!rejected && index <= current ? "complete" : ""} ${stage === status ? "current" : ""}`}
          key={stage}
        >
          <i />
          <span>{stage.replaceAll("_", " ")}</span>
          <small>
            {stage === "SUBMITTED"
              ? date(submittedDate)
              : stage === "SETTLED"
                ? date(settledDate)
                : date(reviewedDate)}
          </small>
        </div>
      ))}
      {rejected && (
        <div className="timeline-stage rejected">
          <i />
          <span>Rejected</span>
          <small>{date(reviewedDate)}</small>
        </div>
      )}
    </div>
  );
}

function buildCounts(claims: Claim[]): Analytics {
  const count = (status: ClaimStatus) =>
    claims.filter((claim) => getStatus(claim) === status).length;
  const approved = count("APPROVED");
  const rejected = count("REJECTED");
  return {
    totalClaims: claims.length,
    submittedClaims: count("SUBMITTED"),
    underReviewClaims: count("UNDER_REVIEW"),
    approvedClaims: approved,
    rejectedClaims: rejected,
    settledClaims: count("SETTLED"),
    approvalRatio:
      approved + rejected
        ? Number(((approved * 100) / (approved + rejected)).toFixed(2))
        : 0,
    settlementRatio: approved
      ? Number(((count("SETTLED") * 100) / approved).toFixed(2))
      : 0,
  };
}
function ClaimsFrame({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  const { session } = useAuth();
  return (
    <main className="claims-workspace">
      <header>
        <Link to={session ? defaultRoute(session.role) : "/login"}>
          <ChevronLeft size={17} />
          Workspace
        </Link>
        <span>
          <ShieldCheck size={18} />
          Claims Management
        </span>
      </header>
      <section className="claims-title">
        <p>CLAIMS MANAGEMENT</p>
        <h1>{title}</h1>
      </section>
      {children}
    </main>
  );
}
function Metric({
  label,
  value,
  icon: Icon,
  tone,
}: {
  label: string;
  value: number;
  icon: typeof ClipboardCheck;
  tone: string;
}) {
  return (
    <article className="metric-card">
      <div className={`metric-icon ${tone}`}>
        <Icon size={19} />
      </div>
      <p>{label}</p>
      <strong>{value}</strong>
      <span>Current claim data</span>
    </article>
  );
}
function Status({ value }: { value: ClaimStatus }) {
  return (
    <span className={`claim-status ${value.toLowerCase()}`}>
      {value.replaceAll("_", " ")}
    </span>
  );
}
function InfoCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <article className="claim-panel">
      <p>{title.toUpperCase()}</p>
      <h2>{title}</h2>
      {children}
    </article>
  );
}
function Info({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="claim-info">
      <span>{label}</span>
      <b>{value}</b>
    </div>
  );
}
function State({ text, error }: { text: string; error?: boolean }) {
  return <div className={`claims-state${error ? " error" : ""}`}>{text}</div>;
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
    <div className="claim-pagination">
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

type Worklist = "queue" | "approvals" | "settlements";

const worklistConfig: Record<
  Worklist,
  { title: string; eyebrow: string; statuses: ClaimStatus[] }
> = {
  queue: {
    title: "Claims queue",
    eyebrow: "SUBMITTED AND UNDER REVIEW",
    statuses: ["SUBMITTED", "UNDER_REVIEW"],
  },
  approvals: {
    title: "Approvals",
    eyebrow: "AWAITING APPROVAL DECISION",
    statuses: ["UNDER_REVIEW"],
  },
  settlements: {
    title: "Settlements",
    eyebrow: "APPROVED CLAIMS",
    statuses: ["APPROVED"],
  },
};

export function ClaimsWorklistPage({ worklist }: { worklist: Worklist }) {
  const config = worklistConfig[worklist];
  const cache = useQueryClient();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<Claim | null>(null);
  const [reason, setReason] = useState("");
  const claims = useQuery({
    queryKey: ["claims", worklist, page],
    queryFn: () =>
      worklist === "queue" ? claimsApi.queue(page) : claimsApi.all(page),
  });
  const update = useMutation({
    mutationFn: (action: "review" | "approve" | "reject" | "settle") =>
      claimsApi.process(selected!.id, action, reason || undefined),
    onSuccess: () => {
      cache.invalidateQueries({ queryKey: ["claims"] });
      setSelected(null);
      setReason("");
    },
  });
  const visible =
    claims.data?.content.filter((claim) =>
      config.statuses.includes(getStatus(claim)),
    ) ?? [];
  const actions = selected
    ? worklist === "queue" && getStatus(selected) === "SUBMITTED"
      ? ["review"]
      : worklist === "settlements"
        ? ["settle"]
        : ["approve", "reject"]
    : [];

  return (
    <ClaimsFrame title={config.title}>
      <section className="worklist-heading">
        <div>
          <p>{config.eyebrow}</p>
          <span>
            {worklist === "queue"
              ? "Submitted and in-review claims requiring attention."
              : worklist === "approvals"
                ? "Claims reviewed and ready for an approval decision."
                : "Approved claims awaiting settlement."}
          </span>
        </div>
        <Link className="claim-outline" to="/claims">
          All claims
        </Link>
      </section>
      {claims.isLoading ? (
        <State text="Loading claims..." />
      ) : claims.isError ? (
        <State text="Unable to load claims." error />
      ) : (
        <section className="claims-table">
          <table>
            <thead>
              <tr>
                <th>Claim number</th>
                <th>Customer</th>
                <th>Policy</th>
                <th>Amount</th>
                <th>Submitted</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {visible.map((claim) => (
                <tr key={claim.id}>
                  <td>
                    <b>{claim.claimNumber}</b>
                  </td>
                  <td>{claim.submittedBy}</td>
                  <td>{claim.policyNumber}</td>
                  <td>{money(claim.claimAmount)}</td>
                  <td>{date(claim.submittedDate)}</td>
                  <td>
                    <Status value={getStatus(claim)} />
                  </td>
                  <td>
                    <button
                      className="table-action"
                      onClick={() => setSelected(claim)}
                    >
                      {worklist === "settlements" ? "Settle" : "Process"}
                    </button>
                    <button
                      className="table-action"
                      onClick={() => navigate(`/claims/${claim.id}`)}
                    >
                      View
                    </button>
                  </td>
                </tr>
              ))}
              {!visible.length && (
                <tr>
                  <td colSpan={7}>
                    <State
                      text={`No claims are currently in this ${worklist} worklist.`}
                    />
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </section>
      )}
      <Pagination
        page={page}
        totalPages={claims.data?.totalPages ?? 1}
        onChange={setPage}
      />
      {selected && (
        <div className="claim-modal" role="dialog" aria-modal="true">
          <section>
            <button className="modal-close" onClick={() => setSelected(null)}>
              Close
            </button>
            <p>{config.eyebrow}</p>
            <h2>{selected.claimNumber}</h2>
            <span>
              {selected.submittedBy} · {selected.policyNumber}
            </span>
            {actions.includes("reject") && (
              <textarea
                value={reason}
                onChange={(event) => setReason(event.target.value)}
                maxLength={500}
                placeholder="Decision reason required to reject"
              />
            )}
            {actions.includes("review") && (
              <button
                className="claim-outline"
                onClick={() => update.mutate("review")}
              >
                Move to review
              </button>
            )}
            {actions.includes("approve") && (
              <button
                className="claim-primary"
                onClick={() => update.mutate("approve")}
              >
                Approve claim
              </button>
            )}
            {actions.includes("reject") && (
              <button
                className="danger-action"
                disabled={!reason.trim()}
                onClick={() => update.mutate("reject")}
              >
                Reject claim
              </button>
            )}
            {actions.includes("settle") && (
              <button
                className="claim-primary"
                onClick={() => update.mutate("settle")}
              >
                Mark settled
              </button>
            )}
            {update.isError && (
              <span className="form-error">{update.error.message}</span>
            )}
          </section>
        </div>
      )}
    </ClaimsFrame>
  );
}
