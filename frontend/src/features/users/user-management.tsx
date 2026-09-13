import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ChevronLeft,
  Eye,
  Plus,
  Search,
  ShieldCheck,
  Trash2,
} from "lucide-react";
import { useState, type ReactNode } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { z } from "zod";
import { apiClient, type ApiResponse } from "../../api/client";
import "./user-management.css";

type Role = "CUSTOMER" | "AGENT" | "CLAIMS_OFFICER" | "ADMIN";
type Gender = "MALE" | "FEMALE" | "OTHER" | "PREFER_NO_TO_SAY";
type User = {
  id: number;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  phoneNumber: string;
  email: string;
  username: string;
  role: Role;
  enabled?: boolean;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
};
type Page<T> = { content: T[]; totalPages: number; totalElements: number };
type CreateUser = Omit<User, "id" | "enabled"> & { password: string };
const roles: Role[] = ["CUSTOMER", "AGENT", "CLAIMS_OFFICER", "ADMIN"];
const genders: Gender[] = ["MALE", "FEMALE", "OTHER", "PREFER_NO_TO_SAY"];
const roleLabel = (role: string) =>
  role.replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase());
const userStatus = (enabled: boolean | undefined) =>
  enabled === true
    ? { label: "Enabled", className: "user-status enabled" }
    : enabled === false
      ? { label: "Disabled", className: "user-status disabled" }
      : { label: "Unknown", className: "user-status unknown" };
const schema = z.object({
  firstName: z.string().min(1).max(50),
  lastName: z.string().min(1).max(50),
  dateOfBirth: z.string().min(1, "Date of birth is required"),
  gender: z.enum(["MALE", "FEMALE", "OTHER", "PREFER_NO_TO_SAY"]),
  phoneNumber: z.string().regex(/^\d{10}$/, "Use exactly 10 digits"),
  email: z.string().email().max(100),
  username: z
    .string()
    .regex(
      /^[A-Za-z0-9._]{4,20}$/,
      "Use 4-20 letters, numbers, dots, or underscores",
    ),
  password: z
    .string()
    .regex(
      /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@#$%^&+=!]).{8,20}$/,
      "Password requires upper, lower, number, special character",
    ),
  role: z.enum(["CUSTOMER", "AGENT", "CLAIMS_OFFICER", "ADMIN"]),
  addressLine1: z.string().max(150).optional(),
  addressLine2: z.string().max(150).optional(),
  city: z.string().max(50).optional(),
  state: z.string().max(50).optional(),
  country: z.string().max(50).optional(),
  postalCode: z.string().max(15).optional(),
});
async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) {
  const response = await request;
  if (!response.data.success) throw new Error(response.data.message);
  return response.data.data;
}
const usersApi = {
  list: (params: Record<string, string | number | boolean | undefined>) =>
    unwrap(
      apiClient.get<ApiResponse<Page<User>>>("/api/customers", { params }),
    ),
  detail: (id: string) =>
    unwrap(apiClient.get<ApiResponse<User>>(`/api/customers/${id}`)),
  create: (body: CreateUser) =>
    unwrap(apiClient.post<ApiResponse<User>>("/api/customers", body)),
  remove: (id: number) =>
    unwrap(apiClient.delete<ApiResponse<void>>(`/api/customers/${id}`)),
};

export function UserListPage() {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [role, setRole] = useState("");
  const [enabled, setEnabled] = useState("");
  const [page, setPage] = useState(0);
  const users = useQuery({
    queryKey: ["users", query, role, enabled, page],
    queryFn: () =>
      usersApi.list({
        query: query || undefined,
        role: role || undefined,
        enabled: enabled === "" ? undefined : enabled === "true",
        page,
        size: 20,
        sortBy: "createdAt",
        direction: "DESC",
      }),
  });
  return (
    <UsersFrame title="User management">
      <section className="user-toolbar">
        <Search size={18} />
        <input
          value={query}
          onChange={(event) => {
            setQuery(event.target.value);
            setPage(0);
          }}
          placeholder="Search name, email, or username"
        />
        <select
          value={role}
          onChange={(event) => {
            setRole(event.target.value);
            setPage(0);
          }}
        >
          <option value="">All roles</option>
          {roles.map((item) => (
            <option key={item}>{item}</option>
          ))}
        </select>
        <select
          value={enabled}
          onChange={(event) => {
            setEnabled(event.target.value);
            setPage(0);
          }}
        >
          <option value="">All statuses</option>
          <option value="true">Enabled</option>
          <option value="false">Disabled</option>
        </select>
        <Link className="user-primary" to="/admin/users/new">
          <Plus size={16} />
          Create user
        </Link>
      </section>
      {users.isLoading ? (
        <State text="Loading users..." />
      ) : users.isError ? (
        <State text="Unable to load users." error />
      ) : (
        <>
          <section className="user-table">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Username</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Created date</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {users.data?.content.map((user) => {
                  const status = userStatus(user.enabled);
                  return (
                  <tr key={user.id}>
                    <td>
                      <b>
                        {user.firstName} {user.lastName}
                      </b>
                    </td>
                    <td>{user.email}</td>
                    <td>{user.username}</td>
                    <td>
                      <span className="role-badge">{roleLabel(user.role)}</span>
                    </td>
                    <td>
                      <span className={status.className}>{status.label}</span>
                    </td>
                    <td>
                      <span className="not-available">Not exposed</span>
                    </td>
                    <td>
                      <button
                        className="user-icon"
                        onClick={() => navigate(`/admin/users/${user.id}`)}
                        title="View user"
                      >
                        <Eye size={16} />
                      </button>
                    </td>
                  </tr>
                  );
                })}
              </tbody>
            </table>
          </section>
          <Pagination
            page={page}
            totalPages={users.data?.totalPages ?? 1}
            onChange={setPage}
          />
        </>
      )}
    </UsersFrame>
  );
}

export function UserDetailsPage() {
  const { id = "" } = useParams();
  const navigate = useNavigate();
  const cache = useQueryClient();
  const user = useQuery({
    queryKey: ["user", id],
    queryFn: () => usersApi.detail(id),
  });
  const remove = useMutation({
    mutationFn: () => usersApi.remove(Number(id)),
    onSuccess: () => {
      cache.invalidateQueries({ queryKey: ["users"] });
      navigate("/admin/users");
    },
  });
  if (user.isLoading) return <State text="Loading user details..." />;
  if (!user.data || user.isError)
    return <State text="Unable to load user details." error />;
  const data = user.data;
  return (
    <UsersFrame title={`${data.firstName} ${data.lastName}`}>
      <Link className="user-back" to="/admin/users">
        <ChevronLeft size={17} />
        All users
      </Link>
      <section className="user-detail-grid">
        <Info title="Account">
          <Row label="Username" value={data.username} />
          <Row label="Role" value={roleLabel(data.role)} />
          <Row label="Status" value={userStatus(data.enabled).label} />
        </Info>
        <Info title="Contact">
          <Row label="Email" value={data.email} />
          <Row label="Phone" value={data.phoneNumber} />
          <Row label="Date of birth" value={data.dateOfBirth} />
        </Info>
        <Info title="Address">
          <Row
            label="Address"
            value={
              [data.addressLine1, data.addressLine2]
                .filter(Boolean)
                .join(", ") || "Not provided"
            }
          />
          <Row label="City" value={data.city || "Not provided"} />
          <Row
            label="Region"
            value={
              [data.state, data.country, data.postalCode]
                .filter(Boolean)
                .join(", ") || "Not provided"
            }
          />
        </Info>
      </section>
      <section className="user-actions">
        <button
          className="danger-action"
          onClick={() => {
            if (window.confirm(`Delete ${data.firstName} ${data.lastName}?`))
              remove.mutate();
          }}
          disabled={remove.isPending}
        >
          <Trash2 size={16} />
          Delete user
        </button>
        <span>
          Editing, account activation, deactivation, and password reset are
          unavailable because no backend endpoints support those operations.
        </span>
        {remove.isError && <p className="form-error">{remove.error.message}</p>}
      </section>
    </UsersFrame>
  );
}

export function CreateUserPage() {
  const navigate = useNavigate();
  const cache = useQueryClient();
  const form = useForm<CreateUser>({
    resolver: zodResolver(schema),
    defaultValues: {
      firstName: "",
      lastName: "",
      dateOfBirth: "",
      gender: "OTHER",
      phoneNumber: "",
      email: "",
      username: "",
      password: "",
      role: "CUSTOMER",
    },
  });
  const create = useMutation({
    mutationFn: usersApi.create,
    onSuccess: (user) => {
      cache.invalidateQueries({ queryKey: ["users"] });
      navigate(`/admin/users/${user.id}`);
    },
  });
  return (
    <UsersFrame title="Create user">
      <form
        className="user-form"
        onSubmit={form.handleSubmit((data) => create.mutate(data))}
      >
        <FormSection title="Personal information">
          <Field
            label="First name"
            error={form.formState.errors.firstName?.message}
          >
            <input {...form.register("firstName")} />
          </Field>
          <Field
            label="Last name"
            error={form.formState.errors.lastName?.message}
          >
            <input {...form.register("lastName")} />
          </Field>
          <Field
            label="Date of birth"
            error={form.formState.errors.dateOfBirth?.message}
          >
            <input type="date" {...form.register("dateOfBirth")} />
          </Field>
          <Field label="Gender">
            <select {...form.register("gender")}>
              {genders.map((item) => (
                <option key={item}>{item}</option>
              ))}
            </select>
          </Field>
        </FormSection>
        <FormSection title="Account">
          <Field label="Email" error={form.formState.errors.email?.message}>
            <input type="email" {...form.register("email")} />
          </Field>
          <Field
            label="Phone number"
            error={form.formState.errors.phoneNumber?.message}
          >
            <input {...form.register("phoneNumber")} />
          </Field>
          <Field
            label="Username"
            error={form.formState.errors.username?.message}
          >
            <input {...form.register("username")} />
          </Field>
          <Field
            label="Temporary password"
            error={form.formState.errors.password?.message}
          >
            <input type="password" {...form.register("password")} />
          </Field>
          <Field label="Role">
            <select {...form.register("role")}>
              {roles.map((item) => (
                <option key={item}>{item}</option>
              ))}
            </select>
          </Field>
        </FormSection>
        <FormSection title="Address">
          <Field label="Address line 1">
            <input {...form.register("addressLine1")} />
          </Field>
          <Field label="Address line 2">
            <input {...form.register("addressLine2")} />
          </Field>
          <Field label="City">
            <input {...form.register("city")} />
          </Field>
          <Field label="State">
            <input {...form.register("state")} />
          </Field>
          <Field label="Country">
            <input {...form.register("country")} />
          </Field>
          <Field label="Postal code">
            <input {...form.register("postalCode")} />
          </Field>
        </FormSection>
        <div className="user-form-actions">
          <Link className="user-outline" to="/admin/users">
            Cancel
          </Link>
          <button className="user-primary" disabled={create.isPending}>
            {create.isPending ? "Creating..." : "Create user"}
          </button>
        </div>
        {create.isError && <p className="form-error">{create.error.message}</p>}
      </form>
    </UsersFrame>
  );
}
function UsersFrame({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <main className="users-page">
      <header>
        <Link to="/admin/dashboard">
          <ChevronLeft size={17} />
          Administrator workspace
        </Link>
        <span>
          <ShieldCheck size={18} />
          User Management
        </span>
      </header>
      <section className="users-title">
        <p>ADMINISTRATION</p>
        <h1>{title}</h1>
      </section>
      {children}
    </main>
  );
}
function Info({ title, children }: { title: string; children: ReactNode }) {
  return (
    <article className="user-panel">
      <h2>{title}</h2>
      {children}
    </article>
  );
}
function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="user-row">
      <span>{label}</span>
      <b>{value}</b>
    </div>
  );
}
function FormSection({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <section>
      <h2>{title}</h2>
      <div className="user-form-grid">{children}</div>
    </section>
  );
}
function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <label>
      <span>{label}</span>
      {children}
      {error && <small>{error}</small>}
    </label>
  );
}
function State({ text, error }: { text: string; error?: boolean }) {
  return <main className={`user-state${error ? " error" : ""}`}>{text}</main>;
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
    <div className="user-pagination">
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
