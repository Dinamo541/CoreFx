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

  function currentLang() {
    return document.documentElement.getAttribute("data-lang") || "en";
  }

  function setLang(lang) {
    if (lang !== "es") lang = "en";
    applyLang(lang);
    try { localStorage.setItem(LANG_KEY, lang); } catch (e) { /* ignore */ }
  }

  function currentTheme() {
    return document.documentElement.getAttribute("data-theme") || "dark";
  }

  function syncIcons() {
    var dark = currentTheme() === "dark";
    var btns = document.querySelectorAll("[data-theme-toggle]");
    for (var i = 0; i < btns.length; i++) {
      btns[i].textContent = dark ? "🌙" : "☀️";
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
