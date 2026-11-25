"""Solo lectura por defecto. --compra escribe datos identificados en una BD de evaluación."""
import argparse
import json
import sys
import uuid
from urllib.error import HTTPError
from urllib.parse import urlparse
from urllib.request import Request, urlopen

def check(condition, why):
    if not condition: raise RuntimeError(why)

def call(url, method="GET", body=None, token=None, expected=200):
    headers={"Accept":"application/json"}
    if token: headers["Authorization"]="Bearer "+token
    if body is not None: headers["Content-Type"]="application/json"
    req=Request(url,data=None if body is None else json.dumps(body).encode(),headers=headers,method=method)
    try:
        response=urlopen(req,timeout=12)
    except HTTPError as e:
        if e.code==expected: e.close();return None
        raise RuntimeError(f"{method} {urlparse(url).path}: HTTP {e.code}") from None
    with response:
        check(response.status==expected,f"{method} {urlparse(url).path}: {response.status}")
        return json.load(response)

def main():
    cli=argparse.ArgumentParser(description=__doc__);cli.add_argument("--compra",action="store_true")
    cli.add_argument("--gateway",default="http://localhost:8080");cli.add_argument("--eureka",default="http://localhost:8761")
    args=cli.parse_args(); root=args.gateway.rstrip("/")+"/api"
    reg=call(args.eureka.rstrip("/")+"/eureka/apps")["applications"]["application"]
    expected={"OYL-API-GATEWAY","OYL-USER-SERVICE","OYL-PRODUCT-SERVICE","OYL-ORDER-SERVICE"}
    seen=set()
    for app in reg:
        if app["name"] not in expected: continue
        for ins in app["instance"]:
            check(ins["status"]=="UP",app["name"]+" no está UP")
            response=call(ins["statusPageUrl"])
            check(response.get("status") in ["UP","ok"],"Enlace de estado sin respuesta")
        seen.add(app["name"])
    check(seen==expected,"Faltan instancias: "+str(expected-seen))
    print("OK: cuatro instancias UP y sus cuatro enlaces de estado.")
    for name in ["users","products","orders"]:
        check(call(root+"/health/"+name)=={"status":"ok","service":name},name)
    check(call(root+"/health")["service"]=="users","Compatibilidad /health")
    places=call(root+"/locations");catalog=call(root+"/products")
    check(isinstance(catalog,list) and len(places)>0,"Catálogo o puntos no disponibles")
    print(f"OK: 3 rutas de salud, catálogo ({len(catalog)} productos), retiro ({len(places)} puntos).")
    if not args.compra:return
    suffix=uuid.uuid4().hex[:10]
    def req(method,path,body=None,token=None,expected=200):
        return call(root+path,method,body,token,expected)
    a=req("POST","/auth/login",{"alias":"admin","password":"Admin1234!"})
    at=a["token"];aid=a["id"]
    u=req("POST","/auth/register",dict(alias="e"+suffix,password="Cliente1234!",name="Cliente API",email=suffix+"@oyl.cl",phone="912345678",address="Santiago"))
    ut=u["token"];uid=u["id"];check(len(ut.split("."))==3,"JWT")
    cataloguePath="/products?adminId="+str(aid)
    product=req("POST",cataloguePath,dict(name="Nuez EP4 "+suffix,category="Frutos secos",price=1500,stock=5),at)
    pid=product["id"];itemPath=f"/products/{pid}?adminId={aid}"
    product["price"]=1250
    req("PUT",itemPath,product,at);req("POST",f"/products/{pid}/stock?adminId={aid}",{"delta":2},at)
    req("DELETE",f"/products/{pid}?adminId={uid}",token=ut,expected=403)
    place=places[0]
    def buy(q):return dict(userId=uid,delivery="RETIRO",contact="Cliente API",items=[dict(productId=pid,quantity=q)],fulfillment=dict(delivery="RETIRO",recipientName="Cliente API",phone="912345678",email="",pickupLocationId=place["id"],pickupName=place["name"],pickupAddress=place["address"],pickupHours=place["hours"],street="",number="",apartment="",commune="",region="",postalCode="",instructions=""))
    def stock():return next(p["stock"] for p in req("GET","/products") if p["id"]==pid)
    req("POST","/orders",buy(8),ut,409);check(stock()==7,"La compra rechazada cambió el stock")
    out=req("POST","/orders",buy(2),ut)
    check(out["total"]==2500 and stock()==5,"Total o descuento de inventario incorrecto")
    req("PATCH",f"/orders/{out['id']}/status?adminId={aid}",{"status":"CONFIRMADO"},at)
    history=req("GET",f"/orders?userId={uid}",token=ut)
    check(any(x["id"]==out["id"] and x["status"]=="CONFIRMADO" and x["lines"][0]["productId"]==pid for x in history),"Pedido no persistido")
    check(req("DELETE",itemPath,token=at)["active"] is False,"No se desactivó")
    req("PUT",f"/users/{uid}",dict(name="Cliente actualizado",email=suffix+"@oyl.cl",phone="912345678",address="Santiago Centro"),ut)
    print(f"OK: login, JWT, registro, perfil, CRUD (baja lógica), roles, stock insuficiente, compra #{out['id']}, stock 7 -> 5 e historial confirmado.")
if __name__=="__main__":
    try:main()
    except Exception as e: print("FALLO:",e,file=sys.stderr);sys.exit(1)
