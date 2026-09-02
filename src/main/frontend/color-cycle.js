// Continuously drifts --aura-accent-color and --aura-background-color through an
// underwater palette: deep blues, turquoise and a hint of sea green.
(function () {
  // Hue band we stay inside: 170 = green-teal, 182 = turquoise, 202 = sky blue.
  // The wave eases at both ends, so the app dwells there - hence 170 rather
  // than a pure sea green, which made green the dominant note instead of the
  // light accent it should be.
  const HUE_MIN = 170;
  const HUE_MAX = 202;
  const BACKGROUND_PERIOD = 24; // seconds for a full drift back and forth
  const ACCENT_PERIOD = 17; // slightly out of sync, so the pair keeps shifting
  const ACCENT_HUE_OFFSET = -18; // accent leans greener/brighter than the water

  let start = null;
  let rafId = null;

  // Smooth 0..1 oscillation, so the drift eases at both ends of the band.
  function wave(elapsed, period) {
    return (1 - Math.cos((2 * Math.PI * elapsed) / period)) / 2;
  }

  function clampHue(hue) {
    return Math.min(HUE_MAX, Math.max(HUE_MIN, hue));
  }

  function tick(timestamp) {
    if (!start) start = timestamp;
    const elapsed = (timestamp - start) / 1000;

    const backgroundWave = wave(elapsed, BACKGROUND_PERIOD);
    const accentWave = wave(elapsed, ACCENT_PERIOD);

    const hueBackground = HUE_MIN + backgroundWave * (HUE_MAX - HUE_MIN);
    const hueAccent = clampHue(HUE_MIN + accentWave * (HUE_MAX - HUE_MIN) + ACCENT_HUE_OFFSET);

    // Gentle brightness swell, like light rippling through water.
    const backgroundLightness = 30 + backgroundWave * 14; // 30%..44%
    const accentLightness = 52 + accentWave * 10; // 52%..62%

    const background = `hsl(${hueBackground}, 65%, ${backgroundLightness}%)`;
    const accent = `hsl(${hueAccent}, 85%, ${accentLightness}%)`;

    const root = document.documentElement;

    // Depth stops for the gradient in bubbles.css: sunlit near the surface,
    // deep blue further down.
    root.style.setProperty('--water-surface', `hsl(${clampHue(hueBackground - 14)}, 70%, ${58 + backgroundWave * 6}%)`);
    root.style.setProperty('--water-mid', background);
    root.style.setProperty('--water-deep', `hsl(${hueBackground + 8}, 72%, ${22 + backgroundWave * 6}%)`);

    //Properties for Light Mode
    root.style.setProperty('--aura-accent-color-light', accent);
    root.style.setProperty('--aura-background-color-light', background);

    //Properties for Dark Mode
    root.style.setProperty('--aura-accent-color-dark', accent);
    root.style.setProperty('--aura-background-color', background);

    rafId = requestAnimationFrame(tick);
  }

  // --- Air bubbles ---------------------------------------------------------
  // The field sits behind the app layout (see bubbles.css) and only exists
  // while the mode is on, so nothing animates when it is switched off.
  const BUBBLE_COUNT = 46;
  let bubbleField = null;

  function random(min, max) {
    return min + Math.random() * (max - min);
  }

  function createBubbleField() {
    const field = document.createElement('div');
    field.className = 'bubble-field';
    field.setAttribute('aria-hidden', 'true');

    for (let i = 0; i < BUBBLE_COUNT; i++) {
      const bubble = document.createElement('div');
      bubble.className = 'bubble';
      const size = random(8, 44);
      // Bigger bubbles rise faster, small ones linger.
      bubble.style.setProperty('--bubble-size', `${size.toFixed(1)}px`);
      // One bubble per column, jittered inside it: plain random clumps and
      // leaves gaps, and the narrow drawer column would often end up empty.
      const column = ((i + random(0, 1)) / BUBBLE_COUNT) * 100;
      bubble.style.setProperty('--bubble-x', `${column.toFixed(1)}%`);
      bubble.style.setProperty('--bubble-drift', `${random(8, 34).toFixed(1)}px`);
      const duration = random(26 - size * 0.3, 38 - size * 0.3);
      bubble.style.setProperty('--bubble-duration', `${duration.toFixed(1)}s`);
      // Negative delay starts each bubble mid-flight, so the field is already
      // populated instead of releasing everything from the bottom at once.
      // Spread over its own duration, so every phase is equally likely.
      bubble.style.setProperty('--bubble-delay', `${(-random(0, duration)).toFixed(1)}s`);
      bubble.style.setProperty('--bubble-opacity', random(0.3, 0.68).toFixed(2));
      field.appendChild(bubble);
    }

    return field;
  }

  function startBubbles() {
    if (!bubbleField) {
      bubbleField = createBubbleField();
      document.body.appendChild(bubbleField);
    }
    // Lets bubbles.css open up the otherwise opaque app-layout background.
    document.documentElement.classList.add('unicorn-mode');
  }

  function stopBubbles() {
    document.documentElement.classList.remove('unicorn-mode');
    if (bubbleField) {
      bubbleField.remove();
      bubbleField = null;
    }
  }

  window.colorCycle = {
    start() {
      startBubbles();
      if (!rafId) {
        start = null;
        rafId = requestAnimationFrame(tick);
      }
    },
    stop() {
      stopBubbles();
      if (rafId) {
        cancelAnimationFrame(rafId);
        rafId = null;
      }
      const root = document.documentElement;
      root.style.removeProperty('--aura-accent-color-light');
      root.style.removeProperty('--aura-background-color-light');

      root.style.removeProperty('--aura-accent-color-dark');
      root.style.removeProperty('--aura-background-color');

      root.style.removeProperty('--water-surface');
      root.style.removeProperty('--water-mid');
      root.style.removeProperty('--water-deep');
    },
  };
})();
