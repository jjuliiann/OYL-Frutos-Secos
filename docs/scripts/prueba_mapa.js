// Prueba de regresión de la lógica de estado, con DOM/Leaflet de prueba.
// No ejecuta un navegador ni descarga mosaicos. Ejecutar: node docs/scripts/prueba_mapa.js
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const assert = require('assert');
const source = fs.readFileSync(path.resolve(__dirname, '../../android/app/src/main/assets/leaflet/pickup-map.js'), 'utf8');
let rect = {left:0,top:0,right:390,bottom:0,width:390,height:0};
let tileRect = {left:0,top:0,right:256,bottom:256,width:256,height:256};
let loaded = true;
const image = {complete:true, get naturalWidth(){return loaded ? 256 : 0;}, classList:{contains:()=>true},getBoundingClientRect:()=>tileRect};
const container = {getBoundingClientRect:()=>rect,querySelectorAll:()=>[image]};
const events = {}; let markers = 0; let tick;
const map = {setView:()=>map, invalidateSize:()=>{}};
const layer = {on:(name,callback)=>{events[name]=callback;return layer;},addTo:()=>layer};
const marker = {addTo:()=>marker,bindTooltip:()=>marker};
const context = {window:{addEventListener:()=>{}},document:{getElementById:()=>container,createElement:()=>({})},
 L:{map:()=>map,tileLayer:()=>layer,circleMarker:()=>{markers++;return marker;}},
 setTimeout:fn=>fn(),setInterval:fn=>{tick=fn;},requestAnimationFrame:fn=>fn(),console};
vm.createContext(context); vm.runInContext(source,context);
context.initPickupMap([{id:1,name:'Centro',latitude:-33.4489,longitude:-70.6693},{id:2,name:'Providencia',latitude:-33.4314,longitude:-70.6093},{id:3,name:'Peñalolén',latitude:-33.4858,longitude:-70.5525}],2,'test-token');
assert.equal(context.window.oylMapReport.status,'loading','Mosaico cargado con contenedor de alto cero NO está listo');
assert.equal(markers,3,'Conserva los tres puntos');
rect={left:0,top:0,right:390,bottom:220,width:390,height:220}; loaded=false;tick();
assert.equal(context.window.oylMapReport.status,'loading','Imagen sin contenido NO está lista');
loaded=true;tileRect={left:400,top:0,right:656,bottom:256,width:256,height:256};tick();
assert.equal(context.window.oylMapReport.status,'loading','Mosaico fuera del área visible NO está listo');
tileRect={left:0,top:0,right:256,bottom:256,width:256,height:256};tick();
assert.equal(context.window.oylMapReport.status,'ready');
assert.equal(context.window.oylMapReport.visibleTiles,1);
assert.equal(context.window.oylMapReport.token,'test-token','Cada documento conserva su identificador para ignorar respuestas anteriores');
loaded=false;events.tileerror();
assert.equal(context.window.oylMapReport.status,'error');
console.log('OK: 8 comprobaciones de regresión de estado/geometría/marcadores/token. DOM simulado: NO verifica WebView ni red.');
