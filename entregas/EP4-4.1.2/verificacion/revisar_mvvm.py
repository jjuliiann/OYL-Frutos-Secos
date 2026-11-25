from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
b=root/'android/app/src/main/java/cl/oyl/mobile'
main=(b/'MainActivity.kt').read_text();nav=(b/'ui/navigation/OylNavigation.kt').read_text()
owned=dict(re.findall(r'private val (\w+) by viewModels<(\w+)>\(\)',main))
assert len(owned)==11,owned
classes={}
for p in (b/'viewmodel').rglob('*.kt'):
 s=p.read_text();m=re.search(r'class (\w+)\(',s)
 if m:classes[m[1]]=s
for variable,clazz in owned.items():
 assert clazz in classes,(variable,clazz)
 assert f'{variable}: {clazz}' in nav,(variable,clazz)
 assert f'{variable} = {variable}' in main,(variable,clazz)
for variable,method in re.findall(r'(\w+Vm)::(\w+)',nav):
 assert variable in owned,variable
 assert re.search(r'fun '+method+r'\(',classes[owned[variable]]),(variable,method)
assert 'repository' not in classes['OylViewModel']
for obsolete in ['fun login(', 'fun addProduct(', 'fun confirmOrder(', 'fun saveProduct(', 'fun setStatus(']:
 assert obsolete not in classes['OylViewModel'],obsolete
assert 'MutableStateFlow' not in classes['OylViewModel']
assert 'Page.CART -> if (isClient)' in nav
assert 'Page.CHECKOUT -> if (isClient)' in nav
assert 'Page.PRODUCTS -> if (isAdmin)' in nav
assert 'Page.INVENTORY -> if (isAdmin)' in nav
assert 'version = 4' in (b/'data/local/LocalDb.kt').read_text()
assert not list((b/'model').glob('*.kt')),'Quedan modelos sin agrupar'
assert not (b/'ui/qr/QrParser.kt').exists()
assert (b/'domain/qr/QrParser.kt').exists()
qr=(b/'ui/qr/QrScreen.kt').read_text()
assert 'vm.result.collectAsState()' in qr and 'vm.resolve(payload)' in qr
assert 'currentProducts.firstOrNull' not in qr
for p in (b/'ui').rglob('*.kt'):
 assert 'import cl.oyl.mobile.data.local.LocalDb' not in p.read_text(),p
print('OK: 11 ViewModels Activity/ViewModelProvider, callbacks conectados, estado transversal encapsulado, modelos agrupados, QR resuelto por ViewModel, rutas por rol y DB v4 conservados. No es ejecución de Compose.')
