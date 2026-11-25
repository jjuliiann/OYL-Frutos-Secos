from pathlib import Path
import sqlite3,re
root=Path(__file__).resolve().parents[1]
s=(root/'android/app/src/main/java/cl/oyl/mobile/data/local/migration/CheckoutMigration.kt').read_text()
query=re.search(r'const val CREATE_TABLE = """(.*?)"""',s,re.S)[1]
db=sqlite3.connect(':memory:');db.execute('PRAGMA foreign_keys=ON')
db.execute('CREATE TABLE orders (id INTEGER NOT NULL PRIMARY KEY, total INTEGER NOT NULL, contact TEXT NOT NULL)')
db.execute("INSERT INTO orders VALUES (10,2990,'Contacto anterior')")
db.execute(query);db.execute(query)
assert db.execute('SELECT * FROM orders').fetchall()==[(10,2990,'Contacto anterior')]
assert db.execute('SELECT COUNT(*) FROM order_fulfillment').fetchone()==(0,)
columns={x[1]:x[2] for x in db.execute('PRAGMA table_info(order_fulfillment)')}
assert len(columns)==16,columns
assert columns['pickupLocationId']=='INTEGER' and columns['street']=='TEXT'
values=(10,'ENTREGA','Ana Martinez','912345678','ana@example.com',None,'','','','Central','123','4','Santiago','Metropolitana','1234567','Portón azul')
db.execute('INSERT INTO order_fulfillment VALUES ('+','.join('?'*len(values))+')',values)
assert db.execute('SELECT street,number,instructions FROM order_fulfillment WHERE orderId=10').fetchone()==('Central','123','Portón azul')
db.execute('DELETE FROM orders WHERE id=10');assert db.execute('SELECT COUNT(*) FROM order_fulfillment').fetchone()==(0,)
print('OK: migración 3→4, 16 columnas, pedidos anteriores conservados, dirección guardada y eliminación en cascada comprobada en SQLite.')
