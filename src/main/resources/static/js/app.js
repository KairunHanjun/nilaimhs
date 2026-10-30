"use strict";
(() => {
  const role = document.body.dataset.role;
  if (!role) return;
  const $ = (id) => document.getElementById(id);
  const username = document.body.dataset.username;
  let students = [],
    users = [],
    editing = null;
  const fmt = (value) =>
    value === null || value === undefined
      ? "—"
      : Number(value).toLocaleString("id-ID", { maximumFractionDigits: 2 });
  const text = (tag, value, className) => {
    const node = document.createElement(tag);
    node.textContent = value ?? "—";
    if (className) node.className = className;
    return node;
  };
  function notify(message, error = false) {
    const box = $("feedback");
    box.textContent = message;
    box.className = error ? "error" : "success";
    box.hidden = false;
    box.scrollIntoView({ block: "nearest" });
  }
  async function api(url, options = {}) {
    const headers = { ...options.headers };
    if (options.body) headers["Content-Type"] = "application/json";
    if (options.method && options.method !== "GET")
      headers[document.querySelector('meta[name="_csrf_header"]').content] =
        document.querySelector('meta[name="_csrf"]').content;
    const response = await fetch(url, { ...options, headers });
    if (response.redirected) {
      window.location.assign(response.url);
      throw new Error("Sesi berakhir. Silakan masuk kembali.");
    }
    const contentType = response.headers.get("content-type") || "";
    const result = contentType.includes("application/json")
      ? await response.json()
      : {};
    if (!response.ok)
      throw new Error(
        result.message ||
          (response.status === 403
            ? "Akses ditolak. Muat ulang halaman dan coba lagi."
            : "Permintaan gagal diproses."),
      );
    return result;
  }
  function badge(report) {
    return text(
      "span",
      report.statusKelulusan || "Belum lengkap",
      "badge " +
        (report.statusKelulusan === "Lulus"
          ? "pass"
          : report.statusKelulusan
            ? "fail"
            : ""),
    );
  }
  function identity(row, idKey = "nis") {
    const cell = document.createElement("td");
    cell.append(
      text("strong", row.nama || "Administrator"),
      text("small", row[idKey]),
    );
    return cell;
  }
  function td(value) {
    return text("td", value);
  }
  function empty(body, columns) {
    const row = document.createElement("tr"),
      cell = td("Tidak ada data yang sesuai.");
    cell.colSpan = columns;
    row.append(cell);
    body.append(row);
  }
  function gradeRows(body, rows, short = false) {
    body.replaceChildren();
    for (const row of rows) {
      const tr = document.createElement("tr");
      tr.append(identity(row), td(row.kelas));
      if (!short)
        tr.append(
          td(fmt(row.nilaiTugas)),
          td(fmt(row.nilaiUTS)),
          td(fmt(row.nilaiUAS)),
        );
      tr.append(td(fmt(row.nilaiAkhir)));
      const status = document.createElement("td");
      status.append(badge(row));
      tr.append(status);
      body.append(tr);
    }
    if (!rows.length) empty(body, short ? 4 : 7);
  }
  function metric(label, value, detail) {
    const card = text("div", null, "metric");
    card.textContent = "";
    card.append(
      text("span", label, "metric-label"),
      text("strong", value),
      text("small", detail),
    );
    return card;
  }
  function renderOverview(stats) {
    const data = role === "SISWA" ? [students[0]] : students;
    const done = students.filter((s) => s.nilaiAkhir !== null).length;
    $("metrics").replaceChildren(
      ...(role === "SISWA"
        ? [
            metric(
              "Nilai akhir",
              fmt(students[0]?.nilaiAkhir),
              "Bobot 30% + 30% + 40%",
            ),
            metric(
              "Status evaluasi",
              students[0]?.statusKelulusan || "Menunggu",
              "Batas kelulusan 70",
            ),
            metric(
              "Komponen terisi",
              [
                students[0]?.nilaiTugas,
                students[0]?.nilaiUTS,
                students[0]?.nilaiUAS,
              ].filter((v) => v != null).length + " / 3",
              "Tugas, UTS, dan UAS",
            ),
          ]
        : [
            metric(
              "Total siswa",
              stats?.totalSiswa ?? students.length,
              "Siswa terdaftar",
            ),
            metric(
              role === "ADMIN" ? "Total guru" : "Nilai lengkap",
              stats?.totalGuru ?? done,
              role === "ADMIN"
                ? "Guru aktif dalam sistem"
                : "Seluruh komponen sudah terisi",
            ),
            metric(
              role === "ADMIN" ? "Nilai terproses" : "Belum lengkap",
              stats?.nilaiTerproses ?? students.length - done,
              role === "ADMIN"
                ? "Evaluasi akademik lengkap"
                : "Menunggu komponen nilai",
            ),
          ]),
    );
    if (role === "SISWA")
      $("greeting").textContent = "Halo, " + students[0].nama;
    else if (role === "GURU") $("greeting").textContent = "Ringkasan penilaian";
    $("overview-table-title").textContent =
      role === "SISWA" ? "Hasil evaluasi saya" : "Ringkasan hasil belajar";
    const row = document.createElement("tr");
    for (const label of ["Siswa / NIS", "Kelas", "Nilai akhir", "Status"])
      row.append(text("th", label));
    $("overview-head").replaceChildren(row);
    gradeRows($("overview-body"), data.slice(0, 6), true);
  }
  function renderGrades() {
    const query = $("grade-search")?.value.toLowerCase() || "";
    gradeRows(
      $("grades-body"),
      students.filter((s) =>
        (s.nama + " " + s.nis).toLowerCase().includes(query),
      ),
    );
  }
  function renderUsers() {
    const query = $("user-search").value.toLowerCase();
    const body = $("users-body");
    body.replaceChildren();
    for (const row of users.filter((u) =>
      (u.nama + " " + u.nomorInduk).toLowerCase().includes(query),
    )) {
      const tr = document.createElement("tr");
      tr.append(identity(row, "nomorInduk"));
      const roleCell = document.createElement("td");
      roleCell.append(text("span", row.role, "badge"));
      tr.append(roleCell, td(row.detail || "—"));
      const actions = document.createElement("td");
      const edit = text("button", "Edit", "row-action");
      edit.type = "button";
      edit.addEventListener("click", () =>
        openUser(row.nomorInduk).catch((e) => notify(e.message, true)),
      );
      actions.append(edit);
      if (row.nomorInduk !== username) {
        const remove = text("button", "Hapus", "row-action danger");
        remove.type = "button";
        remove.addEventListener("click", () => removeUser(row.nomorInduk));
        actions.append(remove);
      }
      tr.append(actions);
      body.append(tr);
    }
    if (!body.children.length) empty(body, 4);
  }
  function renderReport() {
    const row = students[0];
    $("report-name").textContent = row.nama;
    $("report-identity").textContent = "NIS " + row.nis + " · " + row.kelas;
    $("report-task").textContent = fmt(row.nilaiTugas);
    $("report-uts").textContent = fmt(row.nilaiUTS);
    $("report-uas").textContent = fmt(row.nilaiUAS);
    $("report-final").textContent = fmt(row.nilaiAkhir);
    $("report-status").replaceChildren(badge(row));
  }
  function fillGrades() {
    const row = students.find((s) => s.nis === $("student-select").value);
    if (!row) return;
    $("selected-student").textContent = row.nama + " · " + row.kelas;
    for (const [key, value] of Object.entries({
      nilaiTugas: row.nilaiTugas,
      nilaiUts: row.nilaiUTS,
      nilaiUas: row.nilaiUAS,
    }))
      $("grade-form").elements.namedItem(key).value = value ?? "";
  }
  async function refresh() {
    students =
      role === "SISWA"
        ? [await api("/api/siswa/laporanku")]
        : await api("/api/protected/siswa");
    let stats;
    if (role === "ADMIN") {
      [users, stats] = await Promise.all([
        api("/api/admin/users"),
        api("/api/admin/stats"),
      ]);
      renderUsers();
    }
    renderOverview(stats);
    if (role === "SISWA") renderReport();
    else renderGrades();
    if (role === "GURU") {
      const select = $("student-select"),
        selected = select.value;
      select.replaceChildren(
        ...students.map((s) => {
          const option = text("option", s.nis + " · " + s.nama);
          option.value = s.nis;
          return option;
        }),
      );
      if (students.some((s) => s.nis === selected)) select.value = selected;
      fillGrades();
    }
  }
  function switchSection(id) {
    document
      .querySelectorAll(".view")
      .forEach((view) => (view.hidden = view.id !== id));
    document
      .querySelectorAll("[data-section]")
      .forEach((button) =>
        button.classList.toggle("active", button.dataset.section === id),
      );
    $("feedback").hidden = true;
  }
  document
    .querySelectorAll("[data-section]")
    .forEach((button) =>
      button.addEventListener("click", () =>
        switchSection(button.dataset.section),
      ),
    );
  function userFields() {
    const form = $("user-form"),
      selected = form.elements.namedItem("role").value;
    document
      .querySelectorAll(".student-field")
      .forEach((n) => (n.hidden = selected !== "SISWA"));
    document
      .querySelectorAll(".teacher-field")
      .forEach((n) => (n.hidden = selected !== "GURU"));
    document
      .querySelectorAll(".person-field")
      .forEach((n) => (n.hidden = selected === "ADMIN"));
    form.elements.namedItem("nama").required = selected !== "ADMIN";
    form.elements.namedItem("kelas").required = selected === "SISWA";
    form.elements.namedItem("mataPelajaran").required = selected === "GURU";
  }
  async function openUser(id = null) {
    editing = id;
    const form = $("user-form");
    form.reset();
    form.elements.namedItem("nomorInduk").readOnly = !!id;
    form.elements.namedItem("role").disabled = !!id;
    form.elements.namedItem("password").required = !id;
    $("user-form-title").textContent = id ? "Edit pengguna" : "Tambah pengguna";
    if (id) {
      const row = await api("/api/admin/users/" + encodeURIComponent(id));
      for (const [key, value] of Object.entries(row)) {
        const field = form.elements.namedItem(key);
        if (field) field.value = value ?? "";
      }
    }
    userFields();
    $("user-dialog").showModal();
  }
  async function removeUser(id) {
    if (!window.confirm("Hapus akun " + id + " beserta datanya?")) return;
    try {
      await api("/api/admin/users/" + encodeURIComponent(id), {
        method: "DELETE",
      });
      await refresh();
      notify("Pengguna berhasil dihapus.");
    } catch (e) {
      notify(e.message, true);
    }
  }
  function payload(form) {
    const result = Object.fromEntries(new FormData(form));
    for (const key of ["nilaiTugas", "nilaiUts", "nilaiUas"])
      if (key in result)
        result[key] = result[key] === "" ? null : Number(result[key]);
    return result;
  }
  function submit(form, action) {
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const button = form.querySelector('[type="submit"]');
      button.disabled = true;
      try {
        await action();
      } catch (e) {
        notify(e.message, true);
        if ($("user-dialog")?.open) {
          let error = $("dialog-error");
          if (!error) {
            error = text("p", "", "notice error");
            error.id = "dialog-error";
            form.prepend(error);
          }
          error.textContent = e.message;
        }
      } finally {
        button.disabled = false;
      }
    });
  }
  if (role === "ADMIN") {
    $("new-user").addEventListener("click", () =>
      openUser().catch((e) => notify(e.message, true)),
    );
    $("close-user").addEventListener("click", () => $("user-dialog").close());
    $("user-dialog").addEventListener("close", () =>
      $("dialog-error")?.remove(),
    );
    $("user-form")
      .elements.namedItem("role")
      .addEventListener("change", userFields);
    $("user-search").addEventListener("input", renderUsers);
    submit($("user-form"), async () => {
      const data = payload($("user-form"));
      data.role = $("user-form").elements.namedItem("role").value;
      const result = await api(
        "/api/admin/users" + (editing ? "/" + encodeURIComponent(editing) : ""),
        { method: editing ? "PUT" : "POST", body: JSON.stringify(data) },
      );
      $("user-dialog").close();
      await refresh();
      notify(result.message);
    });
  }
  if (role !== "SISWA")
    $("grade-search").addEventListener("input", renderGrades);
  if (role === "GURU") {
    $("student-select").addEventListener("change", fillGrades);
    submit($("grade-form"), async () => {
      const data = payload($("grade-form"));
      const result = await api(
        "/api/guru/siswa/" + encodeURIComponent($("student-select").value),
        { method: "PUT", body: JSON.stringify(data) },
      );
      await refresh();
      notify(result.message);
    });
  }
  if (role !== "ADMIN") {
    const form = $("profile-form");
    $("profile-id").value = username;
    if (role === "GURU")
      $("profile-detail-label").firstChild.textContent = "Mata pelajaran";
    submit(form, async () => {
      const data = payload(form);
      data[role === "GURU" ? "mataPelajaran" : "kelas"] =
        $("profile-detail").value;
      const result = await api("/api/protected/profil", {
        method: "PUT",
        body: JSON.stringify(data),
      });
      form.elements.namedItem("password").value = "";
      await refresh();
      notify(result.message);
    });
  }
  if (role === "SISWA")
    $("print-report").addEventListener("click", () => window.print());
  refresh()
    .then(() => {
      if (role !== "ADMIN") {
        const form = $("profile-form");
        form.elements.namedItem("nama").value =
          role === "SISWA"
            ? students[0].nama
            : document.body.dataset.name || "";
        $("profile-detail").value =
          role === "SISWA"
            ? students[0].kelas
            : document.body.dataset.detail || "";
      }
    })
    .catch((e) => notify(e.message, true));
})();
