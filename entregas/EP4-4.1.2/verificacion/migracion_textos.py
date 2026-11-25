"""Ejecuta el SQL real de la migración sobre SQLite y comprueba conservación de datos."""
from pathlib import Path
import re, sqlite3
root=Path(__file__).resolve().parents[1]
source=(root/'android/app/src/main/java/cl/oyl/mobile/data/local/migration/DisplayCopyMigration.kt').read_text()
queries=re.findall(r'"""(UPDATE.*?)"""', source, re.S)
db=sqlite3.connect(':memory:')
db.executescript('''
CREATE TABLE accounts(id INTEGER, alias TEXT, name TEXT);
CREATE TABLE active_session(id INTEGER, alias TEXT, name TEXT);
CREATE TABLE products(id INTEGER, name TEXT, description TEXT, stock INTEGER);
CREATE TABLE cached_products(id INTEGER, name TEXT, description TEXT);
CREATE TABLE pickup_locations(id INTEGER, name TEXT, hours TEXT, latitude REAL, longitude REAL);
CREATE TABLE local_notices(id INTEGER, title TEXT);
CREATE TABLE orders(id INTEGER, total INTEGER);
CREATE TABLE cart(userId INTEGER, productId INTEGER, quantity INTEGER);
INSERT INTO accounts VALUES(2,'cliente','Cliente de demostración'),(3,'personal','Mi nombre');
INSERT INTO active_session VALUES(2,'cliente','Cliente de demostración');
INSERT INTO products VALUES(1,'Almendras','Almendras seleccionadas para tu alimentación diaria. Producto de demostración OYL.',17),(8,'Personalizado','Mi descripción personalizada',9);
INSERT INTO cached_products VALUES(1,'Almendras','Almendras seleccionadas para tu alimentación diaria. Producto de demostración OYL.');
INSERT INTO pickup_locations VALUES(1,'Punto de prueba Santiago Centro','Referencia: 09:00–18:00',-33.4489,-70.6693);
INSERT INTO local_notices VALUES(1,'Promoción de demostración');
INSERT INTO orders VALUES(10,2990);
INSERT INTO cart VALUES(2,1,3);
''')
for _ in range(2):
    for query in queries: db.execute(query)
assert len(queries)==9
assert db.execute('SELECT name FROM accounts WHERE id=2').fetchone()==('Cliente',)
assert db.execute('SELECT name FROM active_session').fetchone()==('Cliente',)
assert db.execute('SELECT name FROM accounts WHERE id=3').fetchone()==('Mi nombre',)
assert db.execute('SELECT description,stock FROM products WHERE id=1').fetchone()==('Almendras seleccionadas para tu alimentación diaria.',17)
assert db.execute('SELECT description,stock FROM products WHERE id=8').fetchone()==('Mi descripción personalizada',9)
assert db.execute('SELECT name,hours,latitude,longitude FROM pickup_locations').fetchone()==('Punto de retiro Santiago Centro','Horario: 09:00–18:00',-33.4489,-70.6693)
assert db.execute('SELECT title FROM local_notices').fetchone()==('Novedades OYL',)
assert db.execute('SELECT * FROM orders').fetchall()==[(10,2990)]
assert db.execute('SELECT * FROM cart').fetchall()==[(2,1,3)]
print('OK: 9 consultas; textos actualizados; pedidos, stock, carrito, coordenadas y datos personalizados conservados; SQL idempotente.')
