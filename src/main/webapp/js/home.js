// src/main/webapp/js/home.js
(() => {
  const canvas = document.getElementById('particleCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  let width = canvas.width = window.innerWidth;
  let height = canvas.height = window.innerHeight;
  const particles = [];
  const particleCount = Math.min(60, Math.floor(width / 24));
  const colors = ['#3b82f6', '#8b5cf6', '#10b981', '#60a5fa'];

  class Particle {
    constructor() {
      this.x = Math.random() * width;
      this.y = Math.random() * height;
      this.vx = (Math.random() - 0.5) * 0.4;
      this.vy = (Math.random() - 0.5) * 0.4;
      this.r = Math.random() * 1.8 + 0.8;
      this.c = colors[Math.floor(Math.random() * colors.length)];
    }
    step() {
      this.x += this.vx;
      this.y += this.vy;
      if (this.x < 0 || this.x > width) this.vx *= -1;
      if (this.y < 0 || this.y > height) this.vy *= -1;
    }
    draw() {
      ctx.beginPath();
      ctx.arc(this.x, this.y, this.r, 0, Math.PI * 2);
      ctx.fillStyle = this.c;
      ctx.globalAlpha = 0.6;
      ctx.fill();
    }
  }

  for (let i = 0; i < particleCount; i++) particles.push(new Particle());

  function resize() {
    width = canvas.width = window.innerWidth;
    height = canvas.height = window.innerHeight;
  }
  window.addEventListener('resize', resize);

  function animate() {
    ctx.clearRect(0, 0, width, height);
    for (let i = 0; i < particles.length; i++) {
      const p = particles[i];
      p.step();
      p.draw();
      for (let j = i + 1; j < particles.length; j++) {
        const o = particles[j];
        const dx = p.x - o.x;
        const dy = p.y - o.y;
        const d = Math.sqrt(dx*dx + dy*dy);
        if (d < 140) {
          ctx.beginPath();
          ctx.moveTo(p.x, p.y);
          ctx.lineTo(o.x, o.y);
          ctx.strokeStyle = p.c;
          ctx.globalAlpha = 0.12 * (1 - d / 140);
          ctx.lineWidth = 1;
          ctx.stroke();
        }
      }
    }
    requestAnimationFrame(animate);
  }
  animate();
})();

(() => {
  const els = document.querySelectorAll('.reveal');
  if (els.length === 0) return;
  const io = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (e.isIntersecting) e.target.classList.add('visible');
    });
  }, { threshold: 0.12 });
  els.forEach(el => io.observe(el));
})();

(() => {
  const counters = document.querySelectorAll('[data-count]');
  if (counters.length === 0) return;
  const io = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (!entry.isIntersecting) return;
      const el = entry.target;
      const target = parseInt(el.getAttribute('data-count'), 10);
      let curr = 0;
      const step = Math.max(1, Math.floor(target / 50));
      const id = setInterval(() => {
        curr += step;
        if (curr >= target) {
          el.textContent = target;
          clearInterval(id);
        } else el.textContent = curr;
      }, 30);
      io.unobserve(el);
    });
  }, { threshold: 0.5 });
  counters.forEach(el => io.observe(el));
})();

(() => {
  const header = document.getElementById('siteHeader');
  if (!header) return;
  let last = 0;
  window.addEventListener('scroll', () => {
    const y = window.scrollY || document.documentElement.scrollTop;
    if (y > last && y > 60) header.style.transform = 'translateY(-110%)';
    else header.style.transform = 'translateY(0)';
    last = y;
  });
})();

(() => {
  const toggle = document.getElementById('menuToggle');
  const nav = document.getElementById('siteNav');
  if (!toggle || !nav) return;
  toggle.addEventListener('click', () => {
    nav.classList.toggle('open');
  });
})();