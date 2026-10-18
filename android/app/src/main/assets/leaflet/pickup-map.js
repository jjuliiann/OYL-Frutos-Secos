// Mosaicos reales de OpenStreetMap. Leaflet se incluye localmente.
function initPickupMap(points, selectedId, token) {
  window.oylMapReport = { token: token, status: 'loading', width: 0, height: 0, visibleTiles: 0 };
  try {
    var selected = points.find(function(p) { return p.id === selectedId; }) || points[0];
    var container = document.getElementById('map');
    var map = L.map(container).setView([selected.latitude, selected.longitude], 13);
    var tiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
    });
    var failed = false;
    function report() {
      var rect = container.getBoundingClientRect();
      var visible = Array.from(container.querySelectorAll('img.leaflet-tile')).filter(function(img) {
        var tileRect = img.getBoundingClientRect();
        return img.complete && img.naturalWidth > 0 && img.classList.contains('leaflet-tile-loaded') &&
          tileRect.width > 0 && tileRect.height > 0 && tileRect.right > rect.left &&
          tileRect.left < rect.right && tileRect.bottom > rect.top && tileRect.top < rect.bottom;
      }).length;
      window.oylMapReport = {
        token: token, width: rect.width, height: rect.height, visibleTiles: visible,
        status: rect.width > 0 && rect.height > 0 && visible > 0 ? 'ready' : (failed ? 'error' : 'loading')
      };
    }
    tiles.on('loading', function() { failed = false; report(); });
    tiles.on('tileload', report);
    tiles.on('tileerror', function() { failed = true; report(); });
    tiles.on('load', report);
    tiles.addTo(map);
    points.forEach(function(p) {
      var label = document.createElement('span');
      label.textContent = p.name;
      var chosen = p.id === selectedId;
      L.circleMarker([p.latitude, p.longitude], {
        radius: chosen ? 10 : 7, color: chosen ? '#a14e16' : '#1b6a43',
        fillColor: chosen ? '#eeae56' : '#68a879', fillOpacity: 1, weight: 3
      }).addTo(map).bindTooltip(label, {permanent: chosen, direction: 'top'});
    });
    function resize() { map.invalidateSize({animate: false}); report(); }
    window.addEventListener('resize', resize);
    if (window.ResizeObserver) new ResizeObserver(resize).observe(container);
    requestAnimationFrame(resize);
    setTimeout(resize, 250);
    setInterval(report, 1000);
  } catch (error) {
    window.oylMapReport.status = 'error';
    console.error('OYL map initialization failed', error);
  }
}
