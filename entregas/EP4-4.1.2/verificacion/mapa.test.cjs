// Prueba del contrato JS de selección, sin simular una descarga de mosaicos.
const fs=require('fs'),vm=require('vm'),assert=require('assert');
const box={width:400,height:300,left:0,right:400,top:0,bottom:300};
const map={setView(){return this},flyTo(pos,z){this.last={pos,z}},invalidateSize(){}};
const markers=[];
const ctx={console,window:{addEventListener(){}},document:{getElementById(){return {getBoundingClientRect(){return box},querySelectorAll(){return []}}},createElement(){return {}}},
L:{map(){return map},tileLayer(){return {on(){return this},addTo(){}}},circleMarker(pos,style){let m={pos,style,addTo(){return this},bindTooltip(){return this},on(event,fn){this.click=fn;return this},setStyle(s){this.style=s},openTooltip(){this.open=true}};markers.push(m);return m}},
requestAnimationFrame(fn){fn()},setTimeout(fn){fn()},setInterval(){}};
vm.createContext(ctx);
vm.runInContext(fs.readFileSync(__dirname+'/../android/app/src/main/assets/leaflet/pickup-map.js','utf8'),ctx);
const points=[{id:1,name:'Centro',latitude:-33.4489,longitude:-70.6693},{id:2,name:'Providencia',latitude:-33.4314,longitude:-70.6093},{id:3,name:'Peñalolén',latitude:-33.4858,longitude:-70.5525}];
ctx.initPickupMap(points,1,'token');assert.equal(ctx.window.oylMapReport.selectedId,1);
markers[1].click();assert.equal(ctx.window.oylMapReport.selectedId,2);assert.equal(map.last.pos[0],points[1].latitude);
ctx.window.selectPickupPoint(3);assert.equal(ctx.window.oylMapReport.selectedId,3);assert.equal(markers[2].style.radius,10);
ctx.window.selectPickupPoint(999);assert.equal(ctx.window.oylMapReport.selectedId,3);
console.log('OK: selección inicial, clic de marcador, selección desde lista y rechazo de ID inexistente.');
