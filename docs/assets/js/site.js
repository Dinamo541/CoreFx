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
  var root = document.documentElement;
  var depth = parseInt(body.getAttribute("data-depth") || "0", 10);
  var active = body.getAttribute("data-active") || "";
  var P = depth === 0 ? "" : (depth === 1 ? "../" : "../../");
  /* The persistent chrome (navbar, sidebar, footer) is built ONCE and survives all
     partial (PJAX) navigations, even across depths — so its links must be ABSOLUTE,
     resolved against the docs root now. Content links stay relative (they belong to
     the freshly fetched page, resolved against the URL we pushState to). */
  var BASE = (function () {
    try { return new URL(P || "./", window.location.href).href; } catch (e) { return P; }
  })();

  function url(rel) { return BASE + rel; }
  function pkgUrl(s) { return url("packages/" + s + ".html"); }
  function clsUrl(p, c) { return url("classes/" + p + "/" + c + ".html"); }

  /* Bilingual snippet: emits both languages; CSS shows the active one. If no
     Spanish is supplied, the text is language-neutral and shown as-is. */
  function bi(en, es) {
    if (es == null) return en;
    return '<span lang="en">' + en + '</span><span lang="es">' + es + "</span>";
  }

  /* ----------------------------------------------------------- SVG icons
     A small stroke-based icon set (Lucide-style) that replaces the emojis in
     the navbar, sidebar and mobile menu, so the iconography is consistent. */
  var SVG = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">';
  var ICONS = {
    home: SVG + '<path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V21h14V9.5"/></svg>',
    navigation: SVG + '<circle cx="12" cy="12" r="10"/><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"/></svg>',
    ui: SVG + '<rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/></svg>',
    util: SVG + '<path d="M14.7 6.3a4 4 0 0 0-5.66 5.66l-6.02 6.02a1.5 1.5 0 0 0 2.12 2.12l6.02-6.02a4 4 0 0 0 5.66-5.66l-2.4 2.4a2 2 0 1 1-2.12-2.12z"/></svg>',
    persistence: SVG + '<ellipse cx="12" cy="5" rx="8" ry="3"/><path d="M4 5v6c0 1.66 3.58 3 8 3s8-1.34 8-3V5"/><path d="M4 11v6c0 1.66 3.58 3 8 3s8-1.34 8-3v-6"/></svg>',
    github: '<svg viewBox="0 0 16 16" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0016 8c0-4.42-3.58-8-8-8z"/></svg>'
  };
  /* Theme-toggle icons carry their own class so CSS can cross-fade them. */
  var SUN_ICON = '<svg class="i-sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>';
  var MOON_ICON = '<svg class="i-moon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>';
  function icon(slug) { return ICONS[slug] || ""; }

  /* Serious, consistent SVGs that replace the per-callout emojis, chosen by the
     callout's type (note / tip / warning / danger) — applied by site.js so all
     18 pages get the same treatment without editing each file. */
  var CALLOUT_ICONS = {
    note:    SVG + '<circle cx="12" cy="12" r="10"/><line x1="12" y1="11" x2="12" y2="16"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>',
    tip:     SVG + '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>',
    warning: SVG + '<path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
    danger:  SVG + '<polygon points="7.86 2 16.14 2 22 7.86 22 16.14 16.14 22 7.86 22 2 16.14 2 7.86"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>'
  };
  function replaceCalloutIcons() {
    var types = ["danger", "warning", "tip", "note"];
    document.querySelectorAll(".callout").forEach(function (c) {
      var ico = c.querySelector(".c-icon");
      if (!ico) return;
      var type = "note";
      for (var i = 0; i < types.length; i++) {
        if (c.classList.contains(types[i])) { type = types[i]; break; }
      }
      ico.innerHTML = CALLOUT_ICONS[type];
    });
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
        '<button class="icon-btn nav-mobile-btn" data-toggle-sidebar aria-label="Open menu"><span class="ham"><i></i><i></i><i></i></span></button>' +
        '<a class="nav-brand" href="' + url("index.html") + '"><img src="' + url("assets/img/CoreFXLogo.png") + '" alt="CoreFx" class="nav-logo" />CoreFx</a>' +
        '<nav class="nav-links">' + links + "</nav>" +
        '<span class="nav-spacer"></span>' +
        '<div class="nav-actions">' +
          '<div class="lang-switch" role="group" aria-label="Language / Idioma">' +
            '<button type="button" data-lang-set="en" title="English">EN</button>' +
            '<button type="button" data-lang-set="es" title="Español">ES</button>' +
          "</div>" +
          '<button class="icon-btn theme-toggle" data-theme-toggle aria-label="Toggle theme">' + SUN_ICON + MOON_ICON + "</button>" +
        "</div>" +
      "</div>";
    if (window.CoreFxTheme) window.CoreFxTheme.sync();
  }

  /* ----------------------------------------------------------- Sidebar */
  function buildSidebar() {
    var host = document.getElementById("sidebar");
    if (!host) return;
    var homeActive = active === "home" ? " active" : "";
    var html = '<a class="sb-home' + homeActive + '" href="' + url("index.html") + '"><span class="sb-ico">' + ICONS.home + "</span>" + bi("Home", "Inicio") + "</a>";
    PACKAGES.forEach(function (p) {
      var isActivePkg = (active === p.slug) || p.classes.some(function (c) { return c.slug === active; });
      var open = isActivePkg ? " open" : "";
      var links = p.classes.map(function (c) {
        var cls = c.slug === active ? ' class="active"' : "";
        return '<a' + cls + ' data-slug="' + c.slug + '" href="' + clsUrl(p.slug, c.slug) + '">' + c.name + "</a>";
      }).join("");
      var pkgColor = active === p.slug ? "var(--accent)" : "inherit";
      html +=
        "<details class=\"sb-group\" data-slug=\"" + p.slug + "\"" + open + ">" +
          "<summary><span class=\"sb-ico\">" + icon(p.slug) + "</span>" +
            '<a href="' + pkgUrl(p.slug) + '" data-pkg="' + p.slug + '" style="color:' + pkgColor + '">' + bi(p.title, p.title_es) + "</a>" +
            '<span class="chev">▶</span></summary>' +
          '<div class="sb-links"><div class="sb-links-inner">' + links + "</div></div>" +
        "</details>";
    });
    html += '<div class="mm-sep"></div>';
    html += '<a class="sb-home sb-ext" href="' + GITHUB + '" target="_blank" rel="noopener"><span class="sb-ico">' + ICONS.github + "</span>GitHub</a>";
    // User settings panel (background speed + animations on/off).
    html += '<div class="mm-sep"></div>';
    html +=
      '<div class="sb-settings">' +
        '<div class="set-title">' + bi("Settings", "Configuración") + "</div>" +
        '<div class="set-row">' +
          '<span class="set-label">' + bi("Background", "Fondo vivo") + "</span>" +
          '<input type="range" class="anim-range" min="0" max="100" step="1" value="62" ' +
            'aria-label="Velocidad del fondo / Background speed" />' +
        "</div>" +
        '<div class="set-row">' +
          '<span class="set-label">' + bi("Animations", "Animaciones") + "</span>" +
          '<button type="button" class="anim-toggle" data-anim-toggle aria-pressed="true" ' +
            'aria-label="Animaciones / Animations"><span class="knob"></span></button>' +
        "</div>" +
      "</div>";
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

  /* ----------------------------------------------------------- Drawer host */
  /* Pages without a sidebar (the homepage) get one injected here so the ☰ button
     opens the SAME collapsible drawer everywhere. buildSidebar then fills it. */
  function ensureSidebarHost() {
    if (document.getElementById("sidebar")) return; // inner pages already have one
    var nav = document.getElementById("navbar");
    if (!nav) return;
    var backdrop = document.createElement("div");
    backdrop.className = "backdrop";
    backdrop.setAttribute("data-close-sidebar", "");
    var aside = document.createElement("aside");
    aside.className = "sidebar";
    aside.id = "sidebar";
    nav.parentNode.insertBefore(backdrop, nav.nextSibling);
    nav.parentNode.insertBefore(aside, nav.nextSibling);
  }

  /* The menu is entirely user-driven: it opens/closes ONLY via the ☰ button or a
     click on the backdrop — never on its own, not even when you follow a link
     inside it (the new page restores it open). Its open/closed state is persisted
     so it always looks exactly as you left it. .sidebar-animating is added only
     around an actual toggle, so a RESTORED-open menu appears instantly. */
  var sbAnimTimer = null;
  function setSidebarOpen(open) {
    if (open) { body.classList.add("sidebar-open"); root.setAttribute("data-sb-open", ""); }
    else { body.classList.remove("sidebar-open"); root.removeAttribute("data-sb-open"); }
    try { localStorage.setItem("corefx-sb-open", open ? "1" : "0"); } catch (e) { /* ignore */ }
  }
  function animateSidebar(open) {
    body.classList.add("sidebar-animating");
    setSidebarOpen(open);
    window.clearTimeout(sbAnimTimer);
    sbAnimTimer = window.setTimeout(function () { body.classList.remove("sidebar-animating"); }, 520);
  }
  function wireMobileMenu() {
    document.addEventListener("click", function (ev) {
      if (ev.target.closest("[data-toggle-sidebar]")) {
        animateSidebar(!body.classList.contains("sidebar-open"));
      } else if (ev.target.closest("[data-close-sidebar]")) {
        animateSidebar(false);
      }
    });
  }

  /* ----------------------------------------------------------- Menu memory */
  /* Remember which groups (the ▶ triangles) are expanded and whether the menu is
     open, so closing/reopening the menu — or navigating between pages — keeps it
     as you left it. Expanded groups are stored as a list of package slugs. */
  var SB_GROUPS_KEY = "corefx-sb-groups";
  function readGroups() {
    try { var s = localStorage.getItem(SB_GROUPS_KEY); return s ? JSON.parse(s) : null; }
    catch (e) { return null; }
  }
  function writeGroups(arr) {
    try { localStorage.setItem(SB_GROUPS_KEY, JSON.stringify(arr)); } catch (e) { /* ignore */ }
  }
  function openGroupSlugs(host) {
    var out = [];
    host.querySelectorAll("details.sb-group").forEach(function (g) {
      if (g.open) out.push(g.getAttribute("data-slug"));
    });
    return out;
  }
  /* Replay the "options appear one by one" animation every time a group expands.
     We re-add .just-opened after a reflow so the CSS @keyframes restarts reliably,
     then drop it once it's done so :hover transforms work again. */
  function playGroupOpen(g) {
    g.classList.remove("just-opened");
    void g.offsetWidth;            // force reflow → the animation restarts cleanly
    g.classList.add("just-opened");
    window.clearTimeout(g._jt);
    g._jt = window.setTimeout(function () { g.classList.remove("just-opened"); }, 950);
  }
  function wireGroupMemory(host) {
    var saved = readGroups();
    if (saved) {
      // Union: keep the active group open (set by buildSidebar) AND re-open every
      // group the user had expanded. We never force-collapse — that was the bug.
      host.querySelectorAll("details.sb-group").forEach(function (g) {
        if (saved.indexOf(g.getAttribute("data-slug")) !== -1) g.open = true;
      });
    }
    writeGroups(openGroupSlugs(host)); // persist the resulting open set
    // 'toggle' does not bubble — listen in the capture phase.
    host.addEventListener("toggle", function (ev) {
      var g = ev.target;
      if (!g || !g.matches || !g.matches("details.sb-group")) return;
      writeGroups(openGroupSlugs(host));
      // Animate the sub-options in on EVERY expand (but not on the initial restore,
      // which happens before theme-ready — that keeps the load looking settled).
      if (g.open) { if (body.classList.contains("theme-ready")) playGroupOpen(g); }
      else { window.clearTimeout(g._jt); g.classList.remove("just-opened"); }
    }, true);
  }
  function restoreSidebarOpen() {
    var open = false;
    try { open = localStorage.getItem("corefx-sb-open") === "1"; } catch (e) { /* ignore */ }
    if (open) { body.classList.add("sidebar-open"); root.setAttribute("data-sb-open", ""); }
    else { root.removeAttribute("data-sb-open"); }
  }

  /* ----------------------------------------------------------- User settings */
  /* Slider 0..100 (higher = faster). It maps to a duration multiplier on the live
     background (smaller = quicker). Default ~62 ≈ a calm ×1.0. Plus an on/off
     switch for ALL animations. Both persist and are applied before paint (theme.js). */
  var BG_SPEED_KEY = "corefx-bg-speed";
  var ANIM_OFF_KEY = "corefx-anim-off";
  function speedFactor(v) { return (2.0 - (v / 100) * 1.6).toFixed(3); } // v 0→2.0(slow) … 100→0.4(fast)
  function applyBgSpeed(v) {
    root.style.setProperty("--bg-speed", speedFactor(v));
    root.style.setProperty("--range-fill", v + "%");
  }
  function wireSettings() {
    var range = document.querySelector(".anim-range");
    var toggle = document.querySelector("[data-anim-toggle]");
    if (range) {
      var v = 62;
      try { var s = localStorage.getItem(BG_SPEED_KEY); if (s !== null) v = +s; } catch (e) { /* ignore */ }
      range.value = v;
      applyBgSpeed(v);
      range.addEventListener("input", function () {
        applyBgSpeed(+range.value);
        try { localStorage.setItem(BG_SPEED_KEY, range.value); } catch (e) { /* ignore */ }
      });
    }
    if (toggle) {
      toggle.setAttribute("aria-pressed", root.classList.contains("anim-off") ? "false" : "true");
      toggle.addEventListener("click", function () {
        setAnimOff(!root.classList.contains("anim-off"));
      });
    }
  }
  function setAnimOff(off) {
    root.classList.toggle("anim-off", off);
    var toggle = document.querySelector("[data-anim-toggle]");
    if (toggle) toggle.setAttribute("aria-pressed", off ? "false" : "true");
    try { localStorage.setItem(ANIM_OFF_KEY, off ? "1" : "0"); } catch (e) { /* ignore */ }
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

  /* ----------------------------------------------------------- Scroll reveal */
  /* Elements fade + slide into place as they enter the viewport. We only ADD the
     .reveal class from JS, so if JS fails or IntersectionObserver is missing the
     content stays fully visible (progressive enhancement). */
  function wireScrollReveal() {
    if (!("IntersectionObserver" in window)) return;
    var selector = ".feature, .card, .content h2, .callout, .api-method, .tabs, .page-nav, table.tbl";
    var els = Array.prototype.slice.call(document.querySelectorAll(selector));
    if (!els.length) return;

    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (e.isIntersecting) {
          e.target.classList.add("is-visible");
          io.unobserve(e.target);
        }
      });
    }, { threshold: 0.1, rootMargin: "0px 0px -40px 0px" });

    els.forEach(function (el) {
      el.classList.add("reveal");
      io.observe(el);
    });

    // Stagger items inside each grid so they cascade instead of popping together.
    document.querySelectorAll(".feature-grid, .card-grid").forEach(function (grid) {
      Array.prototype.slice.call(grid.children).forEach(function (child, i) {
        if (child.classList.contains("reveal")) {
          child.style.transitionDelay = (i % 4) * 70 + "ms";
        }
      });
    });
  }

  /* ----------------------------------------------------------- Scroll chrome */
  /* A thin gradient reading-progress bar, plus a shadow under the navbar once
     the page is scrolled away from the very top. */
  function wireScrollChrome() {
    var bar = document.createElement("div");
    bar.className = "scroll-progress";
    document.body.appendChild(bar);
    var navbar = document.querySelector(".navbar");

    function onScroll() {
      var st = window.scrollY || document.documentElement.scrollTop || 0;
      var max = document.documentElement.scrollHeight - document.documentElement.clientHeight;
      bar.style.transform = "scaleX(" + (max > 0 ? st / max : 0) + ")";
      if (navbar) navbar.classList.toggle("scrolled", st > 8);
    }
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
  }

  /* ----------------------------------------------------------- Background */
  /* Three blurred colour orbs that slowly drift behind everything, giving the
     flat background depth. Injected once, sits at z-index -1 (see CSS). */
  function injectBackground() {
    if (document.querySelector(".bg-layer")) return;
    var layer = document.createElement("div");
    layer.className = "bg-layer";
    layer.setAttribute("aria-hidden", "true");
    layer.innerHTML = '<span class="orb o1"></span><span class="orb o2"></span><span class="orb o3"></span>';
    document.body.appendChild(layer);
  }

  /* ----------------------------------------------------------- Card spotlight */
  /* A soft highlight that follows the cursor across cards/features. We just feed
     the pointer position into two CSS custom properties; the CSS draws the glow
     and the brightening border (see .card::before / ::after). */
  function wireCardSpotlight() {
    document.addEventListener("pointermove", function (ev) {
      var el = ev.target.closest && ev.target.closest(".card, .feature");
      if (!el) return;
      var r = el.getBoundingClientRect();
      el.style.setProperty("--mx", (ev.clientX - r.left) + "px");
      el.style.setProperty("--my", (ev.clientY - r.top) + "px");
    }, { passive: true });
  }

  /* ----------------------------------------------------------- Stat counters */
  /* Numbers in the hero trust-bar count up from 0 the first time they appear.
     An optional data-suffix (e.g. "%") is kept attached the whole way up, so the
     "100%" stat also animates 0% → 100%. */
  function setNum(el, n) { el.textContent = n + (el.getAttribute("data-suffix") || ""); }
  function wireCounters() {
    var nums = document.querySelectorAll(".trust .num[data-count]");
    if (!nums.length) return;
    var reduce = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    if (reduce || !("IntersectionObserver" in window)) {
      nums.forEach(function (n) { setNum(n, n.getAttribute("data-count")); });
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (!e.isIntersecting) return;
        io.unobserve(e.target);
        var el = e.target, target = parseInt(el.getAttribute("data-count"), 10), t0 = null;
        function step(ts) {
          if (!t0) t0 = ts;
          var p = Math.min((ts - t0) / 1100, 1);
          setNum(el, Math.round(p * target));
          if (p < 1) requestAnimationFrame(step);
        }
        requestAnimationFrame(step);
      });
    }, { threshold: 0.6 });
    nums.forEach(function (n) { io.observe(n); });
  }

  /* ----------------------------------------------------------- Per-content wiring */
  /* Everything that depends on the page's MAIN content (not the persistent chrome)
     lives here, so it can be re-run after each partial (PJAX) swap. */
  function enhanceContent() {
    enhanceCode();
    wrapTables();
    replaceCalloutIcons();
    wireTabs();
    wireScrollReveal();
    wireCounters();
  }

  /* Wrap data tables in a horizontal-scroll container so a wide table scrolls
     inside its own box instead of stretching the page (critical on phones). */
  function wrapTables() {
    document.querySelectorAll("#view table.tbl").forEach(function (t) {
      if (t.parentElement && t.parentElement.classList.contains("table-wrap")) return;
      var w = document.createElement("div");
      w.className = "table-wrap";
      t.parentNode.insertBefore(w, t);
      w.appendChild(t);
    });
  }

  /* Reflect the active page in the (persistent) sidebar without rebuilding it, so
     the menu's open/closed groups and scroll position never jump. */
  function updateSidebarActive(act) {
    var sb = document.getElementById("sidebar");
    if (!sb) return;
    sb.querySelectorAll(".sb-home").forEach(function (a) { a.classList.remove("active"); });
    sb.querySelectorAll(".sb-links a").forEach(function (a) { a.classList.remove("active"); });
    sb.querySelectorAll("summary > a[data-pkg]").forEach(function (a) { a.style.color = "inherit"; });
    if (act === "home") {
      var home = sb.querySelector(".sb-home:not(.sb-ext)");
      if (home) home.classList.add("active");
      return;
    }
    var cl = sb.querySelector('.sb-links a[data-slug="' + act + '"]');
    if (cl) {
      cl.classList.add("active");
      var grp = cl.closest("details.sb-group");
      if (grp) {
        grp.open = true; // reveal the section you're in (never collapses the others)
        var s = grp.querySelector("summary > a[data-pkg]"); if (s) s.style.color = "var(--accent)";
      }
    } else {
      var pk = sb.querySelector('summary > a[data-pkg="' + act + '"]');
      if (pk) {
        pk.style.color = "var(--accent)";
        var pg = pk.closest("details.sb-group"); if (pg) pg.open = true;
      }
    }
  }

  /* ----------------------------------------------------------- View transition (PJAX) */
  /* The key to a flicker-free site: we DON'T reload the page. We fetch the target,
     swap only #view (the center — hero + content), and animate just that. The
     navbar, the slide-out menu, the live background and the footer are never
     re-drawn, so the top chrome and an open menu stay perfectly still. The mode
     fits the link:
       · inside the menu     → quick fade (menu/navbar/background stay put).
       · prev/next class nav → horizontal slide, like changing slides.
       · anything else       → a "scroll" that lands softly on the new view.
     Falls back to a normal navigation if anything goes wrong, and just swaps
     instantly (no animation) under prefers-reduced-motion. */
  /* Per-mode timing. The 'leave' value MUST match the CSS leave-animation duration
     for that mode: we swap the content ONLY after the center has fully faded out
     (opacity 0), so the change is never visible mid-fade — that mid-fade swap was
     the "flicker". 'arrive' matches the CSS arrival duration (used to clean up). */
  /* Tiempos por modo, en MILISEGUNDOS. 'leave' DEBE coincidir con la duración de
     SALIDA del CSS (.45s ↔ 450). 'arrive' debe coincidir con la de LLEGADA (.77s ↔ 770).
     Para cambiar la velocidad: edita aquí Y los segundos equivalentes en style.css. */
  var MODES = {
    "menu":       { leave: 380, arrive: 630 },
    "scroll":     { leave: 530, arrive: 810 },
    "slide-next": { leave: 530, arrive: 780 },
    "slide-prev": { leave: 530, arrive: 780 }
  };

  var pjaxBusy = false;
  var arriveTimer = null;
  var currentPath = window.location.pathname;

  /* In-memory page cache. Every internal page is prefetched on idle, so a click
     swaps from memory with no network wait — the transition is instant and there
     is no chance of a half-loaded flash. */
  var pageCache = {};
  function schedule(fn) {
    if (window.requestIdleCallback) window.requestIdleCallback(fn, { timeout: 600 });
    else window.setTimeout(fn, 140);
  }
  function fetchPage(u) {
    if (pageCache[u]) return Promise.resolve(pageCache[u]);
    return fetch(u, { credentials: "same-origin" }).then(function (r) {
      if (!r.ok) throw new Error("HTTP " + r.status);
      return r.text();
    }).then(function (html) { pageCache[u] = html; return html; });
  }
  function prefetchAll() {
    var urls = [url("index.html")];
    PACKAGES.forEach(function (p) {
      urls.push(pkgUrl(p.slug));
      p.classes.forEach(function (c) { urls.push(clsUrl(p.slug, c.slug)); });
    });
    var i = 0;
    (function next() {
      if (i >= urls.length) return;
      var u = urls[i++];
      if (pageCache[u]) { schedule(next); return; }
      fetch(u, { credentials: "same-origin" })
        .then(function (r) { return r.ok ? r.text() : null; })
        .then(function (html) { if (html) pageCache[u] = html; })
        .catch(function () { /* ignore */ })
        .then(function () { schedule(next); });
    })();
  }

  function swapView(html) {
    var doc = new DOMParser().parseFromString(html, "text/html");
    var incoming = doc.getElementById("view");
    var current = document.getElementById("view");
    if (!incoming || !current) return false;
    current.style.opacity = "0";           // belt: stay invisible through the swap
    current.innerHTML = incoming.innerHTML;
    var na = doc.body.getAttribute("data-active");
    if (na != null) body.setAttribute("data-active", na);
    var nd = doc.body.getAttribute("data-depth");
    if (nd != null) body.setAttribute("data-depth", nd);
    if (doc.title) document.title = doc.title;
    updateSidebarActive(body.getAttribute("data-active") || "");
    enhanceContent();
    return true;
  }

  /* Last resort: a real navigation. We still hand the arrival mode to the next
     page (via sessionStorage) so even a full reload animates in instead of just
     flashing — the animation is never "eclipsed" by the reload. */
  function hardNav(targetUrl, mode) {
    try { if (mode) sessionStorage.setItem("cfx-arrive", mode); } catch (e) { /* ignore */ }
    window.location.href = targetUrl;
  }

  function pjaxGo(targetUrl, mode, isPop) {
    if (pjaxBusy) return;
    pjaxBusy = true;
    window.clearTimeout(arriveTimer);
    root.classList.add("pjax-active");
    var cfg = MODES[mode] || MODES.scroll;

    // Animations OFF → swap instantly (no leave/arrive), still no page reload.
    if (root.classList.contains("anim-off") && window.location.protocol !== "file:") {
      fetchPage(targetUrl).then(function (html) {
        if (!swapView(html)) { hardNav(targetUrl, mode); return; }
        if (!isPop) { try { history.pushState({ pjax: 1 }, "", targetUrl); } catch (e) { /* ignore */ } }
        currentPath = window.location.pathname;
        window.scrollTo(0, 0);
        document.getElementById("view").style.opacity = "";
        pjaxBusy = false;
      }).catch(function () { hardNav(targetUrl, mode); });
      return;
    }

    // The view transition plays. Start the leave animation on the center.
    root.classList.remove("is-arriving");
    root.removeAttribute("data-arrive");
    root.setAttribute("data-leave", mode);
    root.classList.add("pt-leave");
    // Swap ONLY after the leave animation has fully hidden/blurred the content.
    var leaveMs = cfg.leave + 20;

    // file:// can't fetch another page (browser security), so partial swap is
    // impossible — it must be a real reload. We play the FULL leave first so the
    // center is already faded out when the reload happens (no abrupt black cut),
    // then the next page animates IN via the cfx-arrive handoff.
    if (window.location.protocol === "file:") {
      window.setTimeout(function () { hardNav(targetUrl, mode); }, leaveMs);
      return;
    }

    var fetchP = fetchPage(targetUrl);
    var waitP = new Promise(function (res) { window.setTimeout(res, leaveMs); });

    Promise.all([fetchP, waitP]).then(function (vals) {
      if (!swapView(vals[0])) { hardNav(targetUrl, mode); return; }
      if (!isPop) { try { history.pushState({ pjax: 1 }, "", targetUrl); } catch (e) { /* ignore */ } }
      currentPath = window.location.pathname;
      window.scrollTo(0, 0);
      var view = document.getElementById("view");
      root.classList.remove("pt-leave");
      root.removeAttribute("data-leave");
      void view.offsetWidth;               // restart the animation cleanly
      root.classList.add("is-arriving");
      root.setAttribute("data-arrive", mode);
      view.style.opacity = "";             // release; the arrival keyframe takes over
      window.clearTimeout(arriveTimer);
      arriveTimer = window.setTimeout(function () {
        root.classList.remove("is-arriving");
        root.removeAttribute("data-arrive");
      }, cfg.arrive + 80);
      pjaxBusy = false;
    }).catch(function () { hardNav(targetUrl, mode); });
  }

  function wirePageTransition() {
    // If we arrived via a full reload that still wants to animate in (theme.js set
    // is-arriving from the sessionStorage fallback), clean the marks up afterwards.
    if (root.classList.contains("is-arriving")) {
      var am = root.getAttribute("data-arrive") || "scroll";
      var d = (MODES[am] || MODES.scroll).arrive + 80;
      window.setTimeout(function () {
        root.classList.remove("is-arriving");
        root.removeAttribute("data-arrive");
      }, d);
    }

    document.addEventListener("click", function (ev) {
      if (ev.defaultPrevented || ev.button === 1 || ev.metaKey || ev.ctrlKey || ev.shiftKey || ev.altKey) return;
      var a = ev.target.closest("a[href]");
      if (!a || a.target === "_blank" || a.hasAttribute("download")) return;
      var raw = a.getAttribute("href");
      if (!raw || raw.charAt(0) === "#") return; // in-page anchor → let the browser scroll
      var dest;
      try { dest = new URL(a.href, window.location.href); } catch (e) { return; }
      if (dest.origin !== window.location.origin) return;                 // external site
      var proto = dest.protocol;
      if (proto !== "http:" && proto !== "https:" && proto !== "file:") return; // mailto / tel / …
      if (dest.pathname === window.location.pathname && dest.hash) return; // same page, just a hash

      var mode = "scroll";
      if (a.closest(".sidebar") || a.closest(".mobile-menu")) mode = "menu";
      else if (a.closest(".page-nav")) mode = a.classList.contains("prev") ? "slide-prev" : "slide-next";

      ev.preventDefault();
      pjaxGo(dest.href, mode, false);
    });

    window.addEventListener("popstate", function () {
      if (window.location.pathname === currentPath) return; // hash-only change
      pjaxGo(window.location.href, "scroll", true);
    });
  }

  /* ----------------------------------------------------------- Boot */
  function init() {
    // Persistent chrome — built ONCE, survives every partial navigation.
    injectBackground();
    buildNavbar();
    ensureSidebarHost();
    buildSidebar();
    var sb = document.getElementById("sidebar");
    if (sb) wireGroupMemory(sb);   // restore expanded groups + persist on toggle
    restoreSidebarOpen();          // restore open/closed state (no replay)
    wireSettings();                // background-speed slider + animations on/off
    buildFooter();
    wireMobileMenu();
    wireScrollChrome();
    wireCardSpotlight();
    wirePageTransition();
    // Per-content wiring — re-run after each PJAX swap.
    enhanceContent();
    // Enable theme-change AND menu/accordion transitions only AFTER first paint,
    // so a restored-open menu and the saved theme don't visibly animate on load.
    // Robust trigger: rAF can be paused on a backgrounded tab, so we also arm a
    // setTimeout fallback (classList.add is idempotent). This was the cause of the
    // sub-options animating "sometimes yes, sometimes no".
    function markReady() { body.classList.add("theme-ready"); }
    requestAnimationFrame(function () { requestAnimationFrame(markReady); });
    window.setTimeout(markReady, 90);
    // Prefetch every page in the background so navigations swap from memory
    // (instant, no half-loaded flash).
    prefetchAll();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
