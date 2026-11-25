from pathlib import Path
import re,zipfile
root=Path(__file__).resolve().parents[1]
base=root/'android/app/src/main/java/cl/oyl/mobile'
files=list(base.rglob('*.kt'));exports={}
for f in files:
 s=f.read_text();pkg=re.search(r'^package (.*)',s)[1]
 expected='cl.oyl.mobile'+('.'+str(f.parent.relative_to(base)).replace('/','.') if f.parent!=base else '')
 assert pkg==expected,(f,pkg,expected)
 for m in re.finditer(r'^(?:(?:internal|private|abstract|data|enum|sealed|suspend) )*(?:class|interface|object|fun|val) (?:[\w<>?]+\.)?(\w+)',s,re.M):
  key=pkg+'.'+m[1]
  assert key not in exports,(key,f)
  exports[key]=f
for f in (root/'android/app/src').rglob('*.kt'):
 for ref in re.findall(r'^import (cl\.oyl\.mobile\.[\w.]+)$',f.read_text(),re.M):
  assert ref=='cl.oyl.mobile.R' or ref in exports,(f,ref)
assert sorted(p.name for p in base.glob('*.kt'))==['MainActivity.kt']
assert len(files)>=65,len(files)
assert 'version = 4' in (base/'data/local/LocalDb.kt').read_text()
assert '.addMigrations(DisplayCopyMigration, PackageLayoutMigration, CheckoutMigration)' in (base/'data/local/LocalDb.kt').read_text()
print(f'OK: {len(files)} archivos principales, paquetes/rutas coherentes, imports internos resueltos, sin símbolos duplicados y cadena de migraciones registrada.')
