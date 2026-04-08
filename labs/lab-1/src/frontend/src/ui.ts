import {
  ApiError,
  Book,
  BookStatus,
  createBook,
  deleteBook,
  listBooks,
  updateBook,
} from './api';
import {
  currentUsername,
  hasScope,
  keycloak,
  login,
  logout,
} from './keycloak';

const BOOK_STATUSES: BookStatus[] = ['AVAILABLE', 'BORROWED', 'RESERVED', 'MAINTENANCE'];

export function renderApp(root: HTMLElement): void {
  root.innerHTML = `
    <div class="min-h-screen">
      <header class="bg-white border-b border-slate-200 shadow-sm">
        <div class="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
          <div>
            <h1 class="text-xl font-semibold">Bookshelf Admin View</h1>
            <p class="text-sm text-slate-500">Create books by ISBN — title, author and cover are fetched from OpenLibrary.</p>
          </div>
          <div id="auth-area" class="flex items-center gap-3"></div>
        </div>
      </header>

      <main class="max-w-6xl mx-auto px-6 py-8 space-y-8">
        <section id="toast-area" class="space-y-2"></section>

        <section class="bg-white rounded-lg border border-slate-200 shadow-sm">
          <div class="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
            <h2 class="font-medium">Books</h2>
            <button id="refresh-btn" class="text-sm text-indigo-600 hover:underline">Refresh</button>
          </div>
          <div id="books-table" class="p-6 text-sm text-slate-500">Loading&hellip;</div>
        </section>

        <section id="create-section" class="bg-white rounded-lg border border-slate-200 shadow-sm">
          <div class="px-6 py-4 border-b border-slate-200">
            <h2 class="font-medium">Add a new book</h2>
            <p class="text-xs text-slate-500">Requires <code>books:write</code>. Title, author and cover are enriched from OpenLibrary on creation.</p>
          </div>
          <form id="create-form" class="p-6 grid grid-cols-1 sm:grid-cols-3 gap-4">
            <label class="flex flex-col text-sm">
              ISBN (e.g. 978-0321356680)
              <input name="isbn" required pattern="^\\d{3}-\\d{10}$" class="mt-1 border border-slate-300 rounded px-3 py-2" />
            </label>
            <label class="flex flex-col text-sm">
              Internal name / shelf label
              <input name="internalName" required placeholder="SHELF-A-1" class="mt-1 border border-slate-300 rounded px-3 py-2" />
            </label>
            <label class="flex flex-col text-sm">
              Availability date
              <input name="availabilityDate" required type="date" class="mt-1 border border-slate-300 rounded px-3 py-2" />
            </label>
            <div class="sm:col-span-3">
              <button id="create-btn" type="submit" class="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 disabled:bg-slate-300 disabled:cursor-not-allowed" disabled>
                Create &amp; enrich from OpenLibrary
              </button>
            </div>
          </form>
        </section>
      </main>
    </div>
  `;

  renderAuthArea();
  wireCreateForm();
  document.getElementById('refresh-btn')!.addEventListener('click', () => refreshBooks());
  refreshBooks();
}

function renderAuthArea(): void {
  const area = document.getElementById('auth-area')!;
  if (keycloak.authenticated) {
    const username = currentUsername() ?? 'user';
    const scopes = [
      hasScope('books:read') ? 'read' : null,
      hasScope('books:write') ? 'write' : null,
    ].filter(Boolean).join(', ') || 'none';
    area.innerHTML = `
      <span class="text-sm text-slate-600">Signed in as <strong>${username}</strong> <span class="text-xs text-slate-400">(${scopes})</span></span>
      <button id="logout-btn" class="text-sm bg-slate-200 px-3 py-1.5 rounded hover:bg-slate-300">Log out</button>
    `;
    document.getElementById('logout-btn')!.addEventListener('click', () => logout());
  } else {
    area.innerHTML = `
      <button id="login-btn" class="text-sm bg-indigo-600 text-white px-3 py-1.5 rounded hover:bg-indigo-700">Log in with Keycloak</button>
    `;
    document.getElementById('login-btn')!.addEventListener('click', () => login());
  }

  const createButton = document.getElementById('create-btn') as HTMLButtonElement | null;
  if (createButton) {
    createButton.disabled = !hasScope('books:write');
  }
}

async function refreshBooks(): Promise<void> {
  const container = document.getElementById('books-table')!;
  container.innerHTML = 'Loading&hellip;';
  try {
    const books = await listBooks();
    if (books.length === 0) {
      container.innerHTML = '<p class="text-sm text-slate-500">No books yet.</p>';
      return;
    }
    container.innerHTML = `
      <table class="w-full text-sm text-left">
        <thead class="text-xs uppercase text-slate-500 border-b border-slate-200">
          <tr>
            <th class="py-2 pr-4">Cover</th>
            <th class="py-2 pr-4">ISBN</th>
            <th class="py-2 pr-4">Internal</th>
            <th class="py-2 pr-4">Title</th>
            <th class="py-2 pr-4">Author</th>
            <th class="py-2 pr-4">Available</th>
            <th class="py-2 pr-4">Status</th>
            <th class="py-2"></th>
          </tr>
        </thead>
        <tbody>
          ${books.map(bookRow).join('')}
        </tbody>
      </table>
    `;
    books.forEach(wireRowActions);
  } catch (error) {
    container.innerHTML = `<p class="text-sm text-red-600">Failed to load books: ${(error as Error).message}</p>`;
  }
}

function bookRow(book: Book): string {
  const canWrite = hasScope('books:write');
  const statusOptions = BOOK_STATUSES
    .map(status => `<option value="${status}" ${status === book.status ? 'selected' : ''}>${status}</option>`)
    .join('');
  const cover = book.thumbnailUrl
    ? `<img src="${escapeAttr(book.thumbnailUrl)}" alt="cover" class="h-14 w-auto rounded shadow-sm" />`
    : '<div class="h-14 w-10 bg-slate-100 rounded text-[10px] text-slate-400 flex items-center justify-center">no cover</div>';
  return `
    <tr class="border-b border-slate-100 align-top" data-book-id="${book.id}">
      <td class="py-2 pr-4">${cover}</td>
      <td class="py-2 pr-4 font-mono text-xs">${book.isbn}</td>
      <td class="py-2 pr-4"><input data-field="internalName" class="w-full border border-slate-200 rounded px-2 py-1" value="${escapeAttr(book.internalName)}" ${canWrite ? '' : 'disabled'} /></td>
      <td class="py-2 pr-4">${escapeHtml(book.title)}</td>
      <td class="py-2 pr-4">${escapeHtml(book.author)}</td>
      <td class="py-2 pr-4"><input data-field="availabilityDate" type="date" class="border border-slate-200 rounded px-2 py-1" value="${book.availabilityDate}" ${canWrite ? '' : 'disabled'} /></td>
      <td class="py-2 pr-4">
        <select data-field="status" class="border border-slate-200 rounded px-2 py-1" ${canWrite ? '' : 'disabled'}>${statusOptions}</select>
      </td>
      <td class="py-2 flex gap-2 justify-end">
        <button data-action="save" class="text-indigo-600 hover:underline disabled:text-slate-300" ${canWrite ? '' : 'disabled'}>Save</button>
        <button data-action="delete" class="text-red-600 hover:underline disabled:text-slate-300" ${canWrite ? '' : 'disabled'}>Delete</button>
      </td>
    </tr>
  `;
}

function wireRowActions(book: Book): void {
  const row = document.querySelector<HTMLTableRowElement>(`tr[data-book-id="${book.id}"]`);
  if (!row) {
    return;
  }
  row.querySelector<HTMLButtonElement>('button[data-action="save"]')?.addEventListener('click', async () => {
    const internalName = row.querySelector<HTMLInputElement>('input[data-field="internalName"]')!.value;
    const availabilityDate = row.querySelector<HTMLInputElement>('input[data-field="availabilityDate"]')!.value;
    const status = row.querySelector<HTMLSelectElement>('select[data-field="status"]')!.value as BookStatus;
    try {
      await updateBook(book.id, { internalName, availabilityDate, status });
      showToast('success', `Updated "${book.title}".`);
      refreshBooks();
    } catch (error) {
      showToast('error', `Update failed: ${describeError(error)}`);
    }
  });
  row.querySelector<HTMLButtonElement>('button[data-action="delete"]')?.addEventListener('click', async () => {
    if (!confirm(`Delete "${book.title}"?`)) {
      return;
    }
    try {
      await deleteBook(book.id);
      showToast('success', `Deleted "${book.title}".`);
      refreshBooks();
    } catch (error) {
      showToast('error', `Delete failed: ${describeError(error)}`);
    }
  });
}

function wireCreateForm(): void {
  const form = document.getElementById('create-form') as HTMLFormElement;
  form.addEventListener('submit', async event => {
    event.preventDefault();
    const formData = new FormData(form);
    try {
      await createBook({
        isbn: String(formData.get('isbn')),
        internalName: String(formData.get('internalName')),
        availabilityDate: String(formData.get('availabilityDate')),
      });
      showToast('success', 'Book created and enriched from OpenLibrary.');
      form.reset();
      refreshBooks();
    } catch (error) {
      showToast('error', `Create failed: ${describeError(error)}`);
    }
  });
}

function describeError(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 422) {
      return `${error.detail ?? 'OpenLibrary has no record for this ISBN.'}`;
    }
    if (error.status === 401 || error.status === 403) {
      return `${error.status} — log in as bob or admin for write access.`;
    }
    return error.detail ? `${error.status}: ${error.detail}` : error.message;
  }
  return (error as Error).message;
}

function showToast(kind: 'success' | 'error', message: string): void {
  const area = document.getElementById('toast-area')!;
  const toast = document.createElement('div');
  const color = kind === 'success' ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-800';
  toast.className = `rounded px-4 py-2 text-sm ${color}`;
  toast.textContent = message;
  area.appendChild(toast);
  setTimeout(() => toast.remove(), 5000);
}

function escapeAttr(value: string): string {
  return value.replace(/"/g, '&quot;');
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}
