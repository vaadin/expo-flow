// Continuously cycles --aura-accent-color-light and --aura-background-color-light through the hue spectrum
(function () {
  const speed = 15; // degrees per second

  let start = null;
  let rafId = null;

  function tick(timestamp) {
    if (!start) start = timestamp;
    const elapsed = (timestamp - start) / 1000;

    const hueAccent = (elapsed * speed) % 360;
    const hueBackground = (hueAccent + 180) % 360; // complementary

    const root = document.documentElement;
    root.style.setProperty('--aura-accent-color-light', `hsl(${hueAccent}, 90%, 50%)`);
    root.style.setProperty('--aura-background-color-light', `hsl(${hueBackground}, 80%, 50%)`);

    rafId = requestAnimationFrame(tick);
  }

  window.colorCycle = {
    start() {
      if (!rafId) {
        start = null;
        rafId = requestAnimationFrame(tick);
      }
    },
    stop() {
      if (rafId) {
        cancelAnimationFrame(rafId);
        rafId = null;
      }
      const root = document.documentElement;
      root.style.removeProperty('--aura-accent-color-light');
      root.style.removeProperty('--aura-background-color-light');
    },
  };
})();
