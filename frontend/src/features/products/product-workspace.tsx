import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Check,
  ChevronLeft,
  CircleDollarSign,
  Edit3,
  Eye,
  Plus,
  Search,
  ShieldCheck,
  ToggleLeft,
  ToggleRight,
  Trash2,
  X,
} from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/auth-core";
import { apiClient, type ApiResponse } from "../../api/client";
import "./product-workspace.css";

type Category = "HEALTH" | "MOTOR" | "LIFE" | "TRAVEL" | "HOME" | "OTHER";
type Status = "ACTIVE" | "INACTIVE";
type Product = {
  id: number;
  productCode: string;
  productName: string;
  productCategory: Category;
  description: string;
  coverageAmount: number;
  premiumAmount: number;
  policyTenureMonths: number;
  status: Status;
};
type ProductRequest = Omit<Product, "id" | "status">;
type PurchaseRequest = {
  productId: number;
  nomineeName: string;
  nomineeRelation: string;
};
const categories: Category[] = [
  "HEALTH",
  "MOTOR",
  "LIFE",
  "TRAVEL",
  "HOME",
  "OTHER",
];
const money = (value: number) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(value);
const title = (value: string) => value.charAt(0) + value.slice(1).toLowerCase();
async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) {
  const response = await request;
  if (!response.data.success) throw new Error(response.data.message);
  return response.data.data;
}
const productsApi = {
  list: (keyword = "", category = "", status = "") =>
    unwrap(
      apiClient.get<ApiResponse<Product[]>>("/api/products", {
        params: {
          keyword: keyword || undefined,
          category: category || undefined,
          status: status || undefined,
        },
      }),
    ),
  detail: (id: number) =>
    unwrap(apiClient.get<ApiResponse<Product>>(`/api/products/${id}`)),
  compare: (ids: number[]) =>
    unwrap(
      apiClient.get<ApiResponse<Product[]>>("/api/products/compare", {
        params: { ids },
      }),
    ),
  recommend: () =>
    unwrap(
      apiClient.get<ApiResponse<Product[]>>("/api/products/recommendations"),
    ),
  create: (body: ProductRequest) =>
    unwrap(apiClient.post<ApiResponse<Product>>("/api/products", body)),
  update: (id: number, body: ProductRequest) =>
    unwrap(apiClient.put<ApiResponse<Product>>(`/api/products/${id}`, body)),
  remove: (id: number) =>
    unwrap(apiClient.delete<ApiResponse<void>>(`/api/products/${id}`)),
  toggle: (id: number, active: boolean) =>
    unwrap(
      apiClient.patch<ApiResponse<Product>>(
        `/api/products/${id}/${active ? "deactivate" : "activate"}`,
      ),
    ),
  purchase: (body: PurchaseRequest) =>
    unwrap(
      apiClient.post<ApiResponse<unknown>>("/api/policies/purchase", body),
    ),
};

export function ProductWorkspace() {
  const { session } = useAuth();
  const navigate = useNavigate();
  const client = useQueryClient();
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState("");
  const [selected, setSelected] = useState<number[]>([]);
  const [editing, setEditing] = useState<Product | "new" | null>(null);
  const [buying, setBuying] = useState<Product | null>(null);
  const role = session?.role;
  const admin = role === "ADMIN";
  const customer = role === "CUSTOMER";
  const agent = role === "AGENT";
  const products = useQuery({
    queryKey: ["products", keyword, category, admin],
    queryFn: () => productsApi.list(keyword, category, admin ? "" : "ACTIVE"),
  });
  const recommendations = useQuery({
    queryKey: ["products", "recommendations"],
    queryFn: productsApi.recommend,
    enabled: customer || agent,
  });
  const compare = useQuery({
    queryKey: ["products", "compare", selected],
    queryFn: () => productsApi.compare(selected),
    enabled: selected.length >= 2,
  });
  const refresh = () => client.invalidateQueries({ queryKey: ["products"] });
  const remove = useMutation({
    mutationFn: productsApi.remove,
    onSuccess: refresh,
  });
  const toggle = useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) =>
      productsApi.toggle(id, active),
    onSuccess: refresh,
  });
  const choose = (id: number) =>
    setSelected((current) =>
      current.includes(id)
        ? current.filter((item) => item !== id)
        : current.length === 4
          ? current
          : [...current, id],
    );
  return (
    <main className="product-workspace">
      <header className="product-header">
        <Link
          to={
            role === "CUSTOMER"
              ? "/customer/dashboard"
              : role === "AGENT"
                ? "/agent/dashboard"
                : role === "ADMIN"
                  ? "/admin/dashboard"
                  : "/claims/dashboard"
          }
        >
          <ChevronLeft size={18} />
          Workspace
        </Link>
        <span>
          <ShieldCheck size={18} /> Products
        </span>
      </header>
      <section className="product-intro">
        <div>
          <p>PRODUCT MANAGEMENT</p>
          <h1>
            {admin
              ? "Manage product catalog"
              : agent
                ? "Find the right coverage"
                : "Explore insurance products"}
          </h1>
          <span>
            {role === "CLAIMS_OFFICER"
              ? "Read-only coverage information for claims processing."
              : "Search, review, and compare available coverage options."}
          </span>
        </div>
        {admin && (
          <button className="product-primary" onClick={() => setEditing("new")}>
            <Plus size={17} />
            Create product
          </button>
        )}
      </section>
      {(customer || agent) && recommendations.data?.length ? (
        <section className="recommendations">
          <p>RECOMMENDED PRODUCTS</p>
          <div>
            {recommendations.data.slice(0, 3).map((product) => (
              <button key={product.id} onClick={() => choose(product.id)}>
                {product.productName}
                <span>{money(product.premiumAmount)}</span>
              </button>
            ))}
          </div>
        </section>
      ) : null}
      <section className="product-filters">
        <Search size={18} />
        <input
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="Search by name, code, or description"
        />
        <select
          value={category}
          onChange={(event) => setCategory(event.target.value)}
        >
          <option value="">All categories</option>
          {categories.map((item) => (
            <option key={item}>{item}</option>
          ))}
        </select>
      </section>
      {products.isLoading ? (
        <div className="product-state">Loading products...</div>
      ) : products.isError ? (
        <div className="product-state error">
          Unable to load products for this account.
        </div>
      ) : (
        <section className="product-grid">
          {products.data?.map((product) => (
            <article className="product-card" key={product.id}>
              <div className="product-card-top">
                <span className="category-chip">
                  {title(product.productCategory)}
                </span>
                <label>
                  <input
                    type="checkbox"
                    checked={selected.includes(product.id)}
                    onChange={() => choose(product.id)}
                  />{" "}
                  Compare
                </label>
              </div>
              <h2>{product.productName}</h2>
              <small>{product.productCode}</small>
              <p>{product.description}</p>
              <div className="product-values">
                <span>
                  Coverage <b>{money(product.coverageAmount)}</b>
                </span>
                <span>
                  Premium <b>{money(product.premiumAmount)}</b>
                </span>
                <span>
                  Term <b>{product.policyTenureMonths} months</b>
                </span>
              </div>
              <div className="product-actions">
                <button
                  title="View product details"
                  onClick={() => setEditing(product)}
                >
                  <Eye size={16} />
                  View
                </button>
                {customer && (
                  <button
                    className="action-primary"
                    onClick={() => setBuying(product)}
                  >
                    <CircleDollarSign size={16} />
                    Purchase
                  </button>
                )}
                {agent && (
                  <button
                    className="action-primary"
                    onClick={() => choose(product.id)}
                  >
                    <Check size={16} />
                    Recommend
                  </button>
                )}
                {admin && (
                  <>
                    <button
                      title="Edit product"
                      onClick={() => setEditing(product)}
                    >
                      <Edit3 size={16} />
                    </button>
                    <button
                      title={
                        product.status === "ACTIVE"
                          ? "Deactivate product"
                          : "Activate product"
                      }
                      onClick={() =>
                        toggle.mutate({
                          id: product.id,
                          active: product.status === "ACTIVE",
                        })
                      }
                    >
                      {product.status === "ACTIVE" ? (
                        <ToggleRight size={17} />
                      ) : (
                        <ToggleLeft size={17} />
                      )}
                    </button>
                    <button
                      title="Delete product"
                      className="danger"
                      onClick={() => {
                        if (window.confirm(`Delete ${product.productName}?`))
                          remove.mutate(product.id);
                      }}
                    >
                      <Trash2 size={16} />
                    </button>
                  </>
                )}
              </div>
              <span
                className={`product-status ${product.status.toLowerCase()}`}
              >
                {product.status}
              </span>
            </article>
          ))}
        </section>
      )}
      {selected.length >= 2 && (
        <section className="compare-panel">
          <div>
            <p>PRODUCT COMPARISON</p>
            <h2>{selected.length} products selected</h2>
          </div>
          <button onClick={() => setSelected([])}>Clear</button>
          <div className="compare-list">
            {compare.isLoading
              ? "Loading comparison..."
              : compare.data?.map((product) => (
                  <article key={product.id}>
                    <b>{product.productName}</b>
                    <span>{money(product.coverageAmount)} coverage</span>
                    <span>{money(product.premiumAmount)} premium</span>
                    <span>{product.policyTenureMonths} months</span>
                  </article>
                ))}
          </div>
        </section>
      )}
      {editing && (
        <ProductDialog
          product={editing}
          editable={admin}
          onClose={() => setEditing(null)}
          onSaved={refresh}
        />
      )}
      {buying && (
        <PurchaseDialog
          product={buying}
          onClose={() => setBuying(null)}
          onDone={() => navigate("/customer/dashboard")}
        />
      )}
    </main>
  );
}

function ProductDialog({
  product,
  editable,
  onClose,
  onSaved,
}: {
  product: Product | "new";
  editable: boolean;
  onClose: () => void;
  onSaved: () => void;
}) {
  const isNew = product === "new";
  const existingProduct = isNew ? null : product;
  const detail = useQuery({
    queryKey: ["product", existingProduct?.id],
    queryFn: () => productsApi.detail(existingProduct!.id),
    enabled: Boolean(existingProduct),
  });
  const resolvedProduct = detail.data ?? existingProduct;
  const [form, setForm] = useState<ProductRequest>(
    isNew
      ? {
          productCode: "",
          productName: "",
          productCategory: "HEALTH",
          description: "",
          coverageAmount: 0,
          premiumAmount: 0,
          policyTenureMonths: 12,
        }
      : product,
  );
  const mutation = useMutation({
    mutationFn: () =>
      isNew
        ? productsApi.create(form)
        : productsApi.update(existingProduct!.id, form),
    onSuccess: () => {
      onSaved();
      onClose();
    },
  });
  return (
    <div className="product-modal">
      <section>
        <button className="close" onClick={onClose}>
          <X size={18} />
        </button>
        <p>{isNew ? "CREATE PRODUCT" : "PRODUCT DETAILS"}</p>
        <h2>{isNew ? "New insurance product" : resolvedProduct?.productName}</h2>
        {editable ? (
          <form
            onSubmit={(event) => {
              event.preventDefault();
              mutation.mutate();
            }}
          >
            <input
              placeholder="Product code"
              value={form.productCode}
              onChange={(event) =>
                setForm({ ...form, productCode: event.target.value })
              }
            />
            <input
              placeholder="Product name"
              value={form.productName}
              onChange={(event) =>
                setForm({ ...form, productName: event.target.value })
              }
            />
            <select
              value={form.productCategory}
              onChange={(event) =>
                setForm({
                  ...form,
                  productCategory: event.target.value as Category,
                })
              }
            >
              {categories.map((item) => (
                <option key={item}>{item}</option>
              ))}
            </select>
            <textarea
              placeholder="Description"
              value={form.description}
              onChange={(event) =>
                setForm({ ...form, description: event.target.value })
              }
            />
            <input
              type="number"
              placeholder="Coverage amount"
              value={form.coverageAmount || ""}
              onChange={(event) =>
                setForm({ ...form, coverageAmount: Number(event.target.value) })
              }
            />
            <input
              type="number"
              placeholder="Premium amount"
              value={form.premiumAmount || ""}
              onChange={(event) =>
                setForm({ ...form, premiumAmount: Number(event.target.value) })
              }
            />
            <input
              type="number"
              placeholder="Policy tenure in months"
              value={form.policyTenureMonths || ""}
              onChange={(event) =>
                setForm({
                  ...form,
                  policyTenureMonths: Number(event.target.value),
                })
              }
            />
            <button className="product-primary" disabled={mutation.isPending}>
              {mutation.isPending
                ? "Saving..."
                : isNew
                  ? "Create product"
                  : "Save changes"}
            </button>
            {mutation.isError && (
              <small className="error">{mutation.error.message}</small>
            )}
          </form>
        ) : (
          <div className="detail-read">
            {detail.isLoading ? <p>Loading product details...</p> : <><p>{resolvedProduct!.description}</p><b>Coverage: {money(resolvedProduct!.coverageAmount)}</b><b>Premium: {money(resolvedProduct!.premiumAmount)}</b><b>Term: {resolvedProduct!.policyTenureMonths} months</b></>}
          </div>
        )}
      </section>
    </div>
  );
}
function PurchaseDialog({
  product,
  onClose,
  onDone,
}: {
  product: Product;
  onClose: () => void;
  onDone: () => void;
}) {
  const [nomineeName, setNomineeName] = useState("");
  const [nomineeRelation, setNomineeRelation] = useState("");
  const mutation = useMutation({
    mutationFn: () =>
      productsApi.purchase({
        productId: product.id,
        nomineeName,
        nomineeRelation,
      }),
    onSuccess: onDone,
  });
  return (
    <div className="product-modal">
      <section>
        <button className="close" onClick={onClose}>
          <X size={18} />
        </button>
        <p>PURCHASE PRODUCT</p>
        <h2>{product.productName}</h2>
        <form
          onSubmit={(event) => {
            event.preventDefault();
            mutation.mutate();
          }}
        >
          <input
            placeholder="Nominee name"
            value={nomineeName}
            onChange={(event) => setNomineeName(event.target.value)}
            required
          />
          <input
            placeholder="Relationship to nominee"
            value={nomineeRelation}
            onChange={(event) => setNomineeRelation(event.target.value)}
            required
          />
          <button className="product-primary" disabled={mutation.isPending}>
            Confirm purchase
          </button>
          {mutation.isError && (
            <small className="error">{mutation.error.message}</small>
          )}
        </form>
      </section>
    </div>
  );
}
