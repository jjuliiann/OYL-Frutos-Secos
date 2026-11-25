package cl.oyl.products;
/** Reglas de dominio separadas para probarlas sin levantar HTTP ni base de datos. */
public final class ProductRules {
 private ProductRules() {}
 public static void validate(Product p) {
  if(p==null||p.name==null||p.name.isBlank()||p.price<0||p.stock<0||p.minStock<0) throw new IllegalArgumentException("Producto inválido");
 }
 public static int nextStock(int current,int delta) {
  long next=(long)current+delta;
  if(delta==0||current<0||next<0||next>Integer.MAX_VALUE) throw new IllegalArgumentException("Movimiento de stock inválido");
  return (int)next;
 }
}
