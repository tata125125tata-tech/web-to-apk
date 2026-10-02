package com.example.data.model

data class ProjectTemplate(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val isRemoteUrl: Boolean = false,
    val defaultFiles: Map<String, String> = emptyMap()
)

object ProjectTemplates {
    val templates = listOf(
        ProjectTemplate(
            id = "empty",
            name = "Empty Website",
            description = "Clean starter with HTML5, modern CSS3 styling, and JavaScript logic.",
            iconName = "code",
            defaultFiles = mapOf(
                "index.html" to """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>My Web App</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>
  <div class="container">
    <header>
      <div class="badge">Web2APK App</div>
      <h1>Hello Android! 🚀</h1>
      <p>Your HTML/CSS/JS website is ready to become an Android App.</p>
    </header>
    <main>
      <div class="card">
        <h3>Interactive Counter</h3>
        <p>Tap the button below to test JavaScript execution.</p>
        <div class="counter-display" id="count">0</div>
        <div class="btn-group">
          <button id="btn-dec" class="btn btn-outline">- Decrement</button>
          <button id="btn-inc" class="btn btn-primary">+ Increment</button>
        </div>
      </div>
      <div class="card info-card">
        <h3>Device Info</h3>
        <p id="platform-info">Loading...</p>
      </div>
    </main>
    <footer>
      <p>Built with Web2APK IDE</p>
    </footer>
  </div>
  <script src="js/app.js"></script>
</body>
</html>
                """.trimIndent(),
                "css/style.css" to """
:root {
  --primary: #00B4D8;
  --primary-hover: #0077B6;
  --bg: #0F172A;
  --surface: #1E293B;
  --text: #F8FAFC;
  --text-muted: #94A3B8;
  --border: #334155;
  --radius: 16px;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  -webkit-tap-highlight-color: transparent;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

body {
  background-color: var(--bg);
  color: var(--text);
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.container {
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

header {
  text-align: center;
}

.badge {
  display: inline-block;
  background: rgba(0, 180, 216, 0.15);
  color: var(--primary);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 12px;
  border: 1px solid rgba(0, 180, 216, 0.3);
}

h1 {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 8px;
}

p {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.5;
}

.card {
  background: var(--surface);
  border-radius: var(--radius);
  padding: 24px;
  border: 1px solid var(--border);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.card h3 {
  font-size: 18px;
  margin-bottom: 8px;
}

.counter-display {
  font-size: 54px;
  font-weight: 900;
  color: var(--primary);
  text-align: center;
  margin: 20px 0;
}

.btn-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.btn {
  padding: 14px 18px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
}

.btn-primary {
  background: var(--primary);
  color: #0F172A;
}

.btn-primary:active {
  background: var(--primary-hover);
  transform: scale(0.97);
}

.btn-outline {
  background: transparent;
  color: var(--text);
  border: 1px solid var(--border);
}

.btn-outline:active {
  background: rgba(255, 255, 255, 0.05);
  transform: scale(0.97);
}

.info-card {
  padding: 16px 20px;
}

footer {
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
}
                """.trimIndent(),
                "js/app.js" to """
document.addEventListener('DOMContentLoaded', () => {
  let count = 0;
  const countEl = document.getElementById('count');
  const btnInc = document.getElementById('btn-inc');
  const btnDec = document.getElementById('btn-dec');
  const platformInfo = document.getElementById('platform-info');

  btnInc.addEventListener('click', () => {
    count++;
    countEl.textContent = count;
  });

  btnDec.addEventListener('click', () => {
    if (count > 0) count--;
    countEl.textContent = count;
  });

  // Display user agent and viewport dimensions
  const isAndroid = navigator.userAgent.toLowerCase().includes('android');
  platformInfo.innerHTML = `
    <strong>User Agent:</strong> ` + (isAndroid ? 'Android WebView 📱' : 'Desktop / Browser 💻') + `<br>
    <strong>Viewport:</strong> ` + window.innerWidth + `x` + window.innerHeight + ` px<br>
    <strong>Local Storage:</strong> ` + (typeof localStorage !== 'undefined' ? 'Available ✅' : 'Disabled ❌');
});
                """.trimIndent()
            )
        ),
        ProjectTemplate(
            id = "dashboard",
            name = "Modern Mobile Web App",
            description = "Multi-section responsive mobile web app with bottom tabs, theme toggle, and cards.",
            iconName = "dashboard",
            defaultFiles = mapOf(
                "index.html" to """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Pocket Dashboard</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>
  <div class="app-layout">
    <nav class="top-nav">
      <span class="brand">⚡ PocketApp</span>
      <button id="theme-btn" class="icon-btn" aria-label="Toggle theme">🌓</button>
    </nav>
    <main class="content">
      <section id="home-view" class="view active">
        <div class="stat-banner">
          <div>
            <span class="label">Total Balance</span>
            <h2>$24,592.50</h2>
          </div>
          <span class="growth-tag">+14.2%</span>
        </div>
        <div class="action-row">
          <button class="action-btn" onclick="triggerToast('Sent money!')">📤 Send</button>
          <button class="action-btn" onclick="triggerToast('Received request!')">📥 Receive</button>
          <button class="action-btn" onclick="triggerToast('Card frozen!')">🔒 Lock</button>
        </div>
        <div class="recent-list">
          <h4>Recent Activity</h4>
          <div class="item">
            <span>☕ Coffee Shop</span>
            <span class="amount neg">-$4.50</span>
          </div>
          <div class="item">
            <span>💼 Salary Payout</span>
            <span class="amount pos">+$3,200.00</span>
          </div>
          <div class="item">
            <span>🛒 Grocery Mart</span>
            <span class="amount neg">-$68.20</span>
          </div>
        </div>
      </section>
      <section id="settings-view" class="view">
        <h4>Settings & Info</h4>
        <div class="setting-item">
          <span>Push Notifications</span>
          <input type="checkbox" checked>
        </div>
        <div class="setting-item">
          <span>Biometric Login</span>
          <input type="checkbox" checked>
        </div>
        <div class="setting-item">
          <span>App Version</span>
          <span style="color:var(--text-muted)">1.0.0</span>
        </div>
      </section>
    </main>
    <nav class="bottom-nav">
      <button class="tab-btn active" data-target="home-view">🏠 Home</button>
      <button class="tab-btn" data-target="settings-view">⚙️ Settings</button>
    </nav>
    <div id="toast" class="toast"></div>
  </div>
  <script src="js/app.js"></script>
</body>
</html>
                """.trimIndent(),
                "css/style.css" to """
:root {
  --primary: #6366F1;
  --bg: #0F172A;
  --surface: #1E293B;
  --text: #F8FAFC;
  --text-muted: #94A3B8;
  --border: #334155;
  --pos: #10B981;
  --neg: #EF4444;
}

body.light {
  --bg: #F8FAFC;
  --surface: #FFFFFF;
  --text: #0F172A;
  --text-muted: #64748B;
  --border: #E2E8F0;
}

* { box-sizing: border-box; margin: 0; padding: 0; font-family: system-ui, sans-serif; }
body { background: var(--bg); color: var(--text); min-height: 100vh; }
.app-layout { display: flex; flex-direction: column; min-height: 100vh; }
.top-nav { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--border); background: var(--surface); }
.brand { font-size: 18px; font-weight: 800; color: var(--primary); }
.icon-btn { background: none; border: none; font-size: 18px; cursor: pointer; color: var(--text); }
.content { flex: 1; padding: 20px; overflow-y: auto; }
.view { display: none; }
.view.active { display: block; }
.stat-banner { background: linear-gradient(135deg, #4F46E5, #06B6D4); padding: 24px; border-radius: 18px; color: white; display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.stat-banner h2 { font-size: 28px; margin-top: 4px; }
.growth-tag { background: rgba(255,255,255,0.25); padding: 4px 10px; border-radius: 12px; font-weight: 700; font-size: 12px; }
.action-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 24px; }
.action-btn { background: var(--surface); border: 1px solid var(--border); color: var(--text); padding: 14px 8px; border-radius: 14px; font-weight: 600; cursor: pointer; }
.recent-list { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 16px; }
.recent-list h4 { margin-bottom: 12px; }
.item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border); }
.item:last-child { border-bottom: none; }
.amount.pos { color: var(--pos); font-weight: 600; }
.amount.neg { color: var(--neg); font-weight: 600; }
.setting-item { display: flex; justify-content: space-between; align-items: center; padding: 16px 0; border-bottom: 1px solid var(--border); }
.bottom-nav { display: flex; background: var(--surface); border-top: 1px solid var(--border); }
.tab-btn { flex: 1; padding: 14px; border: none; background: none; color: var(--text-muted); font-size: 14px; font-weight: 600; cursor: pointer; }
.tab-btn.active { color: var(--primary); border-top: 2px solid var(--primary); }
.toast { position: fixed; bottom: 80px; left: 50%; transform: translateX(-50%); background: #111827; color: #fff; padding: 10px 20px; border-radius: 20px; border: 1px solid #374151; opacity: 0; pointer-events: none; transition: opacity 0.3s; }
.toast.show { opacity: 1; }
                """.trimIndent(),
                "js/app.js" to """
document.addEventListener('DOMContentLoaded', () => {
  const tabs = document.querySelectorAll('.tab-btn');
  const views = document.querySelectorAll('.view');
  const themeBtn = document.getElementById('theme-btn');

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.classList.remove('active'));
      views.forEach(v => v.classList.remove('active'));
      tab.classList.add('active');
      document.getElementById(tab.dataset.target).classList.add('active');
    });
  });

  themeBtn.addEventListener('click', () => {
    document.body.classList.toggle('light');
  });
});

function triggerToast(msg) {
  const toast = document.getElementById('toast');
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2000);
}
                """.trimIndent()
            )
        ),
        ProjectTemplate(
            id = "game",
            name = "Canvas Touch Game",
            description = "Interactive HTML5 canvas particle tapping arcade game ready for Android touch input.",
            iconName = "sports_esports",
            defaultFiles = mapOf(
                "index.html" to """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>Touch Spark Arcade</title>
  <style>
    body { margin: 0; background: #05050C; overflow: hidden; touch-action: none; font-family: sans-serif; color: white; }
    #ui { position: absolute; top: 16px; left: 16px; pointer-events: none; }
    .score { font-size: 26px; font-weight: bold; color: #38BDF8; }
    .hint { font-size: 13px; color: #94A3B8; margin-top: 4px; }
    canvas { display: block; width: 100vw; height: 100vh; }
  </style>
</head>
<body>
  <div id="ui">
    <div class="score">Score: <span id="scoreVal">0</span></div>
    <div class="hint">Tap anywhere on screen to spark or pop orbs!</div>
  </div>
  <canvas id="gameCanvas"></canvas>
  <script>
    const canvas = document.getElementById('gameCanvas');
    const ctx = canvas.getContext('2d');
    const scoreVal = document.getElementById('scoreVal');
    let score = 0;

    function resize() {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    }
    window.addEventListener('resize', resize);
    resize();

    let particles = [];
    let targets = [];

    class Target {
      constructor() {
        this.radius = 24 + Math.random() * 16;
        this.x = Math.random() * (canvas.width - this.radius * 2) + this.radius;
        this.y = Math.random() * (canvas.height - this.radius * 2) + this.radius;
        this.vx = (Math.random() - 0.5) * 3;
        this.vy = (Math.random() - 0.5) * 3;
        this.hue = Math.floor(Math.random() * 360);
      }
      update() {
        this.x += this.vx;
        this.y += this.vy;
        if (this.x - this.radius < 0 || this.x + this.radius > canvas.width) this.vx *= -1;
        if (this.y - this.radius < 0 || this.y + this.radius > canvas.height) this.vy *= -1;
      }
      draw() {
        ctx.save();
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
        ctx.fillStyle = `hsl(` + this.hue + `, 85%, 60%)`;
        ctx.shadowColor = `hsl(` + this.hue + `, 85%, 60%)`;
        ctx.shadowBlur = 18;
        ctx.fill();
        ctx.restore();
      }
    }

    for (let i = 0; i < 6; i++) targets.push(new Target());

    function spawnSparks(x, y, count = 20) {
      for (let i = 0; i < count; i++) {
        particles.push({
          x, y,
          vx: (Math.random() - 0.5) * 8,
          vy: (Math.random() - 0.5) * 8,
          life: 1,
          decay: 0.02 + Math.random() * 0.03,
          color: `hsl(` + Math.floor(Math.random() * 360) + `, 100%, 70%)`
        });
      }
    }

    function handleInput(x, y) {
      spawnSparks(x, y, 15);
      for (let i = targets.length - 1; i >= 0; i--) {
        const t = targets[i];
        const dist = Math.hypot(x - t.x, y - t.y);
        if (dist <= t.radius + 15) {
          spawnSparks(t.x, t.y, 40);
          score += 100;
          scoreVal.textContent = score;
          targets.splice(i, 1);
          setTimeout(() => targets.push(new Target()), 500);
          break;
        }
      }
    }

    window.addEventListener('pointerdown', (e) => handleInput(e.clientX, e.clientY));

    function loop() {
      ctx.fillStyle = 'rgba(5, 5, 12, 0.3)';
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      targets.forEach(t => { t.update(); t.draw(); });

      for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i];
        p.x += p.vx;
        p.y += p.vy;
        p.life -= p.decay;
        if (p.life <= 0) {
          particles.splice(i, 1);
          continue;
        }
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.life * 4, 0, Math.PI * 2);
        ctx.fillStyle = p.color;
        ctx.fill();
      }

      requestAnimationFrame(loop);
    }
    loop();
  </script>
</body>
</html>
                """.trimIndent()
            )
        ),
        ProjectTemplate(
            id = "remote",
            name = "Website URL Wrapper",
            description = "Wrap any live web app or URL (e.g. https://yourdomain.com) into a native Android WebView app.",
            iconName = "language",
            isRemoteUrl = true,
            defaultFiles = mapOf(
                "index.html" to """
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Offline Fallback</title>
  <style>
    body { font-family: sans-serif; background: #0F172A; color: white; text-align: center; padding: 40px 20px; }
    .icon { font-size: 54px; margin-bottom: 16px; }
    h2 { font-size: 22px; margin-bottom: 8px; }
    p { color: #94A3B8; font-size: 14px; margin-bottom: 24px; }
    button { background: #00B4D8; color: #0F172A; border: none; padding: 12px 24px; border-radius: 8px; font-weight: bold; cursor: pointer; }
  </style>
</head>
<body>
  <div class="icon">📡</div>
  <h2>Connection Offline</h2>
  <p>The remote website could not be reached. Please check your internet connection.</p>
  <button onclick="location.reload()">Retry Connection</button>
</body>
</html>
                """.trimIndent()
            )
        )
    )
}
