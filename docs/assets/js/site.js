/* =========================================================================
   CoreFx Docs — shared chrome + behaviours
   Injects the navbar, sidebar and footer (so the 18 pages don't duplicate
   them), wires the mobile menu and install tabs, adds copy buttons, and does
   lightweight syntax highlighting. Path prefixes are derived from
   <body data-depth>, so every link works on GitHub Pages and from the file
   system alike.
   ========================================================================= */
(function () {
  "use strict";

  /* ----------------------------------------------------------- Data model */
  var PACKAGES = [
    {
      slug: "navigation", name: "navigation", emoji: "🗺️", title: "Navigation & Flow", title_es: "Navegación",
      desc: "View loading, scene swapping, windows and shared state.",
      classes: [
        { slug: "flow-controller", name: "FlowController", tag: "Singleton", desc: "Loads & caches FXML views, swaps scenes, opens windows and modals." },
        { slug: "app-context", name: "AppContext", tag: "Singleton", desc: "Thread-safe, process-wide key-value store for shared state." },
        { slug: "stage-manager", name: "StageManager", tag: "Singleton", desc: "Named registry and lifecycle control for JavaFX windows." }
      ]
    },
    {
      slug: "ui", name: "ui", emoji: "🎨", title: "UI Utilities", title_es: "Utilidades UI",
      desc: "Theming, dialogs, tables, input formatting, images and binding.",
      classes: [
        { slug: "theme-manager", name: "ThemeManager", tag: "Singleton", desc: "Register CSS theme sets and live-switch them across scenes." },
        { slug: "alert-util", name: "AlertUtil", tag: "Singleton", desc: "Themed alert/confirmation dialogs with self-contained CSS." },
        { slug: "message", name: "Message", tag: "Static", desc: "Lightweight, theme-free alert and confirmation dialogs." },
        { slug: "table-utils", name: "TableUtils", tag: "Static", desc: "Type-safe TableView columns, items, selection and live search." },
        { slug: "format", name: "Format", tag: "Singleton", desc: "TextFormatter input filters plus date/decimal formatters." },
        { slug: "binding-utils", name: "BindingUtils", tag: "Static", desc: "Two-way binding between a ToggleGroup and an ObjectProperty." },
        { slug: "image-util", name: "ImageUtil", tag: "Static", desc: "Robust image loading, view shaping and pixel processing." }
      ]
    },
    {
      slug: "util", name: "util", emoji: "🛠️", title: "General Utilities", title_es: "Utilidades generales",
      desc: "Validation and a standardized response wrapper.",
      classes: [
        { slug: "validator", name: "Validator", tag: "Singleton", desc: "Null-safe predicates and throwing contract validators." },
        { slug: "answer", name: "Answer", tag: "Value", desc: "Response wrapper: state, messages and a keyed result payload." }
      ]
    },
    {
      slug: "persistence", name: "persistence", emoji: "💾", title: "Persistence", title_es: "Persistencia",
      desc: "Provider-agnostic persistence-context holder.",
      classes: [
        { slug: "entity-manager-helper", name: "EntityManagerHelper", tag: "Singleton", desc: "Lazily creates, caches and hands back a persistence manager." }
      ]
    }
  ];

  var GITHUB = "https://github.com/Dinamo541/CoreFx";
  var MAVEN = "https://central.sonatype.com/artifact/io.github.dinamo541/corefx";

  /* ----------------------------------------------------------- Paths */
  var body = document.body;
  var depth = parseInt(body.getAttribute("data-depth") || "0", 10);
  var active = body.getAttribute("data-active") || "";
  var P = depth === 0 ? "" : (depth === 1 ? "../" : "../../");

  function url(rel) { return P + rel; }
  function pkgUrl(s) { return url("packages/" + s + ".html"); }
  function clsUrl(p, c) { return url("classes/" + p + "/" + c + ".html"); }

  /* Bilingual snippet: emits both languages; CSS shows the active one. If no
     Spanish is supplied, the text is language-neutral and shown as-is. */
  function bi(en, es) {
    if (es == null) return en;
    return '<span lang="en">' + en + '</span><span lang="es">' + es + "</span>";
  }

  /* ----------------------------------------------------------- Navbar */
  function buildNavbar() {
    var host = document.getElementById("navbar");
    if (!host) return;
    host.className = "navbar";
    var links = PACKAGES.map(function (p) {
      return '<a href="' + pkgUrl(p.slug) + '">' + bi(p.title, p.title_es) + "</a>";
    }).join("");
    host.innerHTML =
      '<div class="nav-inner">' +
        '<button class="icon-btn nav-mobile-btn" data-toggle-sidebar aria-label="Open menu">☰</button>' +
        '<a class="nav-brand" href="' + url("index.html") + '"><span class="logo">Fx</span> CoreFx</a>' +
        '<nav class="nav-links">' + links + "</nav>" +
        '<span class="nav-spacer"></span>' +
        '<div class="nav-actions">' +
          '<div class="lang-switch" role="group" aria-label="Language / Idioma">' +
            '<button type="button" data-lang-set="en" title="English">EN</button>' +
            '<button type="button" data-lang-set="es" title="Español">ES</button>' +
          "</div>" +
          '<button class="icon-btn" data-theme-toggle>🌙</button>' +
          '<a class="icon-btn" href="' + GITHUB + '" target="_blank" rel="noopener" title="GitHub" aria-label="GitHub">' +
            '<svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0016 8c0-4.42-3.58-8-8-8z"/></svg>' +
          "</a>" +
        "</div>" +
      "</div>";
    if (window.CoreFxTheme) window.CoreFxTheme.sync();
  }

  /* ----------------------------------------------------------- Sidebar */
  function buildSidebar() {
    var host = document.getElementById("sidebar");
    if (!host) return;
    var html = '<a class="sb-home" href="' + url("index.html") + '">🏠 ' + bi("Home", "Inicio") + "</a>";
    PACKAGES.forEach(function (p) {
      var isActivePkg = (active === p.slug) || p.classes.some(function (c) { return c.slug === active; });
      var open = isActivePkg ? " open" : "";
      var links = p.classes.map(function (c) {
        var cls = c.slug === active ? ' class="active"' : "";
        return '<a' + cls + ' href="' + clsUrl(p.slug, c.slug) + '">' + c.name + "</a>";
      }).join("");
      var pkgActive = active === p.slug ? ' style="color:var(--accent)"' : "";
      html +=
        "<details class=\"sb-group\"" + open + ">" +
          "<summary><span class=\"emoji\">" + p.emoji + "</span>" +
            '<a href="' + pkgUrl(p.slug) + '"' + pkgActive + ' style="color:inherit">' + p.name + "</a>" +
            '<span class="chev">▶</span></summary>' +
          '<div class="sb-links">' + links + "</div>" +
        "</details>";
    });
    host.innerHTML = html;
  }

  /* ----------------------------------------------------------- Footer */
  function buildFooter() {
    var host = document.getElementById("footer");
    if (!host) return;
    host.className = "site-footer";
    host.innerHTML =
      '<div class="f-links">' +
        '<a href="' + url("index.html") + '">' + bi("Home", "Inicio") + "</a>" +
        '<a href="' + MAVEN + '" target="_blank" rel="noopener">Maven Central</a>' +
        '<a href="' + GITHUB + '" target="_blank" rel="noopener">GitHub</a>' +
        '<a href="' + GITHUB + '/blob/main/CHANGELOG.md" target="_blank" rel="noopener">' + bi("Changelog", "Cambios") + "</a>" +
        '<a href="' + GITHUB + '/blob/main/LICENSE" target="_blank" rel="noopener">' + bi("License", "Licencia") + "</a>" +
      "</div>" +
      "<div>" + bi(
        "CoreFx — the foundation layer for JavaFX applications. Released under the MIT License.",
        "CoreFx — la capa base para aplicaciones JavaFX. Publicado bajo la licencia MIT."
      ) + "</div>" +
      '<div style="margin-top:6px;color:var(--text-faint)">' + bi("Built with care by", "Hecho con dedicación por") + " " +
        '<a href="https://github.com/Dinamo541" target="_blank" rel="noopener">Dominique</a> & ' +
        '<a href="https://github.com/SemMora" target="_blank" rel="noopener">Sem</a>.</div>';
  }

  /* ----------------------------------------------------------- Mobile menu */
  function wireMobileMenu() {
    document.addEventListener("click", function (ev) {
      if (ev.target.closest("[data-toggle-sidebar]")) {
        body.classList.toggle("sidebar-open");
      } else if (ev.target.closest("[data-close-sidebar]")) {
        body.classList.remove("sidebar-open");
      } else if (ev.target.closest(".sb-links a")) {
        body.classList.remove("sidebar-open");
      }
    });
  }

  /* ----------------------------------------------------------- Install tabs */
  function wireTabs() {
    var groups = document.querySelectorAll(".tabs");
    groups.forEach(function (group) {
      var btns = group.querySelectorAll(".tab-btns button");
      var panels = group.querySelectorAll(".tab-panel");
      btns.forEach(function (btn, i) {
        btn.addEventListener("click", function () {
          btns.forEach(function (b) { b.classList.remove("active"); });
          panels.forEach(function (pp) { pp.classList.remove("active"); });
          btn.classList.add("active");
          if (panels[i]) panels[i].classList.add("active");
        });
      });
    });
  }

  /* ----------------------------------------------------------- Highlighting */
  function escapeHtml(s) {
    return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }

  var JAVA_KW = ("abstract assert boolean break byte case catch char class const continue default do double " +
    "else enum extends final finally float for goto if implements import instanceof int interface long native " +
    "new package private protected public return short static strictfp super switch synchronized this throw " +
    "throws transient try var void volatile while record sealed permits yield true false null").split(" ");
  var KW_RE = new RegExp("\\b(" + JAVA_KW.join("|") + ")\\b", "g");

  function highlightJava(code) {
    var esc = escapeHtml(code);
    // Order: comments, strings/chars, annotations, numbers, keywords.
    var re = /(\/\/[^\n]*|\/\*[\s\S]*?\*\/)|("(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*')|(@\w+)|\b(\d[\d_]*\.?\d*[fFdDlL]?)\b/g;
    esc = esc.replace(re, function (m, com, str, ann, num) {
      if (com !== undefined && com !== null && com !== "") return '<span class="tok-com">' + com + "</span>";
      if (str) return '<span class="tok-str">' + str + "</span>";
      if (ann) return '<span class="tok-ann">' + ann + "</span>";
      if (num) return '<span class="tok-num">' + num + "</span>";
      return m;
    });
    // Keywords (won't touch text already wrapped in spans because those are
    // inside tags; the \b boundaries and span syntax keep them safe enough
    // for our short, curated snippets).
    esc = esc.replace(/(<span[^>]*>[\s\S]*?<\/span>)|([A-Za-z_$][\w$]*)/g, function (m, span, word) {
      if (span) return span;
      if (JAVA_KW.indexOf(word) !== -1) return '<span class="tok-key">' + word + "</span>";
      return word;
    });
    return esc;
  }

  function highlightXml(code) {
    var esc = escapeHtml(code);
    esc = esc.replace(/(&lt;!--[\s\S]*?--&gt;)/g, '<span class="tok-com">$1</span>');
    esc = esc.replace(/("[^"]*")/g, '<span class="tok-str">$1</span>');
    esc = esc.replace(/(&lt;\/?)([\w.:-]+)/g, '$1<span class="tok-type">$2</span>');
    return esc;
  }

  function enhanceCode() {
    var blocks = document.querySelectorAll("pre > code, pre");
    var seen = [];
    document.querySelectorAll("pre").forEach(function (pre) {
      if (pre.parentElement && pre.parentElement.classList.contains("code-wrap")) return;
      var codeEl = pre.querySelector("code") || pre;
      var raw = codeEl.textContent;
      var lang = (pre.getAttribute("data-lang") || "").toLowerCase();

      var wrap = document.createElement("div");
      wrap.className = "code-wrap";
      pre.parentNode.insertBefore(wrap, pre);
      wrap.appendChild(pre);

      if (lang) {
        var label = document.createElement("span");
        label.className = "code-lang";
        label.textContent = lang;
        wrap.appendChild(label);
      }

      var btn = document.createElement("button");
      btn.className = "copy-btn";
      btn.type = "button";
      btn.textContent = "Copy";
      btn.addEventListener("click", function () {
        navigator.clipboard.writeText(raw).then(function () {
          btn.textContent = "Copied!";
          btn.classList.add("copied");
          setTimeout(function () { btn.textContent = "Copy"; btn.classList.remove("copied"); }, 1600);
        });
      });
      wrap.appendChild(btn);

      if (lang === "java" || lang === "groovy" || lang === "kotlin") {
        codeEl.innerHTML = highlightJava(raw);
      } else if (lang === "xml" || lang === "html") {
        codeEl.innerHTML = highlightXml(raw);
      }
    });
    void blocks; void seen;
  }

  /* ----------------------------------------------------------- Boot */
  function init() {
    buildNavbar();
    buildSidebar();
    buildFooter();
    wireMobileMenu();
    wireTabs();
    enhanceCode();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
