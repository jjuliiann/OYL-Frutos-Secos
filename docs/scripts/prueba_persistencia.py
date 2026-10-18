"""Prueba HTTP con dos arranques reales del JAR y una base H2 temporal.
Opcional: Python 3 + JDK; ejecutar tras mvn package.
"""
import json, pathlib, socket, subprocess, tempfile, time, urllib.request

root = pathlib.Path(__file__).resolve().parents[2]
jar = root / 'backend/target/oyl-api-1.0.0.jar'
assert jar.exists(), 'Primero ejecutar mvn package en backend/'
http = urllib.request.build_opener(urllib.request.ProxyHandler({}))
with socket.socket() as sock:
    sock.bind(('127.0.0.1', 0)); port = sock.getsockname()[1]
base = f'http://127.0.0.1:{port}/api/'
def request(path, data=None, method=None):
    req = urllib.request.Request(base + path, data=None if data is None else json.dumps(data).encode(),
        headers={'Content-Type':'application/json'}, method=method)
    with http.open(req, timeout=5) as response: return json.load(response)

def stop(proc):
    proc.terminate()
    try: proc.wait(timeout=15)
    except subprocess.TimeoutExpired: proc.kill(); proc.wait()

with tempfile.TemporaryDirectory(prefix='oyl-h2-') as directory:
    def start():
        log = open(pathlib.Path(directory) / 'server.log', 'a')
        proc = subprocess.Popen(['java', '-jar', str(jar), f'--server.port={port}',
            '--spring.datasource.url=jdbc:h2:file:./oyl;DB_CLOSE_ON_EXIT=FALSE'], cwd=directory,
            stdout=log, stderr=subprocess.STDOUT)
        try:
            for _ in range(120):
                if proc.poll() is not None: raise RuntimeError('Spring Boot terminó; revisar server.log')
                try:
                    if request('health')['status'] == 'ok':
                        request('auth/login', {'alias':'admin','password':'Admin1234!'})
                        if len(request('products')) >= 7: return proc, log
                except Exception: pass
                time.sleep(0.25)
            raise RuntimeError('Timeout de arranque')
        except Exception: stop(proc); log.close(); raise
    proc, log = start()
    try:
        admin = request('auth/login', {'alias':'admin','password':'Admin1234!'})
        user = request('auth/register', {'alias':'persistencia','password':'Clave1234!', 'name':'Cliente persistente',
            'email':'demo@example.com','phone':'+56912345678','address':'Dirección ficticia'})
        product = request(f'products?adminId={admin["id"]}', {'name':'Producto persistencia','category':'Frutos secos',
            'description':'Dato sintético','imageUri':'data:image/jpeg;base64,cHJ1ZWJh','presentation':'250 g',
            'price':3500,'stock':12,'minStock':2,'active':True})
        order = request('orders', {'userId':user['id'],'delivery':'RETIRO','contact':'Contacto ficticio',
            'items':[{'productId':product['id'],'quantity':2}]})
        request(f'orders/{order["id"]}/status?adminId={admin["id"]}', {'status':'CONFIRMADO'}, 'PATCH')
    finally: stop(proc); log.close()
    assert (pathlib.Path(directory)/'oyl.mv.db').exists()
    proc, log = start()
    try:
        recovered = request('auth/login', {'alias':'persistencia','password':'Clave1234!'})
        assert recovered['id'] == user['id']
        p = next(p for p in request('products') if p['id'] == product['id'])
        assert p['stock'] == 10 and p['price'] == 3500 and p['imageUri'] == product['imageUri']
        o = next(o for o in request(f'orders?userId={user["id"]}') if o['id'] == order['id'])
        assert o['total'] == 7000 and o['status'] == 'CONFIRMADO' and len(o['lines']) == 1
        print('OK: dos arranques reales; usuario, producto, foto, precio, stock y pedido/estado conservados.')
    finally: stop(proc); log.close()
