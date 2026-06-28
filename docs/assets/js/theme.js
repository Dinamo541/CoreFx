/* =========================================================================
   CoreFx Docs — theme + language handling
   Loaded synchronously in <head> so the saved theme AND language are applied
   before paint, avoiding a flash of the wrong theme or the wrong language.
   ========================================================================= */
(function () {
  "use strict";

  var STORAGE_KEY = "corefx-theme";
  var LANG_KEY = "corefx-lang";

  function preferred() {
    try {
      var saved = localStorage.getItem(STORAGE_KEY);
      if (saved === "light" || saved === "dark") return saved;
    } catch (e) { /* localStorage may be unavailable */ }
    // Fall back to the OS preference, defaulting to dark (the brand default).
    if (window.matchMedia && window.matchMedia("(prefers-color-scheme: light)").matches) {
      return "light";
    }
    return "dark";
  }

  function apply(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    // Paint the root background to the theme color IMMEDIATELY (before the external
    // CSS even loads), so a reload never shows a white/black flash in the gap.
    document.documentElement.style.backgroundColor = (theme === "light") ? "#ffffff" : "#0d1117";
  }

  // Apply immediately (runs during <head> parsing).
  apply(preferred());

  /* ------------------------------------------------------------- Language */
  function preferredLang() {
    try {
      var saved = localStorage.getItem(LANG_KEY);
      if (saved === "es" || saved === "en") return saved;
    } catch (e) { /* localStorage may be unavailable */ }
    return "en"; // English is the default.
  }

  function applyLang(lang) {
    document.documentElement.setAttribute("data-lang", lang);
    document.documentElement.lang = lang;
  }

  // Apply the saved language before paint, too.
  applyLang(preferredLang());

  /* ------------------------------------------------------------- View transition
     In-app navigation is partial (PJAX in site.js) and needs no handoff. But if PJAX
     ever has to fall back to a real reload, it leaves an arrival mode here; we apply
     it BEFORE paint so even a full reload animates the center in (never a bare flash).
     site.js cleans the marks up. */
  try {
    var arr = sessionStorage.getItem("cfx-arrive");
    if (arr) {
      sessionStorage.removeItem("cfx-arrive");
      document.documentElement.setAttribute("data-arrive", arr);
      document.documentElement.classList.add("is-arriving");
    }
  } catch (e) { /* sessionStorage may be unavailable */ }

  /* Restore the slide-out menu's open state BEFORE paint, so a refresh on a page
     where the menu was left open never flashes closed first. */
  try {
    if (localStorage.getItem("corefx-sb-open") === "1") {
      document.documentElement.setAttribute("data-sb-open", "");
    }
  } catch (e) { /* localStorage may be unavailable */ }

  /* Restore the user's animation settings BEFORE paint: the live-background speed
     (slider 0..100 → duration multiplier) and the global animations on/off switch.
     Applying them this early avoids any flash of the wrong speed or a burst of
     animation before it's disabled. site.js syncs the controls' visible state. */
  try {
    var bgv = localStorage.getItem("corefx-bg-speed");
    if (bgv !== null) {
      var factor = (2.0 - (+bgv / 100) * 1.6).toFixed(3);
      document.documentElement.style.setProperty("--bg-speed", factor);
      document.documentElement.style.setProperty("--range-fill", bgv + "%");
    }
    if (localStorage.getItem("corefx-anim-off") === "1") {
      document.documentElement.classList.add("anim-off");
    }
  } catch (e) { /* localStorage may be unavailable */ }

  function currentLang() {
    return document.documentElement.getAttribute("data-lang") || "en";
  }

  function setLang(lang) {
    if (lang !== "es") lang = "en";
    applyLang(lang);
    try { localStorage.setItem(LANG_KEY, lang); } catch (e) { /* ignore */ }
    // Briefly flag the switch so CSS can fade the newly shown language in.
    var root = document.documentElement;
    root.classList.add("lang-switching");
    window.setTimeout(function () { root.classList.remove("lang-switching"); }, 450);
  }

  function currentTheme() {
    return document.documentElement.getAttribute("data-theme") || "dark";
  }

  function syncIcons() {
    // The sun/moon icons themselves are SVGs shown/hidden by CSS via the
    // [data-theme] attribute, so here we only keep the tooltip/label in sync.
    var dark = currentTheme() === "dark";
    var btns = document.querySelectorAll("[data-theme-toggle]");
    for (var i = 0; i < btns.length; i++) {
      btns[i].setAttribute("title", dark ? "Switch to light theme" : "Switch to dark theme");
      btns[i].setAttribute("aria-label", btns[i].getAttribute("title"));
    }
  }

  function toggle() {
    var next = currentTheme() === "dark" ? "light" : "dark";
    apply(next);
    try { localStorage.setItem(STORAGE_KEY, next); } catch (e) { /* ignore */ }
    syncIcons();
  }

  // Delegated click handler — works no matter when the button is injected.
  document.addEventListener("click", function (ev) {
    if (!ev.target.closest) return;
    var t = ev.target.closest("[data-theme-toggle]");
    if (t) { ev.preventDefault(); toggle(); return; }
    var l = ev.target.closest("[data-lang-set]");
    if (l) { ev.preventDefault(); setLang(l.getAttribute("data-lang-set")); }
  });

  // Keep icons in sync once the DOM (and the injected navbar) is ready.
  document.addEventListener("DOMContentLoaded", syncIcons);

  // Expose for site.js to call right after it injects the navbar.
  window.CoreFxTheme = { toggle: toggle, sync: syncIcons, current: currentTheme };
  window.CoreFxLang = { set: setLang, current: currentLang };
})();
